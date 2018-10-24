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
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 */
internal class TockConnectorController constructor(
    val bot: Bot,
    override val connector: Connector,
    private val verticle: BotVerticle,
    override val botDefinition: BotDefinition = bot.botDefinition
) : ConnectorController {

    companion object {

        private val logger = KotlinLogging.logger {}
        private val maxLockedAttempts = intProperty("tock_bot_max_locked_attempts", 10)
        private val lockedAttemptsWaitInMs = longProperty("tock_bot_locked_attempts_wait_in_ms", 500L)

        internal fun register(
            connector: Connector,
            bot: Bot,
            verticle: BotVerticle
        ): TockConnectorController =
            TockConnectorController(bot, connector, verticle)
                .apply {
                    logger.info { "Register connector $connector for bot $bot" }
                    connector.register(this)
                }
    }

    private val executor: Executor by injector.instance()
    private val userLock: UserLock by injector.instance()
    private val userTimelineDAO: UserTimelineDAO by injector.instance()

    val connectorType: ConnectorType get() = connector.connectorType

    private var serviceInstallers: MutableList<BotVerticle.ServiceInstaller> = CopyOnWriteArrayList()

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
                            action.playerId,
                            data.priorUserId
                        ) { bot.botDefinition.findStoryDefinition(it) }
                    bot.handle(action, userTimeline, this, data)
                    if(data.saveTimeline) {
                        userTimelineDAO.save(userTimeline)
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
                    action.playerId,
                    data.priorUserId,
                    { bot.botDefinition.findStoryDefinition(it) }
                )
            bot.support(action, userTimeline, this, data)
        } catch (t: Throwable) {
            callback.exceptionThrown(action, t)
            0.0
        }
    }

    override fun registerServices(serviceIdentifier: String, installer: (Router) -> Unit) {
        verticle.registerServices(serviceIdentifier, installer).also { serviceInstallers.add(it) }
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

    fun startTypingInAnswerTo(action: Action, data: ConnectorData) {
        connector.send(TypingOnEvent(action.playerId, action.applicationId), data.callback)
    }

}