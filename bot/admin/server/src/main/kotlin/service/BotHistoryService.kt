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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.dashboard.BotDashboardDAO
import ai.tock.bot.admin.dashboard.BotHistoryEvent
import ai.tock.bot.admin.dashboard.BotHistorySnapshot
import ai.tock.shared.injector
import ai.tock.shared.provide
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging

/** Informational timeline: a failed history write must not fail an already successful save. */
object BotHistoryService {
    private val dao: BotDashboardDAO get() = injector.provide()
    private val logger = KotlinLogging.logger {}
    private val mapper = jacksonObjectMapper()
    private val secrets = setOf("apiKey", "password", "secretKey")

    internal fun sanitize(value: Any?): Any? =
        when (value) {
            is Map<*, *> -> value.entries.filterNot { it.key in secrets }.associate { it.key.toString() to sanitize(it.value) }
            is Iterable<*> -> value.map { sanitize(it) }
            else -> value
        }

    /** Use persisted entities, avoiding DTOs which resolve credentials from the secret manager. */
    internal fun snapshot(entity: Any?): Map<String, Any?>? {
        if (entity == null) return null
        @Suppress("UNCHECKED_CAST")
        return (sanitize(mapper.convertValue(entity, Map::class.java)) as Map<String, Any?>)
            .filterKeys { it !in setOf("_id", "id", "namespace", "botId") }
    }

    fun <T> configuration(
        namespace: String,
        botId: String,
        type: String,
        author: String?,
        previous: () -> Any?,
        save: () -> T,
    ): T {
        if (author == null) return save()
        val before = runCatching { snapshot(previous()) }
        val saved = save()
        safely(namespace, botId, type) {
            val current = requireNotNull(snapshot(saved))
            // Also avoids an artificial first event when an existing bot is saved unchanged.
            if (before.getOrThrow() != current) {
                val previousSnapshot = dao.latest(namespace, botId, type)?.snapshot?.current
                if (previousSnapshot != current) {
                    dao.append(BotHistoryEvent(namespace, botId, type, author, snapshot = BotHistorySnapshot(previousSnapshot, current)))
                }
            }
        }
        return saved
    }

    fun record(
        namespace: String,
        botId: String,
        type: String,
        author: String,
        params: Map<String, Any?> = emptyMap(),
    ) {
        safely(namespace, botId, type) { dao.append(BotHistoryEvent(namespace, botId, type, author, params)) }
    }

    private fun safely(
        namespace: String,
        botId: String,
        type: String,
        action: () -> Unit,
    ) {
        try {
            action()
        } catch (_: Exception) {
            // Driver/serialization exceptions can contain secrets from the source document.
            logger.error { "Dashboard history write failed: namespace=$namespace botId=$botId type=$type" }
        }
    }
}
