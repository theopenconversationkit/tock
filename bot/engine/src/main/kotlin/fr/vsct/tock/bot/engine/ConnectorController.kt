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
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.event.Event
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
class ConnectorController internal constructor(
        val bot: Bot,
        private val connector: Connector,
        private val verticle: BotVerticle) {

    companion object {

        private val logger = KotlinLogging.logger {}
        private val maxLockedAttempts = intProperty("tock_bot_mav_locked_attempts", 3)
        private val lockedAttemptsWaitInMs = longProperty("tock_bot_locked_attempts_wait_in_ms", 200L)

        internal fun register(connector: Connector,
                              bot: Bot,
                              verticle: BotVerticle) {
            logger.info { "Register connector $connector for bot $bot" }
            connector.register(ConnectorController(bot, connector, verticle))
        }
    }

    private val executor: Executor by injector.instance()
    private val userLock: UserLock by injector.instance()
    private val userTimelineDAO: UserTimelineDAO by injector.instance()

    internal val connectorType: ConnectorType get() = connector.connectorType

    fun handle(event: Event) {
        when (event) {
            is Action -> handleAction(event, 0)
            else -> bot.handleEvent(connector, event)
        }
    }

    private fun handleAction(action: Action, nbAttempts: Int) {
        val playerId = action.playerId
        val id = playerId.id

        logger.debug { "try to lock $playerId" }
        if (userLock.lock(id)) {
            try {
                val userTimeline = userTimelineDAO.loadWithLastValidDialog(action.playerId, { bot.botDefinition.findStoryDefinition(it) })
                bot.handle(action, userTimeline, this)
                userTimelineDAO.save(userTimeline)
            } catch(t: Throwable) {
                logger.error(t)
                send(bot.errorActionFor(action))
            } finally {
                userLock.releaseLock(id)
            }
        } else if (nbAttempts < maxLockedAttempts) {
            logger.debug { "$playerId locked - wait" }
            executor.executeBlocking(Duration.ofMillis(lockedAttemptsWaitInMs)) {
                handleAction(action, nbAttempts + 1)
            }

        } else {
            logger.debug { "$playerId locked for $maxLockedAttempts times - skip $action" }
        }
    }

    fun registerServices(rootPath: String, installer: (Router) -> Unit) {
        verticle.registerServices(rootPath, installer)
    }

    internal fun send(action: Action, delay: Long = 0) {
        if (connectorType.asynchronous) {
            sendAsynchronous(action, delay)
        } else {
            sendSynchronous(action, delay)
        }
    }

    private fun sendSynchronous(action: Action, delay: Long = 0) {
        try {
            logger.debug { "message sent: $action" }
            connector.send(action)
        } catch(t: Throwable) {
            logger.error(t)
        }
    }

    private fun sendAsynchronous(action: Action, delay: Long = 0) {
        executor.executeBlocking(Duration.ofMillis(delay)) {
            logger.debug { "message sent: $action" }
            connector.send(action)
        }
    }

    fun errorMessage(playerId: PlayerId, applicationId: String, recipientId: PlayerId): Action {
        val errorAction = bot.botDefinition.errorAction(playerId, applicationId, recipientId)
        errorAction.botMetadata.lastAnswer = true
        return errorAction
    }

    internal fun loadProfile(applicationId: String, playerId: PlayerId): UserPreferences {
        return connector.loadProfile(applicationId, playerId)
    }

    internal fun startTypingInAnswerTo(action: Action) {
        connector.startTypingInAnswerTo(action)
    }

}