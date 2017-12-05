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
import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorData
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.event.TypingOnEvent
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserLock
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.intProperty
import fr.vsct.tock.shared.longProperty
import io.vertx.ext.web.Router
import mu.KotlinLogging
import java.time.Duration

/**
 *
 */
internal class TockConnectorController constructor(
        val bot: Bot,
        override val connector: Connector,
        private val verticle: BotVerticle,
        override val botDefinition: BotDefinition = bot.botDefinition) : ConnectorController {

    companion object {

        private val logger = KotlinLogging.logger {}
        private val maxLockedAttempts = intProperty("tock_bot_max_locked_attempts", 10)
        private val lockedAttemptsWaitInMs = longProperty("tock_bot_locked_attempts_wait_in_ms", 500L)

        internal fun register(connector: Connector,
                              bot: Bot,
                              verticle: BotVerticle) {
            logger.info { "Register connector $connector for bot $bot" }
            connector.register(TockConnectorController(bot, connector, verticle))
        }
    }

    private val executor: Executor by injector.instance()
    private val userLock: UserLock by injector.instance()
    private val userTimelineDAO: UserTimelineDAO by injector.instance()

    val connectorType: ConnectorType get() = connector.connectorType

    override fun handle(event: Event, data: ConnectorData) {
        val callback = data.callback
        try {
            if (!botDefinition.eventListener.listenEvent(this, event)) {
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

    private fun handleAction(action: Action, nbAttempts: Int, data: ConnectorData) {
        val callback = data.callback
        try {
            val playerId = action.playerId
            val id = playerId.id

            logger.debug { "try to lock $playerId" }
            if (userLock.lock(id)) {
                try {
                    callback.userLocked(action)
                    val userTimeline = userTimelineDAO.loadWithLastValidDialog(action.playerId, { bot.botDefinition.findStoryDefinition(it) })
                    bot.handle(action, userTimeline, this, data)
                    userTimelineDAO.save(userTimeline)
                } catch (t: Throwable) {
                    callback.exceptionThrown(action, t)
                    send(errorMessage(action.recipientId, action.applicationId, action.playerId))
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

    override fun registerServices(rootPath: String, installer: (Router) -> Unit) {
        verticle.registerServices(rootPath, installer)
    }

    fun send(action: Action, delay: Long = 0) = send(null, null, action, delay)

    internal fun send(data: ConnectorData?, userAction: Action?, action: Action, delay: Long = 0) {
        try {
            logger.debug { "message sent to connector: $action" }
            connector.send(action, delay)
        } catch (t: Throwable) {
            logger.error(t)
        } finally {
            if (action.metadata.lastAnswer) {
                userAction?.let { data?.callback?.eventAnswered(userAction) }
            }
        }
    }

    fun loadProfile(applicationId: String, playerId: PlayerId): UserPreferences? {
        return connector.loadProfile(applicationId, playerId)
    }

    fun startTypingInAnswerTo(action: Action) {
        connector.send(TypingOnEvent(action.playerId, action.applicationId))
    }

}