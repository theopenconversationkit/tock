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

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationRuntimeDAO
import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.storage.dynamodb.DynamoStore.configTable
import mu.KotlinLogging
import org.litote.kmongo.Id
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.Delete
import software.amazon.awssdk.services.dynamodb.model.Put
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException

/**
 * Tier 1 (configuration) pattern: little data, read by application.
 *
 * - every [BotApplicationConfiguration] lives in the partition `BAC`, one item per configuration;
 *   every query reads the partition and filters in memory (a few dozen items);
 * - the two Mongo unique indexes (namespace+applicationId+botId, path) become guard items
 *   in the partition `BAC_UQ`, written in the same transaction as the document;
 * - [BotConfiguration] uses its natural key (namespace, botId, name) as sort key, so the Mongo upsert is a put.
 *
 * Change streams are not available: [listenChanges] and [listenBotChanges] listen to the publications of the
 * admin ([DynamoChanges]).
 */
object BotApplicationConfigurationDynamoDAO : BotApplicationConfigurationRuntimeDAO {
    private val logger = KotlinLogging.logger {}

    private const val CONF_PK = "BAC"
    private const val GUARD_PK = "BAC_UQ"
    private const val BOT_PK = "BOT"
    private const val OWNER = "owner"

    override fun listenChanges(listener: () -> Unit) = DynamoChanges.listen(DynamoChanges.BOT_APPLICATION, listener)

    override fun listenBotChanges(listener: () -> Unit) = DynamoChanges.listen(DynamoChanges.BOT_CONFIGURATION, listener)

    private fun configurations(): List<BotApplicationConfiguration> =
        DynamoStore
            .queryPartition(configTable, CONF_PK, consistentRead = true)
            .map { DocumentCodec.decode<BotApplicationConfiguration>(it.bytes(DOC)) }

    private fun guards(conf: BotApplicationConfiguration): List<String> =
        listOfNotNull(
            "app#${conf.namespace}#${conf.applicationId}#${conf.botId}",
            conf.path?.let { "path#$it" },
        )

    override fun save(conf: BotApplicationConfiguration): BotApplicationConfiguration {
        val id = conf._id.toString()
        val old = getConfigurationById(conf._id)
        val newGuards = guards(conf)
        val obsoleteGuards = old?.let { guards(it) - newGuards.toSet() } ?: emptyList()
        val items =
            listOf(
                TransactWriteItem
                    .builder()
                    .put(
                        Put
                            .builder()
                            .tableName(configTable)
                            .item(DynamoStore.key(CONF_PK, id) + (DOC to b(DocumentCodec.encode(conf))))
                            .build(),
                    ).build(),
            ) +
                newGuards.map { guard ->
                    TransactWriteItem
                        .builder()
                        .put(
                            Put
                                .builder()
                                .tableName(configTable)
                                .item(DynamoStore.key(GUARD_PK, guard) + (OWNER to s(id)))
                                .conditionExpression("attribute_not_exists(#pk) OR #owner = :id")
                                .expressionAttributeNames(mapOf("#pk" to PK, "#owner" to OWNER))
                                .expressionAttributeValues(mapOf(":id" to s(id)))
                                .build(),
                        ).build()
                } +
                obsoleteGuards.map { guard ->
                    TransactWriteItem
                        .builder()
                        .delete(
                            Delete
                                .builder()
                                .tableName(configTable)
                                .key(DynamoStore.key(GUARD_PK, guard))
                                .build(),
                        ).build()
                }
        try {
            DynamoStore.client
                .transactWriteItems { it.transactItems(items).returnConsumedCapacity(ReturnConsumedCapacity.TOTAL) }
                .also { DynamoStore.recordWrite(it.consumedCapacity()) }
        } catch (e: TransactionCanceledException) {
            // same behaviour as the Mongo DAO on a unique index violation: log and return the configuration
            logger.error(e) { "unique constraint violated for $conf" }
        }
        return conf
    }

