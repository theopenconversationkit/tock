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

import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.definition.BotProvider
import fr.vsct.tock.shared.vertx.vertx
import io.vertx.ext.web.Router

/**
 *
 */
object BotRepository {

    internal val connectorProviders: MutableSet<ConnectorProvider> = mutableSetOf()
    private val botProviders: MutableSet<BotProvider> = mutableSetOf()

    fun registerConnectorProvider(connectorProvider: ConnectorProvider) {
        connectorProviders.add(connectorProvider)
    }


    fun registerBotProvider(bot: BotProvider) {
        botProviders.add(bot)
    }

    fun installBots(routerHandlers: List<(Router) -> Unit>) {
        val verticle = BotVerticle()

        ConnectorConfigurationRepository.getConfigurations().forEach { conf ->
            connectorProviders.first { it.connectorType == conf.type }
                    .apply {
                        connector(conf)
                                .let { connector ->
                                    botProviders.forEach { botProvider ->
                                        ConnectorController.register(connector, botProvider.bot(), verticle)
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