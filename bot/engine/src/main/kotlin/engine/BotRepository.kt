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

package ai.tock.bot.engine

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.NotifyBotStateModifier
import ai.tock.bot.definition.BotAnswerInterceptor
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.BotProvider
import ai.tock.bot.definition.BotProviderId
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryHandlerListener
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.config.StoryConfigurationMonitor
import ai.tock.bot.engine.monitoring.RequestTimer
import ai.tock.bot.engine.nlp.BuiltInKeywordListener
import ai.tock.bot.engine.nlp.NlpController
import ai.tock.bot.engine.nlp.NlpListener
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.nlp.api.client.NlpClient
import ai.tock.nlp.api.client.model.dump.ApplicationDefinition
import ai.tock.shared.Executor
import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.addConstrainedTypes
import ai.tock.shared.listProperty
import ai.tock.shared.provide
import ai.tock.shared.vertx.vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import org.litote.kmongo.Id
import java.util.ServiceLoader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Advanced bot configuration.
 *
 * [ai.tock.bot.registerAndInstallBot] method is the preferred way to start a bot in most use cases.
 */
object BotRepository {

    private val logger = KotlinLogging.logger {}

    //load only specified configuration ids (dev mode)
    private val restrictedConfigurationIds: List<String> = listProperty("tock_restricted_configuration_id", emptyList())

    private val botConfigurationDAO: BotApplicationConfigurationDAO get() = injector.provide()
    private val storyDefinitionConfigurationDAO: StoryDefinitionConfigurationDAO get() = injector.provide()
    internal val botProviders: MutableMap<BotProviderId, BotProvider> = ConcurrentHashMap()
    internal val storyHandlerListeners: MutableList<StoryHandlerListener> = mutableListOf()
    private val nlpListeners: MutableList<NlpListener> = mutableListOf(BuiltInKeywordListener)
    internal val nlpClient: NlpClient get() = injector.provide()
    private val nlpController: NlpController get() = injector.provide()
    private val executor: Executor get() = injector.provide()
    internal val botAnswerInterceptors: MutableList<BotAnswerInterceptor> = mutableListOf()

    internal val connectorProviders: MutableSet<ConnectorProvider> = CopyOnWriteArraySet(
        ServiceLoader.load(ConnectorProvider::class.java).map { it }.apply {
            forEach {
                logger.info { "Connector ${it.connectorType} loaded" }
                addConstrainedTypes(it.supportedResponseConnectorMessageTypes)
            }
        }
    )

    internal val connectorControllerMap: ConcurrentHashMap<BotApplicationConfiguration, ConnectorController> =
        ConcurrentHashMap()

    private val applicationIdBotApplicationConfigurationMap: ConcurrentHashMap<String, BotApplicationConfiguration> =
        ConcurrentHashMap()

    @Volatile
    internal var botsInstalled: Boolean = false

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
     * @param stateModifier allow the notification to bypass current user state
     * @param notificationType the notification type if any
     */
    fun notify(
        applicationId: String,
        recipientId: PlayerId,
        intent: IntentAware,
        step: StoryStep<out StoryHandlerDefinition>? = null,
        parameters: Map<String, String> = emptyMap(),
        stateModifier: NotifyBotStateModifier = NotifyBotStateModifier.KEEP_CURRENT_STATE,
        notificationType: ActionNotificationType? = null
    ) {
        val conf = getConfigurationByApplicationId(applicationId) ?: error("unknown application $applicationId")
        connectorControllerMap.getValue(conf).notifyAndCheckState(recipientId, intent, step, parameters, stateModifier, notificationType)
    }

    /**
     * Sends a notification to the connector.
     * A [BotBus] is created and the corresponding story is called.
     *
     * @param recipientId the recipient identifier
     * @param intent the notification intent
     * @param step the optional step target
     * @param parameters the optional parameters
     * @param stateModifier allow the notification to bypass current user state
     * @param notificationType notification type if any
     */
    private fun ConnectorController.notifyAndCheckState(
        recipientId: PlayerId,
        intent: IntentAware,
        step: StoryStep<out StoryHandlerDefinition>?,
        parameters: Map<String, String>,
        stateModifier: NotifyBotStateModifier,
        notificationType: ActionNotificationType?
    ) {
        val userTimelineDAO: UserTimelineDAO = injector.provide()
        val userTimeline = userTimelineDAO.loadWithoutDialogs(botDefinition.namespace, recipientId)
        val userState = userTimeline.userState
        val currentState = userState.botDisabled

        if (stateModifier == NotifyBotStateModifier.ACTIVATE_ONLY_FOR_THIS_NOTIFICATION
            || stateModifier == NotifyBotStateModifier.REACTIVATE) {
            userState.botDisabled = false
            userTimelineDAO.save(userTimeline, botDefinition)
        }

        notify(recipientId, intent, step, parameters, notificationType)

        if (stateModifier == NotifyBotStateModifier.ACTIVATE_ONLY_FOR_THIS_NOTIFICATION) {
            val userTimelineAfterNotification = userTimelineDAO.loadWithoutDialogs(botDefinition.namespace, recipientId)
            userTimelineAfterNotification.userState.botDisabled = currentState
            userTimelineDAO.save(userTimeline, botDefinition)
        }
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
        botProviders[bot.botProviderId] = bot
    }

