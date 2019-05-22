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

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.BotAnswerInterceptor
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.BotProvider
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.StoryHandlerDefinition
import fr.vsct.tock.bot.definition.StoryHandlerListener
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.config.StoryConfigurationMonitor
import fr.vsct.tock.bot.engine.monitoring.RequestTimer
import fr.vsct.tock.bot.engine.nlp.BuiltInKeywordListener
import fr.vsct.tock.bot.engine.nlp.NlpController
import fr.vsct.tock.bot.engine.nlp.NlpListener
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.nlp.api.client.NlpClient
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.provide
import fr.vsct.tock.shared.vertx.vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.ServiceLoader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Advanced bot configuration.
 *
 * [fr.vsct.tock.bot.registerAndInstallBot] method is the preferred way to start a bot in most use cases.
 */
object BotRepository {

    private val logger = KotlinLogging.logger {}

    private val botConfigurationDAO: BotApplicationConfigurationDAO get() = injector.provide()
    internal val botProviders: MutableSet<BotProvider> = mutableSetOf()
    internal val storyHandlerListeners: MutableList<StoryHandlerListener> = mutableListOf()
    private val nlpListeners: MutableList<NlpListener> = mutableListOf(BuiltInKeywordListener)
    private val nlpClient: NlpClient get() = injector.provide()
    private val nlpController: NlpController get() = injector.provide()
    private val executor: Executor get() = injector.provide()
    internal val botAnswerInterceptors: MutableList<BotAnswerInterceptor> = mutableListOf()

    internal val connectorProviders: MutableSet<ConnectorProvider> = CopyOnWriteArraySet(
        ServiceLoader.load(ConnectorProvider::class.java).map { it }.apply {
            forEach {
                logger.info { "Connector ${it.connectorType} loaded" }
            }
        }
    )

    private val connectorControllerMap: MutableMap<BotApplicationConfiguration, ConnectorController> =
        ConcurrentHashMap()

    @Volatile
    private var botsInstalled: Boolean = false

    /**
     * Request timer for connectors.
     */
    @Volatile
    var requestTimer: RequestTimer = object : RequestTimer {}

    @Volatile
    private var realHealthCheckHandler: (RoutingContext) -> Unit = {
        if (!botsInstalled) {
            it.response().setStatusCode(500).end()
        } else {
            executor.executeBlocking {
                it.response().setStatusCode(if (nlpClient.healthcheck()) 200 else 500).end()
            }
        }
    }

    /**
     * healthcheck handler to answer to GET /healthcheck.
     */
    var healthcheckHandler: (RoutingContext) -> Unit
        get() = realHealthCheckHandler
        set(h) {
            realHealthCheckHandler = {
                if (!botsInstalled) {
                    it.response().setStatusCode(500).end()
                } else {
                    try {
                        h.invoke(it)
                    } catch (t: Throwable) {
                        logger.error(t)
                        try {
                            it.response().setStatusCode(500).end()
                        } catch (t: Throwable) {
                            logger.error(t)
                        }
                    }
                }
            }
        }

    private val verticle by lazy { BotVerticle() }

    /**
     * Calls the specified [action] for each registered [NlpListener].
     */
    fun forEachNlpListener(action: (NlpListener) -> Unit) {
        nlpListeners.forEach(action)
    }

    /**
     * Sends a notification to the connector.
     * A [BotBus] is created and the corresponding story is called.
     *
     * @param applicationId the configuration connector id
     * @param recipientId the recipient identifier
     * @param intent the notification intent
     * @param step the optional step target
     * @param parameters the optional parameters
     */
    fun notify(
        applicationId: String,
        recipientId: PlayerId,
        intent: IntentAware,
        step: StoryStep<out StoryHandlerDefinition>? = null,
        parameters: Map<String, String> = emptyMap()
    ) {
        val conf = connectorControllerMap.keys.firstOrNull { it.applicationId == applicationId }
            ?: error("unknown application $applicationId")
        connectorControllerMap.getValue(conf).notify(recipientId, intent, step, parameters)
    }


