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

import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfiguration
import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfigurationRuntimeDAO
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfiguration
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfigurationRuntimeDAO
import ai.tock.bot.admin.bot.observability.BotObservabilityConfiguration
import ai.tock.bot.admin.bot.observability.BotObservabilityConfigurationRuntimeDAO
import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.admin.bot.rag.BotRAGConfigurationRuntimeDAO
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfiguration
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfigurationRuntimeDAO
import ai.tock.storage.dynamodb.DynamoStore.configTable
import mu.KotlinLogging

/**
 * A configuration of a bot that MongoDB keeps unique by (namespace, botId): one item,
 * `BOTCONF#<kind>#<namespace>#<botId>` / `C`, read at each turn that needs it.
 */
class BotSettingsDynamoStore<T : Any>(
    private val kind: String,
    private val type: Class<T>,
    private val namespaceOf: (T) -> String,
    private val botIdOf: (T) -> String,
) {
    private val logger = KotlinLogging.logger {}

    private fun pk(
        namespace: String,
        botId: String,
    ) = "BOTCONF#$kind#$namespace#$botId"

    fun listenChanges() {
        logger.info { "no change stream on DynamoDB: the $kind configuration changes are not pushed" }
    }

    fun find(
        namespace: String,
        botId: String,
    ): T? =
        DynamoStore
            .getItem(configTable, pk(namespace, botId), "C")
            ?.let { DocumentCodec.decode(it.bytes(DOC), type) }
            ?.takeIf { namespaceOf(it) == namespace && botIdOf(it) == botId }

    /** Stores [conf], as published by the admin: it replaces the configuration of its bot. */
    fun save(conf: T): T {
        DynamoStore.putItem(configTable, DynamoStore.key(pk(namespaceOf(conf), botIdOf(conf)), "C") + (DOC to b(DocumentCodec.encode(conf))))
        return conf
    }

    fun delete(
        namespace: String,
        botId: String,
    ) {
        DynamoStore.deleteItem(configTable, pk(namespace, botId), "C")
    }
}

object BotRAGConfigurationDynamoDAO : BotRAGConfigurationRuntimeDAO {
    val store = BotSettingsDynamoStore("rag", BotRAGConfiguration::class.java, { it.namespace }, { it.botId })

    override fun listenChanges(listener: () -> Unit) = store.listenChanges()

    override fun findByNamespaceAndBotIdAndEnabled(
        namespace: String,
        botId: String,
        enabled: Boolean,
    ): BotRAGConfiguration? = store.find(namespace, botId)?.takeIf { it.enabled == enabled }
}

object BotVectorStoreConfigurationDynamoDAO : BotVectorStoreConfigurationRuntimeDAO {
    val store = BotSettingsDynamoStore("vectorstore", BotVectorStoreConfiguration::class.java, { it.namespace }, { it.botId })

    override fun listenChanges(listener: () -> Unit) = store.listenChanges()

    override fun findByNamespaceAndBotIdAndEnabled(
        namespace: String,
        botId: String,
        enabled: Boolean,
    ): BotVectorStoreConfiguration? = store.find(namespace, botId)?.takeIf { it.enabled == enabled }
}

object BotObservabilityConfigurationDynamoDAO : BotObservabilityConfigurationRuntimeDAO {
    val store = BotSettingsDynamoStore("observability", BotObservabilityConfiguration::class.java, { it.namespace }, { it.botId })

    override fun listenChanges(listener: () -> Unit) = store.listenChanges()

    override fun findByNamespaceAndBotIdAndEnabled(
        namespace: String,
        botId: String,
        enabled: Boolean,
    ): BotObservabilityConfiguration? = store.find(namespace, botId)?.takeIf { it.enabled == enabled }
}

object BotDocumentCompressorConfigurationDynamoDAO : BotDocumentCompressorConfigurationRuntimeDAO {
    val store = BotSettingsDynamoStore("compressor", BotDocumentCompressorConfiguration::class.java, { it.namespace }, { it.botId })

    override fun listenChanges(listener: () -> Unit) = store.listenChanges()

    override fun findByNamespaceAndBotIdAndEnabled(
        namespace: String,
        botId: String,
        enabled: Boolean,
    ): BotDocumentCompressorConfiguration? = store.find(namespace, botId)?.takeIf { it.enabled == enabled }
}

object BotBusinessRulesConfigurationDynamoDAO : BotBusinessRulesConfigurationRuntimeDAO {
    val store = BotSettingsDynamoStore("businessrules", BotBusinessRulesConfiguration::class.java, { it.namespace }, { it.botId })

    override fun listenChanges(listener: () -> Unit) = store.listenChanges()

    override fun findByNamespaceAndBotId(
        namespace: String,
        botId: String,
    ): BotBusinessRulesConfiguration? = store.find(namespace, botId)
}
