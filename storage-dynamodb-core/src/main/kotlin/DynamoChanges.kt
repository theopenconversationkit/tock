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

package ai.tock.storage.dynamodb

import ai.tock.shared.Executor
import ai.tock.shared.changes.ChangeChecks
import ai.tock.shared.injector
import ai.tock.shared.longProperty
import ai.tock.shared.provide
import mu.KotlinLogging
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The changes published to DynamoDB, in place of the change streams of MongoDB.
 *
 * The publication of the admin increments a counter by channel in the item `VERSION` of the configuration table
 * ([changed]). The runtime reads this item at most every `tock_dynamodb_change_check_interval_ms` (10 s by
 * default), at the start of a request ([ChangeChecks]) and with a periodic task of the [Executor] (a process that
 * is not frozen between its requests, or an external scheduler), and calls the listeners of the channels whose
 * counter has changed ([listen]).
 *
 * The counters known at startup are read before the data is loaded ([ChangeChecks.prepareAll], or the first
 * [listen] in a process that does not prepare the checks): a publication made while the data loads is seen at the
 * next check. If they cannot be read, the first check calls every listener. The periodic check runs only once a
 * listener is registered: the admin, which publishes, does not read.
 */
object DynamoChanges {
    private val logger = KotlinLogging.logger {}

    const val BOT_APPLICATION = "bot_application"
    const val BOT_CONFIGURATION = "bot_configuration"
    const val STORY = "story"
    const val LABEL = "label"
    const val SETTINGS = "settings"
    const val NLP_APPLICATION = "nlp_application"
    const val NLP_INTENT = "nlp_intent"
    const val NLP_ENTITY_TYPE = "nlp_entity_type"
    const val NLP_DICTIONARY = "nlp_dictionary"

    private const val VERSION_PK = "VERSION"
    private const val VERSION_SK = "V"

    /** The attributes of the counters: the channel names cannot be reserved words of DynamoDB. */
    private const val PREFIX = "v_"

    private val interval: Long = longProperty("tock_dynamodb_change_check_interval_ms", 10_000)

    private val listeners = ConcurrentHashMap<String, CopyOnWriteArrayList<() -> Unit>>()

    /** The counters seen by the last check: null until they are read. */
    @Volatile
    private var known: Map<String, Long>? = null

    @Volatile
    private var started = false

    private var lastCheck = 0L

    /**
     * Registers the checks, without reading DynamoDB: called when the DAOs are initialized. The process reads the
     * current counters ([ChangeChecks.prepareAll]) before it loads its data.
     */
    fun register() {
        ChangeChecks.register("DynamoDB", prepare = { start() }, check = { check() })
    }

    /** Reads the current counters, if they are not read yet. */
    fun start() {
        if (started) return
        synchronized(this) {
            if (started) return
            started = true
            known =
                try {
                    read()
                } catch (e: Exception) {
                    logger.warn(e) { "the versions of the DynamoDB configuration cannot be read: they are read at the first check" }
                    null
                }
        }
    }

    /** Calls [listener] when the items of [channel] are published. */
    fun listen(
        channel: String,
        listener: () -> Unit,
    ) {
        // a process that has not prepared the checks: the data is already loaded
        start()
        synchronized(this) {
            // the checks run only in the processes that listen to the changes (the runtime, not the admin)
            if (listeners.isEmpty()) {
                register()
                try {
                    injector.provide<Executor>().setPeriodic("dynamodb_change_check", Duration.ofMillis(interval), Duration.ofMillis(interval)) { check(force = true) }
                } catch (e: Exception) {
                    logger.warn(e) { "no periodic check of the DynamoDB changes: they are checked at the start of the requests" }
                }
            }
            listeners.computeIfAbsent(channel) { CopyOnWriteArrayList() } += listener
        }
    }

    /** Records a publication of the items of [channels]: the runtimes call their listeners at their next check. */
    fun changed(channels: Collection<String>) {
        if (channels.isEmpty()) return
        val names = channels.distinct().withIndex().associate { (i, channel) -> "#c$i" to "$PREFIX$channel" }
        DynamoStore.client
            .updateItem {
                it
                    .tableName(DynamoStore.configTable)
                    .key(DynamoStore.key(VERSION_PK, VERSION_SK))
                    .updateExpression("ADD " + names.keys.joinToString { name -> "$name :one" })
                    .expressionAttributeNames(names)
                    .expressionAttributeValues(mapOf(":one" to n(1)))
                    .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
            }.also { DynamoStore.recordWrite(it.consumedCapacity()) }
    }

    /**
     * Calls the listeners of the channels whose counter has changed since the last check, at most once by
     * interval, unless [force].
     */
    fun check(force: Boolean = false) {
        synchronized(this) {
            val now = System.currentTimeMillis()
            if (!force && now - lastCheck < interval) return
            lastCheck = now
        }
        val previous = known
        val current = read()
        known = current
        val changed = current.keys.filter { previous == null || previous[it] != current[it] }
        changed.forEach { channel ->
            logger.info { "the $channel items have been published to DynamoDB" }
            listeners[channel]?.forEach { listener ->
                try {
                    listener()
                } catch (e: Exception) {
                    logger.error(e) { "the listener of the $channel changes failed" }
                }
            }
        }
    }

    private fun read(): Map<String, Long> =
        DynamoStore
            .getItem(DynamoStore.configTable, VERSION_PK, VERSION_SK)
            ?.filterKeys { it.startsWith(PREFIX) }
            ?.map { (name, value) -> name.removePrefix(PREFIX) to value.n().toLong() }
            ?.toMap()
            ?: emptyMap()

    /** Forgets the listeners and the counters (tests). */
    fun reset() {
        synchronized(this) {
            listeners.clear()
            known = null
            started = false
            lastCheck = 0
            ChangeChecks.unregister("DynamoDB")
        }
    }
}
