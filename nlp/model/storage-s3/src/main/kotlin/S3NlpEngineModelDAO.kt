/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.model.service.storage.s3

import ai.tock.aws.s3.S3Bucket
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.model.ClassifierContextKey
import ai.tock.nlp.model.EntityContextKey
import ai.tock.nlp.model.IntentContext.IntentContextKey
import ai.tock.nlp.model.service.storage.NlpApplicationConfigurationDAO
import ai.tock.nlp.model.service.storage.NlpEngineModelDAO
import ai.tock.nlp.model.service.storage.NlpModelStream
import ai.tock.shared.changes.ChangeChecks
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import java.io.InputStream
import java.nio.file.Files
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Stores the model files in an S3 bucket, at `<prefix>intent/<key id>` and `<prefix>entity/<key id>`.
 *
 * S3 has no change stream: the changes are detected by listing the models every [refreshInterval],
 * with a task scheduled by [schedule] when the first listener is registered, and at the start of the requests of
 * a process frozen between them ([ChangeChecks]), at most once by [refreshInterval].
 */
internal class S3NlpEngineModelDAO(
    private val bucket: S3Bucket,
    private val refreshInterval: Duration,
    private val configurationDAO: () -> NlpApplicationConfigurationDAO,
    private val schedule: (Duration, () -> Unit) -> Unit,
) : NlpEngineModelDAO {
    companion object {
        /**
         * The user metadata with the update date of the model, in milliseconds: the last modified date of S3 has a one second precision.
         */
        internal const val UPDATE_DATE_METADATA = "tock-update-date"
        internal const val INTENT_FOLDER = "intent/"
        internal const val ENTITY_FOLDER = "entity/"

        /** The NLP configuration of each model, at `<prefix>configuration/intent/<key id>`... */
        internal const val CONFIGURATION_FOLDER = "configuration/"
    }

    private val logger = KotlinLogging.logger {}

    /**
     * The models of a folder, and the listeners of their changes.
     */
    private inner class ModelFolder(
        val folder: String,
    ) {
        val listeners = CopyOnWriteArrayList<(String) -> Unit>()

        /**
         * The etags of the models at the last poll, by key id, null before the first poll.
         */
        @Volatile
        var etags: Map<String, String>? = null

        fun path(key: ClassifierContextKey): String = folder + key.id()

        fun configurationPath(id: String): String = CONFIGURATION_FOLDER + folder + id

        fun save(
            key: ClassifierContextKey,
            stream: InputStream,
        ) {
            // the size of the model must be known before the upload
            val file = Files.createTempFile("tock-model-", ".bin")
            try {
                Files.newOutputStream(file).use { stream.copyTo(it) }
                // the precision of the metadata: the configuration is read with the date the runtimes read
                val date = Instant.now().truncatedTo(ChronoUnit.MILLIS)
                bucket.put(path(key), file, "application/octet-stream", mapOf(UPDATE_DATE_METADATA to date.toEpochMilli().toString()))
                saveConfiguration(key, date)
            } finally {
                Files.deleteIfExists(file)
            }
        }

        /**
         * Stores the NLP configuration of the model next to it: a runtime without the configuration DAO of the
         * admin (the serverless one) loads the model with it.
         */
        private fun saveConfiguration(
            key: ClassifierContextKey,
            date: Instant,
        ) {
            try {
                val configuration = configurationDAO().loadLastConfiguration(key.applicationName, key.engineType, date)
                if (configuration == null) {
                    bucket.delete(configurationPath(key.id()))
                } else {
                    bucket.put(configurationPath(key.id()), mapper.writeValueAsBytes(configuration), "application/json")
                }
            } catch (e: Exception) {
                logger.error(e) { "the NLP configuration of the model $key is not stored: the runtimes read it from the configuration DAO" }
            }
        }

        /** The configuration stored with the model, or the one of the configuration DAO for the older models. */
        private fun configuration(
            key: ClassifierContextKey,
            date: Instant,
        ): NlpApplicationConfiguration? =
            bucket.getBytes(configurationPath(key.id()))?.let { mapper.readValue<NlpApplicationConfiguration>(it) }
                ?: try {
                    configurationDAO().loadLastConfiguration(key.applicationName, key.engineType, date)
                } catch (e: Exception) {
                    logger.warn(e) { "no NLP configuration for the model $key: the default one is used" }
                    null
                }

        fun inputStream(key: ClassifierContextKey): NlpModelStream? =
            bucket.get(path(key))?.let { stream ->
                val date = updateDate(stream.response().metadata(), stream.response().lastModified())
                NlpModelStream(
                    stream,
                    date,
                    configuration(key, date),
                )
            }

        fun lastUpdate(key: ClassifierContextKey): Instant? = bucket.head(path(key))?.let { updateDate(it.metadata(), it.lastModified()) }

        fun delete(key: ClassifierContextKey) {
            logger.debug { "remove model $key" }
            bucket.delete(listOf(path(key), configurationPath(key.id())))
        }

        fun deleteNotIn(keys: List<ClassifierContextKey>) {
            val ids = keys.map { it.id() }.toSet()
            val removed = ids().keys.filterNot { ids.contains(it) }
            logger.debug { "remove models $removed" }
            bucket.delete(removed.flatMap { listOf(folder + it, configurationPath(it)) })
        }

        /**
         * The etags of the models by key id.
         */
        fun ids(): Map<String, String> = bucket.list(folder).associate { bucket.path(it).removePrefix(folder) to it.eTag() }

        fun poll() {
            val current = ids()
            val previous = etags
            etags = current
            if (previous != null) {
                current
                    .filter { (id, etag) -> previous[id] != etag }
                    .keys
                    .forEach { id -> listeners.forEach { it(id) } }
            }
        }
    }

    private val intents = ModelFolder(INTENT_FOLDER)
    private val entities = ModelFolder(ENTITY_FOLDER)
    private val polling = AtomicBoolean(false)

    @Volatile
    private var lastPoll: Long = 0

    private fun updateDate(
        metadata: Map<String, String>,
        lastModified: Instant,
    ): Instant = metadata[UPDATE_DATE_METADATA]?.toLongOrNull()?.let { Instant.ofEpochMilli(it) } ?: lastModified

    /**
     * Notifies the listeners of the models added or updated since the last call. The first call only reads the current models.
     */
    internal fun pollChanges() {
        lastPoll = System.currentTimeMillis()
        listOf(intents, entities)
            .filter { it.listeners.isNotEmpty() }
            .forEach {
                try {
                    it.poll()
                } catch (e: Exception) {
                    logger.error(e) { "unable to list the models of ${bucket.bucket}/${bucket.key(it.folder)}" }
                }
            }
    }

    private fun listen(
        folder: ModelFolder,
        listener: (String) -> Unit,
    ) {
        folder.listeners.add(listener)
        if (folder.etags == null) {
            // the reference state of the folder
            pollChanges()
        }
        if (polling.compareAndSet(false, true)) {
            schedule(refreshInterval) { pollChanges() }
            ChangeChecks.register("S3 NLP models") {
                if (System.currentTimeMillis() - lastPoll >= refreshInterval.toMillis()) pollChanges()
            }
        }
    }

    override fun getEntityModelInputStream(key: EntityContextKey): NlpModelStream? = entities.inputStream(key)

    override fun saveEntityModel(
        key: EntityContextKey,
        stream: InputStream,
    ) {
        entities.save(key, stream)
    }

    override fun getEntityModelLastUpdate(key: EntityContextKey): Instant? = entities.lastUpdate(key)

    override fun deleteEntityModelsNotIn(keys: List<EntityContextKey>) {
        entities.deleteNotIn(keys)
    }

    override fun deleteEntityModel(key: EntityContextKey) {
        entities.delete(key)
    }

    override fun listenEntityModelChanges(listener: (String) -> Unit) {
        listen(entities, listener)
    }

    override fun getIntentModelInputStream(key: IntentContextKey): NlpModelStream? = intents.inputStream(key)

    override fun saveIntentModel(
        key: IntentContextKey,
        stream: InputStream,
    ) {
        intents.save(key, stream)
    }

    override fun getIntentModelLastUpdate(key: IntentContextKey): Instant? = intents.lastUpdate(key)

    override fun deleteIntentModelsNotIn(keys: List<IntentContextKey>) {
        intents.deleteNotIn(keys)
    }

    override fun deleteIntentModel(key: IntentContextKey) {
        intents.delete(key)
    }

    override fun listenIntentModelChanges(listener: (String) -> Unit) {
        listen(intents, listener)
    }
}
