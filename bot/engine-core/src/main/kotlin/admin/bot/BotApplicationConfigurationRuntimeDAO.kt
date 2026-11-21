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

package ai.tock.bot.admin.bot

import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import org.litote.kmongo.Id

/**
 * The part of [BotApplicationConfigurationDAO] that the bots use at runtime.
 *
 * A storage that only serves the bots (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [BotApplicationConfigurationDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface BotApplicationConfigurationRuntimeDAO {
    fun listenChanges(listener: () -> Unit)

    fun save(conf: BotApplicationConfiguration): BotApplicationConfiguration

    fun getConfigurations(): List<BotApplicationConfiguration>

    fun listenBotChanges(listener: () -> Unit)

    fun save(conf: BotConfiguration)

    fun getBotConfigurationsByNamespaceAndNameAndBotId(
        namespace: String,
        name: String,
        botId: String,
    ): BotConfiguration?

    fun getBotConfigurations(): List<BotConfiguration>

    companion object {
        /**
         * The bound [BotApplicationConfigurationRuntimeDAO], or the bound [BotApplicationConfigurationDAO] adapted to it.
         */
        fun fromInjector(): BotApplicationConfigurationRuntimeDAO =
            injector.provideOrDefault<BotApplicationConfigurationRuntimeDAO> { injector.provide<BotApplicationConfigurationDAO>().asRuntimeDAO() }
    }
}

/**
 * Adapts this DAO to [BotApplicationConfigurationRuntimeDAO].
 */
fun BotApplicationConfigurationDAO.asRuntimeDAO(): BotApplicationConfigurationRuntimeDAO = BotApplicationConfigurationRuntimeDAOAdapter(this)

private class BotApplicationConfigurationRuntimeDAOAdapter(
    private val dao: BotApplicationConfigurationDAO,
) : BotApplicationConfigurationRuntimeDAO {
    override fun listenChanges(listener: () -> Unit) = dao.listenChanges(listener)

    override fun save(conf: BotApplicationConfiguration): BotApplicationConfiguration = dao.save(conf)

    override fun getConfigurations(): List<BotApplicationConfiguration> = dao.getConfigurations()

    override fun listenBotChanges(listener: () -> Unit) = dao.listenBotChanges(listener)

    override fun save(conf: BotConfiguration) = dao.save(conf)

    override fun getBotConfigurationsByNamespaceAndNameAndBotId(
        namespace: String,
        name: String,
        botId: String,
    ): BotConfiguration? = dao.getBotConfigurationsByNamespaceAndNameAndBotId(namespace, name, botId)

    override fun getBotConfigurations(): List<BotConfiguration> = dao.getBotConfigurations()
}
