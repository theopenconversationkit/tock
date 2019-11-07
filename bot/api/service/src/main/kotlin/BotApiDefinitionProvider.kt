/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.bot.api.service

import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.api.model.configuration.ClientConfiguration
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.BotProvider
import ai.tock.bot.definition.BotProviderId
import ai.tock.bot.engine.BotRepository
import mu.KotlinLogging

internal class BotApiDefinitionProvider(private val configuration: BotConfiguration) : BotProvider {

    private val logger = KotlinLogging.logger {}

    @Volatile
    private var lastConfiguration: ClientConfiguration? = null
    @Volatile
    private var bot: BotDefinition
    private val handler: BotApiHandler = BotApiHandler(this, configuration)

    init {
        lastConfiguration = handler.configuration()
        bot = BotApiDefinition(configuration, lastConfiguration, handler)
    }

    fun updateIfConfigurationChange(conf: ClientConfiguration) {
        logger.debug { "check conf $conf" }
        if (conf != lastConfiguration) {
            this.lastConfiguration = conf
            bot = BotApiDefinition(configuration, conf, handler)
            configurationUpdated = true
            BotRepository.registerBuiltInStoryDefinitions(this)
            BotRepository.checkBotConfigurations()
        }
    }

    override fun botDefinition(): BotDefinition = bot

    override fun equals(other: Any?): Boolean = botProviderId == (other as? BotProvider)?.botProviderId

    override fun hashCode(): Int = botProviderId.hashCode()

    override val botProviderId: BotProviderId =
        BotProviderId(configuration.botId, configuration.namespace, configuration.name)

    @Volatile
    override var configurationUpdated: Boolean = true
}