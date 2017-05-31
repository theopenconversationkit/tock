/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.engine

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.definition.BotProvider
import fr.vsct.tock.bot.definition.StoryHandlerListener
import fr.vsct.tock.bot.engine.monitoring.RequestTimer
import fr.vsct.tock.bot.engine.nlp.NlpListener
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.vertx.vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 *
 */
object BotRepository {

    private val botConfigurationDAO: BotApplicationConfigurationDAO by injector.instance()

    internal val connectorProviders: MutableSet<ConnectorProvider> = mutableSetOf()
    private val botProviders: MutableSet<BotProvider> = mutableSetOf()
    internal val storyHandlerListeners: MutableList<StoryHandlerListener> = mutableListOf()
    internal val nlpListeners: MutableList<NlpListener> = mutableListOf()

    /**
     * Request timer for connectors.
     */
    var requestTimer: RequestTimer = object : RequestTimer {}

    /**
     * healthcheck handler to answer to GET /healthcheck.
     */
    var healthcheckHandler: (RoutingContext) -> Unit = { it.response().end() }

    fun registerConnectorProvider(connectorProvider: ConnectorProvider) {
        connectorProviders.add(connectorProvider)
    }

    fun registerBotProvider(bot: BotProvider) {
        botProviders.add(bot)
    }

    fun registerStoryHandlerListener(listener: StoryHandlerListener) {
        storyHandlerListeners.add(listener)
    }

    fun registerNlpListener(listener: NlpListener) {
        nlpListeners.add(listener)
    }

    fun installBots(routerHandlers: List<(Router) -> Unit>) {
        val verticle = BotVerticle()

        ConnectorConfigurationRepository.getConfigurations().forEach { conf ->
            connectorProviders.first { it.connectorType == conf.type }
                    .apply {
                        connector(conf)
                                .let { connector ->
                                    botProviders.forEach { botProvider ->
                                        botProvider.bot().let { bot ->
                                            //register bot configuration
                                            with(bot.botDefinition) {
                                                botConfigurationDAO.save(
                                                        BotApplicationConfiguration(
                                                                conf.applicationId,
                                                                botId,
                                                                namespace,
                                                                nlpModelName,
                                                                connector.connectorType)
                                                )
                                            }

                                            //register connector
                                            ConnectorController.register(connector, bot, verticle)
                                        }

                                    }
                                }
                    }
        }

        routerHandlers.forEachIndexed { index, handler ->
            verticle.registerServices("_handler_$index", handler)
        }

        vertx.deployVerticle(verticle)
    }
}