    /**
     * Register built-in story definitions.
     */
    fun registerBuiltInStoryDefinitions(botProvider: BotProvider) {
        val botDefinition = botProvider.botDefinition()
        val configurationName = botProvider.botProviderId.configurationName
        executor.executeBlocking {
            storyDefinitionConfigurationDAO.createBuiltInStoriesIfNotExist(
                botDefinition.stories
                    .filter { it.mainIntent() != Intent.unknown }
                    .map { storyDefinition ->
                        StoryDefinitionConfiguration(botDefinition, storyDefinition, configurationName)
                    }
            )
        }
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

    internal fun getConfigurationByApplicationId(applicationId: String): BotApplicationConfiguration? =
        applicationIdBotApplicationConfigurationMap[applicationId]

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
        val bots = botProviders.values.map { it.botDefinition() }

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

        //persist builtin stories
        botProviders.values.forEach {
            registerBuiltInStoryDefinitions(it)
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

    /**
     * Checks that configurations are synchronized with the database.
     */
    @Synchronized
    fun checkBotConfigurations(startup: Boolean = false) {
        logger.debug { "check configurations" }
        //the application definition cache
        val applicationsCache = mutableListOf<ApplicationDefinition>()
        //the existing confs mapped by path
        val existingConfsByPath: Map<String?, BotApplicationConfiguration> = connectorControllerMap.keys
            .groupBy { it.path }.mapValues { it.value.first() }
        //the existing confs mapped by id
        val existingConfsById: Map<Id<BotApplicationConfiguration>, BotApplicationConfiguration> = connectorControllerMap.keys
            .groupBy { it._id }.mapValues { it.value.first() }
        //path -> botAppConf
        val confs: Map<Id<BotApplicationConfiguration>, BotApplicationConfiguration> =
            botConfigurationDAO
                .getConfigurations()
                .groupBy { it._id }
                .mapValues { it.value.first() }
                .filter { restrictedConfigurationIds.isEmpty() || restrictedConfigurationIds.contains(it.value.applicationId) }

        confs.values.forEach { c ->
            //gets the provider
            val provider = botProviders[BotProviderId(c.botId, c.namespace, c.name)]
                ?: botProviders[BotProviderId(c.botId, c.namespace)]

            //is there a configuration change ?
            if (provider != null &&
                (provider.configurationUpdated
                    || existingConfsByPath[c.path]?.takeIf { c.equalsWithoutId(it) } == null)) {
                val botDefinition = provider.botDefinition()
                if (botDefinition.namespace == c.namespace) {
                    logger.debug { "refresh configuration $c" }
                    val oldConfiguration = existingConfsById[c._id]
                    val oldConfigurationController = oldConfiguration?.let { connectorControllerMap[it] }
                    try {
                        val connector = findConnectorProvider(c.connectorType)?.connector(ConnectorConfiguration(c))
                        if (connector != null) {
                            //install new conf
                            createBot(botDefinition, connector, c, applicationsCache)
                            if (oldConfigurationController != null) {
                                //remove old conf
                                removeBot(oldConfigurationController)
                                if (oldConfiguration != c) {
                                    connectorControllerMap.remove(oldConfiguration)
                                }
                            }
                        } else {
                            logger.warn { "unknown connector ${c.connectorType}" }
                        }
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                } else {
                    logger.trace { "not valid namespace for bot ${c.botId} - installation skipped" }
                }
            }
        }

        //remove deleted confs
        existingConfsById.values.forEach { conf ->
            if (!confs.containsKey(conf._id)) {
                removeBot(conf)
            }
        }

        //updates of all bot providers are now ok
        botProviders.values.forEach { it.configurationUpdated = false }

        if (!startup) {
            //register new confs
            verticle.configure()
        }
        logger.debug { "end check configurations" }
    }

    private fun createBot(
        botDefinition: BotDefinition,
        connector: Connector,
        conf: BotApplicationConfiguration,
        applicationsCache: MutableList<ApplicationDefinition>
    ): BotApplicationConfiguration {

        val app = applicationsCache.find { it.name == botDefinition.nlpModelName && it.namespace == botDefinition.namespace }
            ?: try {
                nlpController.waitAvailability()
                nlpClient.getApplicationByNamespaceAndName(botDefinition.namespace, botDefinition.nlpModelName)
                    ?.also { applicationsCache.add(it) }
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
                applicationIdBotApplicationConfigurationMap[this.applicationId] = this
            }
    }

    private fun removeBot(conf: BotApplicationConfiguration) {
        logger.debug { "uninstall $conf" }
        val controller = connectorControllerMap.remove(conf)
        removeBot(controller)
    }

    private fun removeBot(controller: ConnectorController?) {
        if (controller != null) {
            logger.debug { "unregister $controller" }
            controller.unregisterServices()
            if (controller is TockConnectorController) {
                StoryConfigurationMonitor.unmonitor(controller.bot)
                TockConnectorController.unregister(controller)
            }
        }
    }

}