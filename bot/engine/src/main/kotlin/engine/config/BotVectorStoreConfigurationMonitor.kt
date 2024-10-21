/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfigurationDAO
import ai.tock.bot.engine.Bot
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.provide
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArraySet

/**
 *
 */
internal object BotVectorStoreConfigurationMonitor {

    private val logger = KotlinLogging.logger {}

    private val vectorStoreConfigurationDAO: BotVectorStoreConfigurationDAO get() = injector.provide()
    private val botsToMonitor: MutableSet<Bot> = CopyOnWriteArraySet()

    init {
        try {
            logger.info { "start bot vector store configuration monitor" }
            vectorStoreConfigurationDAO.listenChanges {
                logger.info { "refresh bots vector store configuration" }
                botsToMonitor.forEach {
                    refresh(it)
                }
            }
        } catch (t: Throwable) {
            logger.error(t)
        }
    }

    fun monitor(bot: Bot) {
        logger.debug { "load vector store configuration & monitor bot $bot" }
        refresh(bot)
        botsToMonitor.add(bot)
    }

    fun unmonitor(bot: Bot) {
        botsToMonitor.remove(bot)
    }

    private fun refresh(bot: Bot) {
        logger.debug { "Refreshing bot vector store configuration ${bot.botDefinition.botId} (${bot.configuration.applicationId}-${bot.configuration._id})..." }
        bot.botDefinition.vectorStoreConfiguration = vectorStoreConfigurationDAO.findByNamespaceAndBotIdAndEnabled(
            bot.botDefinition.namespace,
            bot.botDefinition.botId,
            enabled = true
        )
    }
}
