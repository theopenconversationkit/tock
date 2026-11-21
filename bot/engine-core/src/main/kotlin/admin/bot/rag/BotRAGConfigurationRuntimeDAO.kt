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

package ai.tock.bot.admin.bot.rag

import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import org.litote.kmongo.Id

/**
 * The part of [BotRAGConfigurationDAO] that the bots use at runtime.
 *
 * A storage that only serves the bots (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [BotRAGConfigurationDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface BotRAGConfigurationRuntimeDAO {
    fun listenChanges(listener: () -> Unit)

    fun findByNamespaceAndBotIdAndEnabled(
        namespace: String,
        botId: String,
        enabled: Boolean,
    ): BotRAGConfiguration?

    companion object {
        /**
         * The bound [BotRAGConfigurationRuntimeDAO], or the bound [BotRAGConfigurationDAO] adapted to it.
         */
        fun fromInjector(): BotRAGConfigurationRuntimeDAO = injector.provideOrDefault<BotRAGConfigurationRuntimeDAO> { injector.provide<BotRAGConfigurationDAO>().asRuntimeDAO() }
    }
}

/**
 * Adapts this DAO to [BotRAGConfigurationRuntimeDAO].
 */
fun BotRAGConfigurationDAO.asRuntimeDAO(): BotRAGConfigurationRuntimeDAO = BotRAGConfigurationRuntimeDAOAdapter(this)

private class BotRAGConfigurationRuntimeDAOAdapter(
    private val dao: BotRAGConfigurationDAO,
) : BotRAGConfigurationRuntimeDAO {
    override fun listenChanges(listener: () -> Unit) = dao.listenChanges(listener)

    override fun findByNamespaceAndBotIdAndEnabled(
        namespace: String,
        botId: String,
        enabled: Boolean,
    ): BotRAGConfiguration? = dao.findByNamespaceAndBotIdAndEnabled(namespace, botId, enabled)
}
