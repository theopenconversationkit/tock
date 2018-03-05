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
import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.BotProvider
import fr.vsct.tock.bot.definition.StoryHandlerListener
import fr.vsct.tock.bot.engine.config.BotConfigurationSynchronizer
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.monitoring.RequestTimer
import fr.vsct.tock.bot.engine.nlp.BuiltInKeywordListener
import fr.vsct.tock.bot.engine.nlp.NlpListener
import fr.vsct.tock.nlp.api.client.NlpClient
import fr.vsct.tock.shared.DEFAULT_APP_NAMESPACE
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.tockAppDefaultNamespace
import fr.vsct.tock.shared.vertx.vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * Advanced bot configuration.
 *
 * [fr.vsct.tock.bot.registerAndInstallBot] method is the preferred way to start a bot in most use cases.
 */
object BotRepository {

    private val botConfigurationDAO: BotApplicationConfigurationDAO by injector.instance()

    internal val connectorProviders: MutableSet<ConnectorProvider> = mutableSetOf(
            object : ConnectorProvider {
                override val connectorType: ConnectorType = ConnectorType.none
                override fun connector(connectorConfiguration: ConnectorConfiguration): Connector
                        = object : ConnectorBase(ConnectorType.none) {
                    override fun register(controller: ConnectorController) = Unit

                    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) = Unit
                }
            }
    )
    private val botProviders: MutableSet<BotProvider> = mutableSetOf()
    internal val storyHandlerListeners: MutableList<StoryHandlerListener> = mutableListOf()
    internal val nlpListeners: MutableList<NlpListener> = mutableListOf(BuiltInKeywordListener)
    private val nlpClient: NlpClient by injector.instance()
    private val executor: Executor by injector.instance()

    /**
     * Request timer for connectors.
     */
    @Volatile
    var requestTimer: RequestTimer = object : RequestTimer {}

    /**
     * healthcheck handler to answer to GET /healthcheck.
     */
    @Volatile
    var healthcheckHandler: (RoutingContext) -> Unit = {
        executor.executeBlocking {
            it.response().setStatusCode(if (nlpClient.healthcheck()) 200 else 500).end()
        }
    }

    /**
     * Registers a new [ConnectorProvider].
     */
    fun registerConnectorProvider(connectorProvider: ConnectorProvider) {
        connectorProviders.add(connectorProvider)
    }

    /**
     * Registers a new [BotProvider].
     */
    fun registerBotProvider(bot: BotProvider) {
        botProviders.add(bot)
    }

    /**
     * Registers a new [StoryHandlerListener].
     */
    fun registerStoryHandlerListener(listener: StoryHandlerListener) {
        storyHandlerListeners.add(listener)
    }

    /**
     * Registers an new [NlpListener].
     */
    fun registerNlpListener(listener: NlpListener) {
        nlpListeners.add(listener)
    }

    /**
     * Installs the bot(s).
     *
     * @param routerHandlers the additional router handlers
     * @param adminRestConnectorInstaller the (optional) linked [fr.vsct.tock.bot.connector.rest.RestConnector] installer.
     */
    fun installBots(
            routerHandlers: List<(Router) -> Unit>,
            adminRestConnectorInstaller: (BotApplicationConfiguration) -> ConnectorConfiguration? = { null }
    ) {
        val verticle = BotVerticle()

        fun saveApplicationConfigurationAndRegister(
                connector: Connector,
                bot: Bot,
                configuration: ConnectorConfiguration): BotApplicationConfiguration {
            return with(bot.botDefinition) {
                val conf = BotApplicationConfiguration(
                        configuration.applicationId.run { if (isBlank()) botId else this },
                        botId,
                        namespace,
                        nlpModelName,
                        configuration.type,
                        configuration.ownerConnectorType,
                        configuration.getName().run { if (isBlank()) botId else this },
                        configuration.getBaseUrl(),
                        configuration.parametersWithoutDefaultKeys())

                TockConnectorController.register(connector, bot, verticle)

                botConfigurationDAO.updateIfNotManuallyModified(conf)
            }
        }

        fun findConnectorProvider(connectorType: ConnectorType): ConnectorProvider {
            return connectorProviders.first { it.connectorType == connectorType }
        }

        val connectorConfigurations = ConnectorConfigurationRepository.getConfigurations()
                .run {
                    if (isEmpty()) {
                        listOf(
                                ConnectorConfiguration(
                                        "",
                                        "",
                                        ConnectorType.none,
                                        ConnectorType.none)
                        )
                    } else {
                        this
                    }
                }

        connectorConfigurations.forEach { conf ->
            findConnectorProvider(conf.type)
                    .apply {
                        connector(conf)
                                .let { connector ->
                                    botProviders.forEach { botProvider ->
                                        Bot(botProvider.botDefinition()).let { bot ->
                                            //set default namespace to bot namespace if not already set
                                            if (tockAppDefaultNamespace == DEFAULT_APP_NAMESPACE) {
                                                tockAppDefaultNamespace = bot.botDefinition.namespace
                                            }
                                            val appConf = saveApplicationConfigurationAndRegister(connector, bot, conf)
                                            BotConfigurationSynchronizer.monitor(bot)

                                            //init rest built-in configuration if we need it
                                            adminRestConnectorInstaller.invoke(appConf)
                                                    ?.apply {
                                                        saveApplicationConfigurationAndRegister(
                                                                findConnectorProvider(type).connector(this),
                                                                bot,
                                                                this
                                                        )
                                                    }
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