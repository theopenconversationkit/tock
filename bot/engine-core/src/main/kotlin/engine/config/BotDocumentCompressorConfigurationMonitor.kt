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

package ai.tock.bot.engine.config

import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfigurationDAO
import ai.tock.bot.engine.Bot
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArraySet

/**
 *
 */
internal object BotDocumentCompressorConfigurationMonitor {
    private val logger = KotlinLogging.logger {}

    private val documentCompressorConfigurationDAO: BotDocumentCompressorConfigurationDAO by injector.instance()
    private val botsToMonitor: MutableSet<Bot> = CopyOnWriteArraySet()

    init {
        logger.info { "start bot document compressor configuration monitor" }
        documentCompressorConfigurationDAO.listenChanges {
            logger.info { "refresh bots document compressor configuration" }
            botsToMonitor.forEach {
                refresh(it)
            }
        }
    }

    fun monitor(bot: Bot) {
        logger.debug { "load document compressor configuration & monitor bot $bot" }
        refresh(bot)
        botsToMonitor.add(bot)
    }

    fun unmonitor(bot: Bot) {
        botsToMonitor.remove(bot)
    }

    private fun refresh(bot: Bot) {
        logger.debug { "Refreshing bot document compressor configuration ${bot.botDefinition.botId} (${bot.configuration.applicationId}-${bot.configuration._id})..." }
        bot.botDefinition.documentCompressorConfiguration =
            documentCompressorConfigurationDAO.findByNamespaceAndBotIdAndEnabled(
                bot.botDefinition.namespace,
                bot.botDefinition.botId,
                enabled = true,
            )
    }
}
