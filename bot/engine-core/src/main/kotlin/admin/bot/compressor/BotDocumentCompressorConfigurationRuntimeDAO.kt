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

package ai.tock.bot.admin.bot.compressor

import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import org.litote.kmongo.Id

/**
 * The part of [BotDocumentCompressorConfigurationDAO] that the bots use at runtime.
 *
 * A storage that only serves the bots (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [BotDocumentCompressorConfigurationDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface BotDocumentCompressorConfigurationRuntimeDAO {
    fun listenChanges(listener: () -> Unit)

    fun findByNamespaceAndBotIdAndEnabled(
        namespace: String,
        botId: String,
        enabled: Boolean,
    ): BotDocumentCompressorConfiguration?

    companion object {
        /**
         * The bound [BotDocumentCompressorConfigurationRuntimeDAO], or the bound [BotDocumentCompressorConfigurationDAO] adapted to it.
         */
        fun fromInjector(): BotDocumentCompressorConfigurationRuntimeDAO =
            injector.provideOrDefault<BotDocumentCompressorConfigurationRuntimeDAO> {
                injector.provide<BotDocumentCompressorConfigurationDAO>().asRuntimeDAO()
            }
    }
}

/**
 * Adapts this DAO to [BotDocumentCompressorConfigurationRuntimeDAO].
 */
fun BotDocumentCompressorConfigurationDAO.asRuntimeDAO(): BotDocumentCompressorConfigurationRuntimeDAO = BotDocumentCompressorConfigurationRuntimeDAOAdapter(this)

private class BotDocumentCompressorConfigurationRuntimeDAOAdapter(
    private val dao: BotDocumentCompressorConfigurationDAO,
) : BotDocumentCompressorConfigurationRuntimeDAO {
    override fun listenChanges(listener: () -> Unit) = dao.listenChanges(listener)

    override fun findByNamespaceAndBotIdAndEnabled(
        namespace: String,
        botId: String,
        enabled: Boolean,
    ): BotDocumentCompressorConfiguration? = dao.findByNamespaceAndBotIdAndEnabled(namespace, botId, enabled)
}
