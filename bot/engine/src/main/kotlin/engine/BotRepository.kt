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

package ai.tock.bot.engine

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.bot.BotApplicationConfigurationKey
import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.admin.indicators.Indicator
import ai.tock.bot.admin.indicators.IndicatorDAO
import ai.tock.bot.admin.indicators.metric.Metric
import ai.tock.bot.admin.indicators.metric.MetricDAO
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorService
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.NotifyBotStateModifier
import ai.tock.bot.definition.BotAnswerInterceptor
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.BotProvider
import ai.tock.bot.definition.BotProviderId
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryHandlerListener
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.config.BotDocumentCompressorConfigurationMonitor
import ai.tock.bot.engine.config.BotObservabilityConfigurationMonitor
import ai.tock.bot.engine.config.BotRAGConfigurationMonitor
import ai.tock.bot.engine.config.BotVectorStoreConfigurationMonitor
import ai.tock.bot.engine.config.StoryConfigurationMonitor
import ai.tock.bot.engine.monitoring.RequestTimer
import ai.tock.bot.engine.nlp.AsyncNlpListener
import ai.tock.bot.engine.nlp.BuiltInKeywordListener
import ai.tock.bot.engine.nlp.LegacyNlpListenerAdapter
import ai.tock.bot.engine.nlp.NlpController
import ai.tock.bot.engine.nlp.NlpListener
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.nlp.api.client.NlpClient
import ai.tock.shared.Executor
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.addConstrainedTypes
import ai.tock.shared.listProperty
import ai.tock.shared.provide
import ai.tock.shared.vertx.vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.litote.kmongo.Id
import java.util.ServiceLoader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.locks.Lock

/**
 * Advanced bot configuration.
 *
 * [ai.tock.bot.registerAndInstallBot] method is the preferred way to start a bot in most use cases.
 */
@OptIn(ExperimentalTockCoroutines::class)
object BotRepository {
    private val logger = KotlinLogging.logger {}

    // load only specified configuration ids (dev mode)
    private val restrictedConfigurationIds: List<String> = listProperty("tock_restricted_configuration_id", emptyList())
    private val statsMetricDAO: MetricDAO get() = injector.provide()
    private val indicatorDAO: IndicatorDAO get() = injector.provide()
    private val botConfigurationDAO: BotApplicationConfigurationDAO get() = injector.provide()
    private val storyDefinitionConfigurationDAO: StoryDefinitionConfigurationDAO get() = injector.provide()
    internal val botProviders: MutableMap<BotProviderId, BotProvider> = ConcurrentHashMap()
    internal val storyHandlerListeners: MutableList<StoryHandlerListener> = CopyOnWriteArrayList()
    private val nlpListeners: MutableList<AsyncNlpListener> = CopyOnWriteArrayList(listOf(LegacyNlpListenerAdapter(BuiltInKeywordListener)))
    internal val nlpClient: NlpClient get() = injector.provide()
    private val nlpController: NlpController get() = injector.provide()
    private val executor: Executor get() = injector.provide()
    internal val botAnswerInterceptors: MutableList<BotAnswerInterceptor> = CopyOnWriteArrayList()
    private val connectorServices: MutableSet<ConnectorService> =
        CopyOnWriteArraySet(ServiceLoader.load(ConnectorService::class.java).toList())

    internal val detailedHealthcheckTasks: MutableList<Pair<String, () -> Boolean>> =
        mutableListOf(Pair("nlp_client") { nlpClient.healthcheck() })

    internal val connectorProviders: MutableSet<ConnectorProvider> =
        CopyOnWriteArraySet(
            ServiceLoader.load(ConnectorProvider::class.java).map { it }.apply {
                forEach {
                    logger.info { "Connector ${it.connectorType} loaded" }
                    addConstrainedTypes(it.supportedResponseConnectorMessageTypes)
                }
            },
        )

    internal val connectorControllerMap: ConcurrentHashMap<BotApplicationConfiguration, ConnectorController> =
        ConcurrentHashMap()

    private val applicationIdBotApplicationConfigurationMap:
        ConcurrentHashMap<BotApplicationConfigurationKey, BotApplicationConfiguration> = ConcurrentHashMap()

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

    @Volatile
    var botAPI: Boolean = false

    private val verticle by lazy { BotVerticle() }

    /**
     * Calls the specified [action] for each registered [NlpListener].
     */
    suspend fun forEachNlpListener(action: suspend (AsyncNlpListener) -> Unit) {
        nlpListeners.forEach { action(it) }
    }

