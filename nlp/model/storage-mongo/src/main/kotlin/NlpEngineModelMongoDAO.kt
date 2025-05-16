/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.model.service.storage.mongo

import ai.tock.nlp.model.ClassifierContextKey
import ai.tock.nlp.model.EntityContextKey
import ai.tock.nlp.model.IntentContext.IntentContextKey
import ai.tock.nlp.model.service.storage.NlpEngineModelDAO
import ai.tock.nlp.model.service.storage.NlpModelStream
import ai.tock.nlp.model.service.storage.mongo.MongoModelConfiguration.asyncDatabase
import ai.tock.nlp.model.service.storage.mongo.MongoModelConfiguration.database
import ai.tock.shared.ensureIndex
import ai.tock.shared.watch
import com.mongodb.MongoGridFSException
import com.mongodb.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.gridfs.GridFSDownloadStream
import com.mongodb.client.gridfs.model.GridFSFile
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.ne
import com.mongodb.client.model.changestream.FullDocument
import mu.KotlinLogging
import java.io.InputStream
import java.time.Instant

/**
 *
 */
internal object NlpEngineModelMongoDAO : NlpEngineModelDAO {

    private val logger = KotlinLogging.logger {}

    private val entityBucket: GridFSBucket by lazy {
        GridFSBuckets.create(database, "fs_entity")
            .apply {
                database.getCollection("fs_entity.files").ensureIndex("{filename:1}")
            }
    }
    private val intentBucket: GridFSBucket by lazy {
        GridFSBuckets.create(database, "fs_intent")
            .apply {
                database.getCollection("fs_intent.files").ensureIndex("{filename:1}")
            }
    }

    private val asyncEntityCol = asyncDatabase.getCollection("fs_entity.files")

    private val asyncIntentCol = asyncDatabase.getCollection("fs_intent.files")

    // TODO remove in 3.0
    private fun ClassifierContextKey.idWithoutNamespace(): String? =
        id().run {
            val i = indexOf(":")
            val i2 = indexOf("-")
            if (i != -1 && i2 > i) {
                substring(i + 1)
            } else {
                null
            }
        }

    private fun getGridFSFile(bucket: GridFSBucket, key: ClassifierContextKey): GridFSFile? {
        return try {
            bucket.find(eq("filename", key.id())).limit(1).first()
                ?: key.idWithoutNamespace()?.let { bucket.find(eq("filename", it)).limit(1).first() }
        } catch (e: MongoGridFSException) {
            logger.debug(e) { "no model exists for $key" }
            null
        }
    }

    private fun getDownloadStream(bucket: GridFSBucket, key: ClassifierContextKey): GridFSDownloadStream? {
        return (
            getGridFSFile(bucket, key)
                ?: key.idWithoutNamespace()?.let { getGridFSFile(bucket, key) }
            )
            ?.let {
                bucket.openDownloadStream(it.id)
            }
    }

    private fun saveModel(bucket: GridFSBucket, key: ClassifierContextKey, stream: InputStream) {
        val filename = key.id()
        val newId = bucket.uploadFromStream(filename, stream)
        // remove old versions
        bucket.find(and(ne("_id", newId), eq("filename", filename))).forEach {
            logger.debug { "Remove file ${it.objectId} for $key" }
            bucket.delete(it.objectId)
        }
    }

    private fun deleteModel(bucket: GridFSBucket, key: ClassifierContextKey) {
        bucket.find(eq("filename", key.id()))
            .limit(1)
            .first()
            ?.apply {
                logger.debug { "Remove file $objectId for $key" }
                bucket.delete(objectId)
            }
    }

    private fun deleteModelNotIn(bucket: GridFSBucket, keys: List<ClassifierContextKey>) {
        bucket.find(Filters.not(Filters.`in`("filename", keys.map { it.id() })))
            .forEach {
                logger.debug { "Remove file ${it.objectId} for ${it.filename}" }
                bucket.delete(it.objectId)
            }
    }

    private fun getModelInputStream(bucket: GridFSBucket, key: ClassifierContextKey): NlpModelStream? {
        return getDownloadStream(bucket, key)?.let { stream ->
            val date = stream.gridFSFile.uploadDate.toInstant()
            NlpModelStream(
                stream,
                date,
                NlpApplicationConfigurationMongoDAO.loadLastConfiguration(key.applicationName, key.engineType, date)
            )
        }
    }

    private fun getLastUpdate(bucket: GridFSBucket, key: ClassifierContextKey): Instant? {
        return getGridFSFile(bucket, key)?.uploadDate?.toInstant()
    }

    override fun getEntityModelInputStream(key: EntityContextKey): NlpModelStream? {
        return getModelInputStream(entityBucket, key)
    }

    override fun saveEntityModel(key: EntityContextKey, stream: InputStream) {
        saveModel(entityBucket, key, stream)
    }

    override fun getEntityModelLastUpdate(key: EntityContextKey): Instant? {
        return getLastUpdate(entityBucket, key)
    }

    override fun deleteEntityModelsNotIn(keys: List<EntityContextKey>) {
        deleteModelNotIn(entityBucket, keys)
    }

    override fun deleteEntityModel(key: EntityContextKey) {
        deleteModel(entityBucket, key)
    }

    override fun getIntentModelInputStream(key: IntentContextKey): NlpModelStream? {
        return getModelInputStream(intentBucket, key)
    }

    override fun saveIntentModel(key: IntentContextKey, stream: InputStream) {
        saveModel(intentBucket, key, stream)
    }

    override fun getIntentModelLastUpdate(key: IntentContextKey): Instant? {
        return getLastUpdate(intentBucket, key)
    }

    override fun deleteIntentModelsNotIn(keys: List<IntentContextKey>) {
        deleteModelNotIn(intentBucket, keys)
    }

    override fun deleteIntentModel(key: IntentContextKey) {
        deleteModel(intentBucket, key)
    }

    override fun listenEntityModelChanges(listener: (String) -> Unit) {
        asyncEntityCol.watch(FullDocument.UPDATE_LOOKUP) {
            val id = it.fullDocument?.get("filename") as? String
            if (id != null) {
                listener.invoke(id)
            }
        }
    }

    override fun listenIntentModelChanges(listener: (String) -> Unit) {
        asyncIntentCol.watch(FullDocument.UPDATE_LOOKUP) {
            val id = it.fullDocument?.get("filename") as? String
            if (id != null) {
                listener.invoke(id)
            }
        }
    }
}
