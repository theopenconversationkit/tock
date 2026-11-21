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

package ai.tock.storage.dynamodb.publication

import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfiguration
import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfigurationDAO
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfiguration
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfigurationDAO
import ai.tock.bot.admin.bot.observability.BotObservabilityConfiguration
import ai.tock.bot.admin.bot.observability.BotObservabilityConfigurationDAO
import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.admin.bot.rag.BotRAGConfigurationDAO
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfiguration
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfigurationDAO
import ai.tock.storage.dynamodb.BotBusinessRulesConfigurationDynamoDAO
import ai.tock.storage.dynamodb.BotDocumentCompressorConfigurationDynamoDAO
import ai.tock.storage.dynamodb.BotObservabilityConfigurationDynamoDAO
import ai.tock.storage.dynamodb.BotRAGConfigurationDynamoDAO
import ai.tock.storage.dynamodb.BotSettingsDynamoStore
import ai.tock.storage.dynamodb.BotVectorStoreConfigurationDynamoDAO

/** A configuration of a bot, unique by (namespace, botId): read in the admin, published to [store]. */
class BotSettingsSource<T : Any>(
    val kind: String,
    private val find: (namespace: String, botId: String) -> T?,
    private val listen: (() -> Unit) -> Unit,
    private val store: BotSettingsDynamoStore<T>,
) {
    fun admin(
        namespace: String,
        botId: String,
    ): T? = find(namespace, botId)

    fun runtime(
        namespace: String,
        botId: String,
    ): T? = store.find(namespace, botId)

    fun listenChanges(listener: () -> Unit) = listen(listener)

    @Suppress("UNCHECKED_CAST")
    fun publish(settings: Any) {
        store.save(settings as T)
    }

    fun delete(
        namespace: String,
        botId: String,
    ) = store.delete(namespace, botId)
}

/** The configurations of the bots that the admin publishes: the ones whose DAO is bound. */
class BotSettingsSources(
    rag: BotRAGConfigurationDAO? = null,
    vectorStore: BotVectorStoreConfigurationDAO? = null,
    observability: BotObservabilityConfigurationDAO? = null,
    compressor: BotDocumentCompressorConfigurationDAO? = null,
    businessRules: BotBusinessRulesConfigurationDAO? = null,
) {
    val all: List<BotSettingsSource<*>> =
        listOfNotNull(
            rag?.let { BotSettingsSource<BotRAGConfiguration>("rag", it::findByNamespaceAndBotId, it::listenChanges, BotRAGConfigurationDynamoDAO.store) },
            vectorStore?.let {
                BotSettingsSource<BotVectorStoreConfiguration>("vectorstore", it::findByNamespaceAndBotId, it::listenChanges, BotVectorStoreConfigurationDynamoDAO.store)
            },
            observability?.let {
                BotSettingsSource<BotObservabilityConfiguration>("observability", it::findByNamespaceAndBotId, it::listenChanges, BotObservabilityConfigurationDynamoDAO.store)
            },
            compressor?.let {
                BotSettingsSource<BotDocumentCompressorConfiguration>("compressor", it::findByNamespaceAndBotId, it::listenChanges, BotDocumentCompressorConfigurationDynamoDAO.store)
            },
            businessRules?.let {
                BotSettingsSource<BotBusinessRulesConfiguration>(
                    "businessrules",
                    it::findByNamespaceAndBotId,
                    it::listenChanges,
                    BotBusinessRulesConfigurationDynamoDAO.store,
                )
            },
        )
}