    /**
     * Sends a notification to a connector.
     * A [Bus] is created and the corresponding story is called.
     *
     * @param applicationId the configuration connector id
     * @param recipientId the recipient identifier
     * @param intent the notification intent
     * @param step the optional step target
     * @param parameters the optional parameters
     * @param stateModifier allow the notification to bypass current user state
     * @param notificationType the notification type if any
     * @param errorListener called when a message has not been delivered
     */
    @Deprecated(
        "use ai.tock.bot.definition.notify",
        replaceWith = ReplaceWith("notify", "ai.tock.bot.definition.notify"),
    )
    fun notify(
        applicationId: String,
        recipientId: PlayerId,
        intent: IntentAware,
        step: StoryStepDef? = null,
        parameters: Map<String, String> = emptyMap(),
        stateModifier: NotifyBotStateModifier = NotifyBotStateModifier.KEEP_CURRENT_STATE,
        notificationType: ActionNotificationType? = null,
        namespace: String? = null,
        botId: String? = null,
        errorListener: (Throwable) -> Unit = {},
    ) {
        val key =
            if (namespace == null || botId == null) {
                logger.warn { "notify without specifying namespace or botId will be removed in next release" }
                applicationIdBotApplicationConfigurationMap.keys.firstOrNull { it.applicationId == applicationId }
            } else {
                BotApplicationConfigurationKey(applicationId = applicationId, namespace = namespace, botId = botId)
            }
        val conf = key?.let { getConfigurationByApplicationId(it) } ?: error("unknown application $applicationId")
        connectorControllerMap.getValue(conf)
            .notifyAndCheckState(recipientId, intent, step, parameters, stateModifier, notificationType, errorListener)
    }