    /**
     * Registers a new [ConnectorProvider].
     */
    internal fun registerConnectorProvider(connectorProvider: ConnectorProvider) {
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
     * Registers a new [BotAnswerInterceptor].
     */
    fun registerBotAnswerInterceptor(botAnswerInterceptor: BotAnswerInterceptor) {
        botAnswerInterceptors.add(botAnswerInterceptor)
    }

    /**
     * Registers an new [NlpListener].
     */
    fun registerNlpListener(listener: NlpListener) {
        nlpListeners.add(listener)
    }

    /**
     * Returns the current [ConnectorController] for a given predicate.
     */
    fun getController(predicate: (BotApplicationConfiguration) -> Boolean): ConnectorController? =
        connectorControllerMap
            .keys
            .firstOrNull(predicate)
            ?.let {
                connectorControllerMap[it]
            }

    /**
     * Installs the bot(s).
     *
     * @param routerHandlers the additional router handlers
     */
    fun installBots(routerHandlers: List<(Router) -> Unit>) {
        val bots = botProviders.map { it.botDefinition() }

        //check that nlp applications exist
        bots.distinctBy { it.namespace to it.nlpModelName }
            .forEach { botDefinition ->
                try {
                    nlpClient.createApplication(
                        botDefinition.namespace,
                        botDefinition.nlpModelName,
                        defaultLocale
                    )?.apply {
                        logger.info { "nlp application initialized $namespace $name with locale $supportedLocales" }
                    }
                } catch (e: Exception) {
                    logger.error(e)
                }
            }

        //load configurations
        try {
            checkBotConfigurations(true)
        } catch (e: Exception) {
            logger.error(e)
        }

        //register services
        routerHandlers.forEachIndexed { index, handler ->
            verticle.registerServices("_handler_$index", handler)
        }

        //deploy verticle
        if (botsInstalled) {
            logger.warn { "bot already installed - try to configure new confs" }
            verticle.configure()
        } else {
            vertx.deployVerticle(verticle) {
                if (it.succeeded()) {
                    logger.info { "Bots installed" }
                    botsInstalled = true
                    //listen future changes
                    botConfigurationDAO.listenChanges { executor.executeBlocking { checkBotConfigurations() } }
                } else {
                    logger.error("Bots installation failure", it.cause() ?: IllegalArgumentException())
                }
            }
        }

    }

    /**
     * Returns the [ConnectorProvider] for the specified [ConnectorType].
     */
    fun findConnectorProvider(connectorType: ConnectorType): ConnectorProvider? {
        return connectorProviders.firstOrNull { it.connectorType == connectorType }
    }

    @Synchronized
    private fun checkBotConfigurations(startup: Boolean = false) {
        logger.trace { "check configurations" }
        //clone conf list as we may update connectorControllerMap
        val existingConfs = ArrayList(connectorControllerMap.keys)
        val confs = botConfigurationDAO.getConfigurations()

        confs.forEach { c ->
            if (existingConfs.none { c.equalsWithoutId(it) }) {
                val botDefinition =
                    botProviders.find { it.botId() == c.botId }?.botDefinition()
                if (botDefinition?.namespace == c.namespace) {
                    logger.debug { "refresh configuration $c" }
                    val oldConfiguration = existingConfs.find { it._id == c._id }
                    val connector = findConnectorProvider(c.connectorType)?.connector(ConnectorConfiguration(c))
                    if (connector != null) {
                        createBot(botDefinition, connector, c)

                        if (oldConfiguration != null) {
                            removeBot(oldConfiguration)
                        }
                    } else {
                        logger.warn { "unknown connector ${c.connectorType}" }
                    }
                } else {
                    logger.trace { "not valid namespace for bot ${c.botId} - installation skipped" }
                }
            }
        }

        //remove old confs
        connectorControllerMap.keys.forEach { conf ->
            if (confs.none { it._id == conf._id }) {
                removeBot(conf)
            }
        }
        if (!startup) {
            //register new confs
            verticle.configure()
        }
    }

    private fun createBot(
        botDefinition: BotDefinition,
        connector: Connector,
        conf: BotApplicationConfiguration
    ): BotApplicationConfiguration {

        val app = try {
            nlpController.waitAvailability()
            nlpClient.getApplicationByNamespaceAndName(botDefinition.namespace, botDefinition.nlpModelName)
        } catch (e: Exception) {
            logger.error(e)
            null
        }
        if (app == null) {
            logger.warn { "model ${botDefinition.namespace}:${botDefinition.nlpModelName} not found" }
        }
        val bot = Bot(botDefinition, conf, app?.supportedLocales ?: emptySet())
        return botConfigurationDAO.save(conf)
            .apply {
                val controller = TockConnectorController.register(connector, bot, verticle, conf)
                //monitor bot
                StoryConfigurationMonitor.monitor(bot)
                //register connector controller map
                connectorControllerMap[this] = controller
            }
    }

    private fun removeBot(conf: BotApplicationConfiguration) {
        logger.debug { "uninstall $conf" }
        val controller = connectorControllerMap.remove(conf)
        if (controller != null) {
            logger.debug { "unregister $conf" }
            controller.unregisterServices()
            if (controller is TockConnectorController) {
                StoryConfigurationMonitor.unmonitor(controller.bot)
                TockConnectorController.unregister(controller)
            }
        }
    }

}