    fun delete(conf: BotApplicationConfiguration) {
        val stored = getConfigurationById(conf._id) ?: return
        DynamoStore.batchWrite(
            configTable,
            deleteKeys =
                listOf(DynamoStore.key(CONF_PK, conf._id.toString())) +
                    guards(stored).map { DynamoStore.key(GUARD_PK, it) },
        )
    }

    fun getConfigurationById(id: Id<BotApplicationConfiguration>): BotApplicationConfiguration? =
        DynamoStore
            .getItem(configTable, CONF_PK, id.toString())
            ?.let { DocumentCodec.decode<BotApplicationConfiguration>(it.bytes(DOC)) }

    fun getConfigurationByApplicationIdAndBotId(
        namespace: String,
        applicationId: String,
        botId: String,
    ): BotApplicationConfiguration? = configurations().firstOrNull { it.namespace == namespace && it.applicationId == applicationId && it.botId == botId }

    fun getConfigurationsByNamespaceAndBotId(
        namespace: String,
        botId: String,
    ): List<BotApplicationConfiguration> = configurations().filter { it.namespace == namespace && it.botId == botId }

    fun getConfigurationByPath(path: String): BotApplicationConfiguration? = configurations().firstOrNull { it.path == path }

    fun getConfigurationByTargetId(id: Id<BotApplicationConfiguration>): BotApplicationConfiguration? = configurations().firstOrNull { it.targetConfigurationId == id }

    fun getConfigurationsByNamespaceAndNlpModel(
        namespace: String,
        nlpModel: String,
    ): List<BotApplicationConfiguration> = configurations().filter { it.namespace == namespace && it.nlpModel == nlpModel }

    fun getConfigurationsByBotNamespaceAndConfigurationName(
        namespace: String,
        botId: String,
        configurationName: String,
    ): List<BotApplicationConfiguration> = configurations().filter { it.namespace == namespace && it.botId == botId && it.name == configurationName }

    override fun getConfigurations(): List<BotApplicationConfiguration> = configurations()

    private fun botKey(
        namespace: String,
        botId: String,
        name: String,
    ): Map<String, AttributeValue> = DynamoStore.key(BOT_PK, "$namespace#$botId#$name")

    private fun bots(): List<BotConfiguration> =
        DynamoStore
            .queryPartition(configTable, BOT_PK, consistentRead = true)
            .map { DocumentCodec.decode<BotConfiguration>(it.bytes(DOC)) }

    override fun save(conf: BotConfiguration) {
        DynamoStore.putItem(configTable, botKey(conf.namespace, conf.botId, conf.name) + (DOC to b(DocumentCodec.encode(conf))))
    }

    fun getBotConfigurationsByNamespaceAndBotId(
        namespace: String,
        botId: String,
    ): List<BotConfiguration> =
        DynamoStore
            .queryPartition(configTable, BOT_PK, skPrefix = "$namespace#$botId#", consistentRead = true)
            .map { DocumentCodec.decode<BotConfiguration>(it.bytes(DOC)) }
            // the prefix also matches a botId that starts with botId followed by '#'
            .filter { it.namespace == namespace && it.botId == botId }

    override fun getBotConfigurationsByNamespaceAndNameAndBotId(
        namespace: String,
        name: String,
        botId: String,
    ): BotConfiguration? =
        botKey(namespace, botId, name).let { key ->
            DynamoStore
                .getItem(configTable, key.getValue(PK).s(), key.getValue(SK).s())
                ?.let { DocumentCodec.decode<BotConfiguration>(it.bytes(DOC)) }
        }

    override fun getBotConfigurations(): List<BotConfiguration> = bots()

    fun delete(conf: BotConfiguration) {
        val key = botKey(conf.namespace, conf.botId, conf.name)
        DynamoStore.deleteItem(configTable, key.getValue(PK).s(), key.getValue(SK).s())
    }
}