    private fun ConnectorController.notifyAndCheckState(
        recipientId: PlayerId,
        intent: IntentAware,
        step: StoryStepDef?,
        parameters: Map<String, String>,
        stateModifier: NotifyBotStateModifier,
        notificationType: ActionNotificationType?,
        errorListener: (Throwable) -> Unit = {},
    ) {
        runBlocking {
            val userTimelineDAO: UserTimelineDAO = injector.provide()
            val userTimeline = userTimelineDAO.loadWithoutDialogs(botDefinition.namespace, recipientId)
            val userState = userTimeline.userState
            val currentState = userState.botDisabled

            if (stateModifier == NotifyBotStateModifier.ACTIVATE_ONLY_FOR_THIS_NOTIFICATION ||
                stateModifier == NotifyBotStateModifier.REACTIVATE
            ) {
                userState.botDisabled = false
                userTimelineDAO.save(userTimeline, botDefinition)
            }

            notify(recipientId, intent, step, parameters, notificationType, errorListener)

            if (stateModifier == NotifyBotStateModifier.ACTIVATE_ONLY_FOR_THIS_NOTIFICATION) {
                val userTimelineAfterNotification =
                    userTimelineDAO.loadWithoutDialogs(botDefinition.namespace, recipientId)
                userTimelineAfterNotification.userState.botDisabled = currentState
                userTimelineDAO.save(userTimeline, botDefinition)
            }
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
     * Check if bot provider has changed
     */
    fun getBotProvider(botProviderId: BotProviderId): BotProvider? = botProviders[botProviderId]

    /**
     * Register built-in story definitions.
     */
    fun registerBuiltInStoryDefinitions(botProvider: BotProvider) {
        val botDefinition = botProvider.botDefinition()
        checkBuiltInStoryCompliance(botDefinition)
        val configurationName = botProvider.botProviderId.configurationName
        executor.executeBlocking {
            storyDefinitionConfigurationDAO.createBuiltInStoriesIfNotExist(
                botDefinition.stories
                    .filter { it.mainIntent() != Intent.unknown }
                    .map { storyDefinition ->
                        StoryDefinitionConfiguration(botDefinition, storyDefinition, configurationName)
                    },
            )
        }
    }

    private fun checkBuiltInStoryCompliance(botDefinition: BotDefinition) {
        val starterIntentsMap: MutableMap<String, MutableList<StoryDefinition>> = mutableMapOf()
        botDefinition.stories.map { s ->
            s.starterIntents.forEach {
                val l = starterIntentsMap.getOrPut(it.name) { mutableListOf() }
                l.add(s)
            }
        }
        val duplicates = starterIntentsMap.mapValues { s -> s.value.distinctBy { it.id } }.filter { it.value.size > 1 }
        if (duplicates.isNotEmpty()) {
            error("duplicate starter intents: $duplicates")
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
     * Registers a new [NlpListener].
     */
    fun registerNlpListener(listener: NlpListener) {
        registerNlpListener(LegacyNlpListenerAdapter(listener))
    }

    /**
     * Registers a new [AsyncNlpListener].
     */
    @ExperimentalTockCoroutines
    fun registerNlpListener(listener: AsyncNlpListener) {
        nlpListeners.add(listener)
    }

    fun registerConnectorService(service: ConnectorService) {
        connectorServices.add(service)
    }

    /**
     * Register a new task to be check by the detailed healthcheck.
     * A task as a name and check action that return true if the service is good and false if it's KO.
     */
    fun registerDetailedHealtcheckTask(task: Pair<String, () -> Boolean>) {
        detailedHealthcheckTasks.add(task)
    }

    internal fun getConfigurationByApplicationId(key: BotApplicationConfigurationKey): BotApplicationConfiguration? = applicationIdBotApplicationConfigurationMap[key]

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
     * @param createApplicationIfNotExists create an nlp application if not exists
     * @param startupLock if not null, wait do listen until the lock is released
     */
    fun installBots(
        routerHandlers: List<(Router) -> Any?>,
        createApplicationIfNotExists: Boolean = true,
        startupLock: Lock? = null,
    ) {
        val bots = botProviders.values.map { it.botDefinition() }

        // check that nlp applications exist
        if (createApplicationIfNotExists) {
            bots.distinctBy { it.namespace to it.nlpModelName }
                .forEach { botDefinition ->
                    try {
                        nlpClient.createApplication(
                            botDefinition.namespace,
                            botDefinition.nlpModelName,
                            defaultLocale,
                        )?.apply {
                            logger.info { "nlp application initialized $namespace $name with locale $supportedLocales" }
                        }
                    } catch (e: Exception) {
                        logger.error(e)
                    }
                }
        }

        // persist builtin stories
        botProviders.values.forEach {
            registerBuiltInStoryDefinitions(it)
        }

        // load configurations
        try {
            checkBotConfigurations(startup = true)
        } catch (e: Exception) {
            logger.error(e)
        }

        // register services
        routerHandlers.forEachIndexed { index, handler ->
            verticle.registerServices("_handler_$index", handler)
        }

        // deploy verticle
        if (botsInstalled) {
            logger.warn { "bot already installed - try to configure new confs" }
            verticle.configure()
        } else {
            val lockFree =
                try {
                    startupLock?.tryLock(5, MINUTES) ?: true
                } catch (e: InterruptedException) {
                    logger.error(e)
                    false
                }
            if (lockFree) {
                vertx.deployVerticle(verticle).onComplete {
                    if (it.succeeded()) {
                        logger.info { "Bots installed" }
                        botsInstalled = true
                        // listen future changes
                        botConfigurationDAO.listenChanges { checkAsyncBotConfigurations() }
                        botConfigurationDAO.listenBotChanges { checkAsyncBotConfigurations() }
                    } else {
                        logger.error("Bots installation failure", it.cause() ?: IllegalArgumentException())
                    }
                }
            } else {
                logger.error("Lock is not free")
            }
        }
    }

    /**
     * Returns the [ConnectorProvider] for the specified [ConnectorType].
     */
    fun findConnectorProvider(connectorType: ConnectorType): ConnectorProvider? {
        return connectorProviders.firstOrNull { it.connectorType == connectorType }
    }

    private fun checkAsyncBotConfigurations() {
        executor.executeBlocking { checkBotConfigurations() }
    }

    /**
     * Checks that configurations are synchronized with the database.
     */
    @Synchronized
    fun checkBotConfigurations(startup: Boolean = false) {
        logger.debug { "check configurations" }
        // the application definition cache
        val botConfigurationsCache = mutableSetOf<BotConfiguration>()
        // the existing confs mapped by path
        val existingConfsByPath: Map<String?, BotApplicationConfiguration> =
            connectorControllerMap.keys
                .groupBy { it.path }.mapValues { it.value.first() }
        // the existing confs mapped by id
        val existingConfsById: Map<Id<BotApplicationConfiguration>, BotApplicationConfiguration> =
            connectorControllerMap.keys
                .groupBy { it._id }.mapValues { it.value.first() }
        // path -> botAppConf
        val confs: Map<Id<BotApplicationConfiguration>, BotApplicationConfiguration> =
            botConfigurationDAO
                .getConfigurations()
                .groupBy { it._id }
                .mapValues { it.value.first() }
                .filter { restrictedConfigurationIds.isEmpty() || restrictedConfigurationIds.contains(it.value.applicationId) }

        confs.values.forEach { c ->
            // gets the provider
            val provider =
                botProviders[BotProviderId(c.botId, c.namespace, c.name)]
                    ?: botProviders[BotProviderId(c.botId, c.namespace)]

            // is there a configuration change ?
            if (provider != null &&
                (
                    provider.configurationUpdated ||
                        existingConfsByPath[c.path]?.takeIf { c.equalsWithoutId(it) } == null
                )
            ) {
                val botDefinition = provider.botDefinition()
                if (botDefinition.namespace == c.namespace) {
                    logger.debug { "refresh configuration $c" }
                    val oldConfiguration = existingConfsById[c._id]
                    val oldConfigurationController = oldConfiguration?.let { connectorControllerMap[it] }
                    try {
                        val connector = findConnectorProvider(c.connectorType)?.connector(ConnectorConfiguration(c))
                        if (connector != null) {
                            // install new conf
                            createBot(botDefinition, connector, c, botConfigurationsCache)
                            if (oldConfigurationController != null) {
                                logger.info { "update configuration: $c" }
                                // remove old conf
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

        // remove deleted confs
        existingConfsById.values.forEach { conf ->
            if (!confs.containsKey(conf._id)) {
                removeBot(conf)
            }
        }

        // updates of all bot providers are now ok
        botProviders.values.forEach { it.configurationUpdated = false }

        if (!startup) {
            // register new confs
            verticle.configure()
        }
        logger.debug { "end check configurations" }
    }

    private fun createBot(
        botDefinition: BotDefinition,
        connector: Connector,
        conf: BotApplicationConfiguration,
        botConfigurationsCache: MutableSet<BotConfiguration>,
    ): BotApplicationConfiguration {
        val botConfiguration =
            botConfigurationsCache.find { it.botId == conf.botId && it.namespace == conf.namespace && it.name == conf.name }
                ?: botConfigurationDAO.getBotConfigurationsByNamespaceAndNameAndBotId(
                    conf.namespace,
                    conf.name,
                    conf.botId,
                )
                ?: BotConfiguration(
                    name = conf.name,
                    botId = conf.botId,
                    namespace = conf.namespace,
                    nlpModel = conf.nlpModel,
                )

        val supportedLocales =
            if (botConfiguration.supportedLocales.isEmpty()) {
                try {
                    nlpController.waitAvailability()
                    val app =
                        nlpClient.getApplicationByNamespaceAndName(
                            botDefinition.namespace,
                            botDefinition.nlpModelName,
                        )

                    val locales = app?.supportedLocales
                    if (locales != null) {
                        val newBotConf = botConfiguration.copy(supportedLocales = locales)
                        botConfigurationDAO.save(newBotConf)
                        botConfigurationsCache.add(newBotConf)
                    }
                    locales ?: emptySet()
                } catch (e: Exception) {
                    logger.error(e)
                    emptySet()
                }
            } else {
                botConfigurationsCache.add(botConfiguration)
                botConfiguration.supportedLocales
            }

        if (supportedLocales.isEmpty()) {
            logger.warn { "no supported locales found for ${botDefinition.namespace}:${botDefinition.nlpModelName}" }
        } else {
            logger.debug { "locales for ${botDefinition.namespace}:${botDefinition.nlpModelName}: $supportedLocales" }
        }
        val bot = Bot(botDefinition, conf, supportedLocales)
        return botConfigurationDAO.save(conf)
            .apply {
                val controller = TockConnectorController.register(connector, bot, verticle, conf)
                // install connector services
                connectorServices.forEach { connectorService ->
                    connectorService.install(controller, conf)
                }
                // monitor bot
                StoryConfigurationMonitor.monitor(bot)
                BotRAGConfigurationMonitor.monitor(bot)
                BotObservabilityConfigurationMonitor.monitor(bot)
                BotVectorStoreConfigurationMonitor.monitor(bot)
                BotDocumentCompressorConfigurationMonitor.monitor(bot)
                // register connector controller map
                connectorControllerMap[this] = controller
                applicationIdBotApplicationConfigurationMap[toKey()] = this
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
                BotRAGConfigurationMonitor.unmonitor(controller.bot)
                BotObservabilityConfigurationMonitor.unmonitor(controller.bot)
                BotVectorStoreConfigurationMonitor.unmonitor(controller.bot)
                BotDocumentCompressorConfigurationMonitor.unmonitor(controller.bot)
                TockConnectorController.unregister(controller)
            }
        }
    }

    /**
     * Delegation method to save one [Metric]
     * @param metric a [Metric] to save
     */
    fun saveMetric(metric: Metric) = statsMetricDAO.save(metric)

    /**
     * Delegation method to save many [Metric]
     * @param metrics a set of [Metric] to save
     */
    fun saveMetrics(metrics: List<Metric>) = statsMetricDAO.saveAll(metrics)

    fun getIndicatorByName(
        name: String,
        namespace: String,
        botId: String,
    ) = indicatorDAO.findByNameAndBotId(name, namespace, botId)

    fun saveIndicator(indicator: Indicator) = indicatorDAO.save(indicator)
}
