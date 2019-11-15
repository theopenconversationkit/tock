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

package ai.tock.bot.connector.teams

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.teams.auth.AuthenticateBotConnectorService
import ai.tock.bot.connector.teams.auth.ForbiddenException
import ai.tock.bot.connector.teams.auth.JWKHandler
import ai.tock.bot.connector.teams.messages.SendActionConverter
import ai.tock.bot.connector.teams.messages.cardImage
import ai.tock.bot.connector.teams.messages.nlpCardAction
import ai.tock.bot.connector.teams.messages.teamsHeroCard
import ai.tock.bot.connector.teams.messages.teamsMessageWithButtonCard
import ai.tock.bot.connector.teams.messages.urlCardAction
import ai.tock.bot.connector.teams.token.TokenHandler
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.SendAttachment.AttachmentType.image
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.monitoring.logError
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.Executor
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import com.microsoft.bot.schema.models.Activity
import com.microsoft.bot.schema.models.ActivityTypes
import mu.KotlinLogging
import java.time.Duration
import kotlin.system.measureTimeMillis

/**
 *
 */
internal class TeamsConnector(
    private val connectorId: String,
    private val path: String,
    val appId: String,
    val appPassword: String
) : ConnectorBase(teamsConnectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private var tokenHandler = TokenHandler(appId, appPassword)
    private val client = TeamsClient(tokenHandler)
    private val jwkHandler = JWKHandler()
    private val executor: Executor by injector.instance()
    private val authenticateBotConnectorService = AuthenticateBotConnectorService(appId)

    override fun unregister(controller: ConnectorController) {
        super.unregister(controller)
        logger.debug("Stopping tokenCollector for $connectorId")
        tokenHandler.stopTokenCollector()
        logger.debug("Stopping JWKHandler for $connectorId")
        jwkHandler.stopJWKCollector()
    }

    override fun register(controller: ConnectorController) {

        logger.debug("Register TeamsConnector : $connectorId")
        tokenHandler.launchTokenCollector(connectorId)
        jwkHandler.launchJWKCollector(connectorId)

        controller.registerServices(path) { router ->

            router.post(path).handler { context ->
                val timeElapsed = measureTimeMillis {
                    var responseSent = false
                    val requestTimerData = BotRepository.requestTimer.start("teams_webhook")
                    try {
                        val body = context.bodyAsString
                        val activity: Activity = mapper.readValue(body)
                        if (activity.type() != ActivityTypes.MESSAGE) {
                            logger.debug(activity.toString())
                            throw NoMessageException("The activity received is not a message")
                        }
                        authenticateBotConnectorService.checkRequestValidity(
                            jwkHandler,
                            context.request().headers(),
                            activity
                        )
                        executor.executeBlocking {
                            val e: Event? = SendSentence(
                                PlayerId(activity.from().id()),
                                connectorId,
                                PlayerId(connectorId, PlayerType.bot),
                                activity.text()
                            )
                            if (e != null) {
                                controller.handle(
                                    e,
                                    ConnectorData(
                                        TeamsConnectorCallback(connectorId, activity)
                                    )
                                )
                            } else {
                                logger.warn { "null event for $body" }
                            }
                        }
                    } catch (e: ForbiddenException) {
                        context.fail(403)
                        responseSent = true
                        logger.logError(e.message ?: "error", requestTimerData)
                    } catch (e: NoMessageException) {
                        logger.warn(e.toString())
                    } catch (e: Exception) {
                        logger.logError(e, requestTimerData)
                    } finally {
                        BotRepository.requestTimer.end(requestTimerData)
                        if (!responseSent) {
                            try {
                                context.response().end()
                            } catch (e: Throwable) {
                                logger.error(e)
                            }
                        }
                    }
                }
                logger.trace("Time elapsed : $timeElapsed ms")
            }
        }
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        if (event is SendSentence && callback is TeamsConnectorCallback) {

            val teamsMessage = SendActionConverter.toActivity(event)

            val delay = Duration.ofMillis(delayInMs)
            executor.executeBlocking(delay) {
                client.sendMessage(callback.activity, teamsMessage)
            }
        }
    }

    override fun addSuggestions(text: CharSequence, suggestions: List<CharSequence>): BotBus.() -> ConnectorMessage? = {
        teamsMessageWithButtonCard(text, suggestions.map { nlpCardAction(it) })
    }

    override fun addSuggestions(message: ConnectorMessage, suggestions: List<CharSequence>): BotBus.() -> ConnectorMessage? = {
        //TODO support complex cards
        message
    }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> = {
        when (message) {
            is MediaCard -> {
                listOf(teamsHeroCard(
                    message.title ?: message.subTitle ?: "",
                    null,
                    message.subTitle ?: message.title ?: "",
                    listOfNotNull(
                        message.file?.takeIf { it.type == image }?.let { cardImage(it.url) }
                    ),
                    message.actions.map {
                        val url = it.url
                        if (url == null) {
                            nlpCardAction(it.title)
                        } else {
                            urlCardAction(it.title, url)
                        }
                    }
                )
                )
            }
            else -> emptyList()
        }
    }
}

class NoMessageException(exception: String) : Exception(exception)
