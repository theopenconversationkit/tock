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

package ai.tock.bot.api.service

import ai.tock.bot.BotIoc
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.definition.BotProviderId
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.BotRepository.botAPI
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.translator.Translator
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    Translator.enabled = true
    BotIoc.setup()
    val dao: BotApplicationConfigurationDAO = injector.provide()
    dao.getBotConfigurations().forEach {
        logger.info("register configuration ${it.name}")
        BotRepository.registerBotProvider(BotApiDefinitionProvider(it))
    }
    botAPI = true
    BotRepository.installBots(emptyList())
    dao.listenBotChanges {
        logger.info("reload bot configurations")
        dao.getBotConfigurations().forEach {
            val oldProvider: BotApiDefinitionProvider? = BotRepository.getBotProvider(BotProviderId(it.botId, it.namespace, it.name)) as? BotApiDefinitionProvider
            if (oldProvider == null || oldProvider.configuration != it) {
                logger.info("reload bot configuration api: $it")
                BotRepository.registerBotProvider(BotApiDefinitionProvider(it))
            }
        }
        BotRepository.checkBotConfigurations()
    }
}
