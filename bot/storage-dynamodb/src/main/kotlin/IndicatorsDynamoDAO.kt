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

import ai.tock.bot.admin.indicators.Indicator
import ai.tock.bot.admin.indicators.IndicatorRuntimeDAO
import ai.tock.bot.admin.indicators.metric.Metric
import ai.tock.bot.admin.indicators.metric.MetricRuntimeDAO
import ai.tock.shared.longProperty
import ai.tock.storage.dynamodb.DynamoStore.configTable
import ai.tock.storage.dynamodb.DynamoStore.runtimeTable
import java.time.Instant

/**
 * The indicators of a bot, in the partition `IND#<namespace>#<botId>`, by name (the Mongo unique index is
 * name + botId).
 */
object IndicatorDynamoDAO : IndicatorRuntimeDAO {
    private fun pk(
        namespace: String,
        botId: String,
    ) = "IND#$namespace#$botId"

    override fun save(indicator: Indicator) {
        DynamoStore.putItem(configTable, DynamoStore.key(pk(indicator.namespace, indicator.botId), indicator.name) + (DOC to b(DocumentCodec.encode(indicator))))
    }

    override fun findByNameAndBotId(
        name: String,
        namespace: String,
        botId: String,
    ): Indicator? =
        DynamoStore
            .getItem(configTable, pk(namespace, botId), name)
            ?.let { DocumentCodec.decode<Indicator>(it.bytes(DOC)) }
            ?.takeIf { it.namespace == namespace && it.botId == botId }

    fun findAllByBotId(
        namespace: String,
        botId: String,
    ): List<Indicator> =
        DynamoStore
            .queryPartition(configTable, pk(namespace, botId), consistentRead = true)
            .map { DocumentCodec.decode<Indicator>(it.bytes(DOC)) }
            .filter { it.namespace == namespace && it.botId == botId }

    fun delete(indicator: Indicator) {
        DynamoStore.deleteItem(configTable, pk(indicator.namespace, indicator.botId), indicator.name)
    }
}

/**
 * The metrics: the runtime only writes them, the analytics of the Studio read them in MongoDB once replicated.
 * They live in the runtime table, `METRIC#<dialogId>` / `<_id>`, for `tock_dynamodb_metric_ttl_days` (7):
 * the time to be replicated.
 */
object MetricDynamoDAO : MetricRuntimeDAO {
    private val ttlSeconds = longProperty("tock_dynamodb_metric_ttl_days", 7) * 24 * 3600

    private fun item(metric: Metric) =
        DynamoStore.key("METRIC#${metric.dialogId}", metric._id.toString()) +
            mapOf(DOC to b(DocumentCodec.encode(metric)), TTL to n(Instant.now().epochSecond + ttlSeconds))

    override fun save(metric: Metric) {
        DynamoStore.putItem(runtimeTable, item(metric))
    }

    override fun saveAll(metrics: List<Metric>) {
        DynamoStore.batchWrite(runtimeTable, puts = metrics.map { item(it) })
    }
}
