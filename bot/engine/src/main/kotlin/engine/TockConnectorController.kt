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

package ai.tock.bot.engine

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.action.SendAttachment.AttachmentType.audio
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.event.TypingOnEvent
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserLock
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.shared.Executor
import ai.tock.shared.booleanProperty
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.intProperty
import ai.tock.shared.longProperty
import ai.tock.shared.provide
import ai.tock.stt.STT
import com.github.salomonbrys.kodein.instance
import io.vertx.ext.web.Router
import mu.KotlinLogging
import java.net.URL
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 */
internal class TockConnectorController constructor(
    val bot: Bot,
    override val connector: Connector,
    private val verticle: BotVerticle,
    override val botDefinition: BotDefinition,
    private val configuration: ConnectorConfiguration
) : ConnectorController {

    companion object {

        private val logger = KotlinLogging.logger {}
        private val maxLockedAttempts = intProperty("tock_bot_max_locked_attempts", 10)
        private val lockedAttemptsWaitInMs = longProperty("tock_bot_locked_attempts_wait_in_ms", 500L)
        private val parseAudioFileEnabled = booleanProperty("tock_bot_audio_nlp_enabled", true)
        private val audioNlpFileLimit = intProperty("tock_bot_audio_nlp_max_size", 1024 * 1024)

        internal fun register(
            connector: Connector,
            bot: Bot,
            verticle: BotVerticle,
            configuration: BotApplicationConfiguration
        ): TockConnectorController =
            TockConnectorController(bot, connector, verticle, bot.botDefinition, ConnectorConfiguration(configuration))
                .apply {
                    logger.info { "Register connector $connector for bot $bot" }
                    connector.register(this)
                }

        internal fun unregister(
            controller: TockConnectorController
        ) {
            logger.info { "Unregister connector ${controller.connector} for bot ${controller.bot}" }
            controller.connector.unregister(controller)
        }

    }

    private val executor: Executor by injector.instance()
    private val userLock: UserLock by injector.instance()
    private val userTimelineDAO: UserTimelineDAO by injector.instance()

    private val serviceInstallers: MutableList<BotVerticle.ServiceInstaller> = CopyOnWriteArrayList()

    fun getBaseUrl(): String = configuration.getBaseUrl()

    override fun handle(event: Event, data: ConnectorData) {
        val callback = data.callback
        try {
            if (!botDefinition.eventListener.listenEvent(this, data, event)) {
                when (event) {
                    is Action -> handleAction(event, 0, data)
                    else -> callback.eventSkipped(event)
                }
            } else {
                callback.eventAnswered(event)
            }
        } catch (t: Throwable) {
            callback.exceptionThrown(event, t)
        }
    }

    private fun tryToParseVoiceAudio(action: Action, userTimeline: UserTimeline): Action {
        if (parseAudioFileEnabled && action is SendAttachment && action.type == audio) {
            val bytes = URL(action.url).readBytes()
            if (bytes.size < audioNlpFileLimit) {
                val stt: STT = injector.provide()
                val text = stt.parse(bytes, userTimeline.userPreferences.locale)
                if (text != null) {
                    return SendSentence(
                        action.playerId,
                        action.applicationId,
                        action.recipientId,
                        text
                    )
                }
            }
        }
        return action
    }

    private fun handleAction(action: Action, nbAttempts: Int, data: ConnectorData) {
        val callback = data.callback
        try {
            val playerId = action.playerId
            val id = playerId.id

            logger.debug { "try to lock $playerId" }
            if (userLock.lock(id)) {
                try {
                    callback.userLocked(action)

                    val userTimeline =
                        userTimelineDAO.loadWithLastValidDialog(
                            botDefinition.namespace,
                            action.playerId,
                            data.priorUserId,
                            data.groupId,
                            storyDefinitionLoader()
                        )

                    val transformedAction = tryToParseVoiceAudio(action, userTimeline)

                    bot.handle(transformedAction, userTimeline, this, data)

                    if (data.saveTimeline) {
                        userTimelineDAO.save(userTimeline, bot.botDefinition)
                    }
                } catch (t: Throwable) {
                    callback.exceptionThrown(action, t)
                    send(data, action, errorMessage(action.recipientId, action.applicationId, action.playerId))
                } finally {
                    userLock.releaseLock(id)
                    callback.userLockReleased(action)
                }
            } else if (nbAttempts < maxLockedAttempts) {
                logger.debug { "$playerId locked - wait" }
                executor.executeBlocking(Duration.ofMillis(lockedAttemptsWaitInMs)) {
                    handleAction(action, nbAttempts + 1, data)
                }

            } else {
                logger.debug { "$playerId locked for $maxLockedAttempts times - skip $action" }
                callback.eventSkipped(action)
            }
        } catch (t: Throwable) {
            callback.exceptionThrown(action, t)
        }
    }

    override fun support(action: Action, data: ConnectorData): Double {
        val callback = data.callback
        return try {
            val userTimeline =
                userTimelineDAO.loadWithLastValidDialog(
                    botDefinition.namespace,
                    action.playerId,
                    data.priorUserId,
                    data.groupId,
                    storyDefinitionLoader()
                )
            bot.support(action, userTimeline, this, data)
        } catch (t: Throwable) {
            callback.exceptionThrown(action, t)
            0.0
        }
    }

    override fun registerServices(serviceIdentifier: String, installer: (Router) -> Unit) {
        verticle.registerServices(serviceIdentifier) { router ->
            //healthcheck
            router.get("$serviceIdentifier/healthcheck").handler {
                it.response().end()
            }
            installer(router)
        }.also { serviceInstallers.add(it) }
    }

    override fun unregisterServices() {
        serviceInstallers.forEach { verticle.unregisterServices(it) }
    }

    internal fun send(data: ConnectorData, userAction: Action, action: Action, delay: Long = 0) {
        try {
            logger.debug { "message sent to connector: $action" }
            connector.send(action, data.callback, delay)
        } catch (t: Throwable) {
            logger.error(t)
        } finally {
            if (action.metadata.lastAnswer) {
                data.callback.eventAnswered(userAction)
            }
        }
    }

    fun loadProfile(data: ConnectorData, playerId: PlayerId): UserPreferences? {
        return connector.loadProfile(data.callback, playerId)
    }

    fun refreshProfile(data: ConnectorData, playerId: PlayerId): UserPreferences? {
        return connector.refreshProfile(data.callback, playerId)
    }

    fun startTypingInAnswerTo(action: Action, data: ConnectorData) {
        connector.send(TypingOnEvent(action.playerId, action.applicationId), data.callback)
    }

    override fun toString(): String = configuration.toString()

}