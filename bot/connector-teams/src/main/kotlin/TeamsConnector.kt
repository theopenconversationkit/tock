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

package ai.tock.bot.connector.teams

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorFeature.CAROUSEL
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.teams.auth.AuthenticateBotConnectorService
import ai.tock.bot.connector.teams.auth.ForbiddenException
import ai.tock.bot.connector.teams.auth.JWKHandler
import ai.tock.bot.connector.teams.messages.SendActionConverter
import ai.tock.bot.connector.teams.messages.TeamsBotMessage
import ai.tock.bot.connector.teams.messages.cardImage
import ai.tock.bot.connector.teams.messages.nlpCardAction
import ai.tock.bot.connector.teams.messages.teamsCarousel
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
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.shared.Executor
import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.warn
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import com.microsoft.bot.schema.Activity
import com.microsoft.bot.schema.ActivityTypes
import mu.KotlinLogging
import java.time.Duration
import java.util.Locale
import kotlin.system.measureTimeMillis

/**
 *
 */
internal class TeamsConnector(
    private val connectorId: String,
    private val path: String,
    val appId: String,
    val appPassword: String,
) : ConnectorBase(teamsConnectorType, setOf(CAROUSEL)) {
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
                val timeElapsed =
                    measureTimeMillis {
                        var responseSent = false
                        val requestTimerData = BotRepository.requestTimer.start("teams_webhook")
                        try {
                            val body = context.body().asString()
                            logger.debug { body }
                            val activity: Activity = mapper.readValue(body)
                            if (activity.type != ActivityTypes.MESSAGE) {
                                throw NoMessageException("The activity received is not a message")
                            }
                            logger.debug { "check authentication..." }
                            authenticateBotConnectorService.checkRequestValidity(
                                jwkHandler,
                                context.request().headers(),
                                activity,
                            )
                            logger.debug { "authentication checked" }
                            executor.executeBlocking {
                                logger.debug { "sentence created..." }
                                val e =
                                    SendSentence(
                                        PlayerId(activity.from.id),
                                        connectorId,
                                        PlayerId(connectorId, PlayerType.bot),
                                        activity.text,
                                    )
                                logger.debug { "send to controller..." }
                                controller.handle(
                                    e,
                                    ConnectorData(
                                        TeamsConnectorCallback(connectorId, activity),
                                    ),
                                )
                            }
                        } catch (e: ForbiddenException) {
                            context.fail(403)
                            responseSent = true
                            logger.logError(e.message ?: "error", requestTimerData)
                        } catch (e: NoMessageException) {
                            logger.warn(e)
                        } catch (e: Throwable) {
                            context.fail(500)
                            responseSent = true
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
                logger.trace { "Time elapsed : $timeElapsed ms" }
            }
        }
    }

    override fun loadProfile(
        callback: ConnectorCallback,
        userId: PlayerId,
    ): UserPreferences {
        return when (callback) {
            is TeamsConnectorCallback -> UserPreferences().apply { locale = locale(callback.activity.locale) }
            else -> UserPreferences()
        }
    }

    override fun refreshProfile(
        callback: ConnectorCallback,
        userId: PlayerId,
    ): UserPreferences? {
        return when (callback) {
            is TeamsConnectorCallback -> UserPreferences().apply { locale = locale(callback.activity.locale) }
            else -> null
        }
    }

    private fun locale(code: String?): Locale =
        try {
            Locale.forLanguageTag(code)
        } catch (e: Exception) {
            logger.error(e)
            defaultLocale
        }

    override fun send(
        event: Event,
        callback: ConnectorCallback,
        delayInMs: Long,
    ) {
        if (event is SendSentence && callback is TeamsConnectorCallback) {
            val teamsMessage = SendActionConverter.toActivity(event)

            val delay = Duration.ofMillis(delayInMs)
            executor.executeBlocking(delay) {
                client.sendMessage(callback.activity, teamsMessage)
            }
        }
    }

    override fun addSuggestions(
        text: CharSequence,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
            teamsMessageWithButtonCard(text, suggestions.map { nlpCardAction(it) })
        }

    override fun addSuggestions(
        message: ConnectorMessage,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
            // TODO support complex cards
            message
        }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> =
        {
            when (message) {
                is MediaCard -> {
                    listOf(
                        teamsHeroCard(
                            message.title ?: "",
                            null,
                            message.subTitle ?: "",
                            listOfNotNull(
                                message.file?.takeIf { it.type == image }?.let { cardImage(it.url) },
                            ),
                            message.actions.map {
                                val url = it.url
                                if (url == null) {
                                    nlpCardAction(it.title)
                                } else {
                                    urlCardAction(it.title, url)
                                }
                            },
                        ),
                    )
                }
                is MediaCarousel -> {
                    listOf(
                        teamsCarousel(
                            message.cards.mapNotNull {
                                toConnectorMessage(it).invoke(this).firstOrNull() as? TeamsBotMessage
                            },
                        ),
                    )
                }
                else -> emptyList()
            }
        }
}

class NoMessageException(exception: String) : Exception(exception)
