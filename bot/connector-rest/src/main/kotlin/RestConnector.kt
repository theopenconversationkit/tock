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

package ai.tock.bot.connector.rest

import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorFeature
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorType.Companion.rest
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.rest.model.MessageRequest
import ai.tock.bot.connector.rest.model.MessageResponse
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.bot.engine.message.GenericMessage.Companion.TEXT_PARAM
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.shared.booleanProperty
import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.RequestFilter
import ai.tock.shared.vertx.blocking
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import java.util.Locale

/**
 *
 */
class RestConnector(
    val applicationId: String,
    private val path: String,
    private val requestFilter: RequestFilter,
) : ConnectorBase(rest) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val disabled = booleanProperty("tock_rest_connector_disabled", false)
        internal val checkNlpStats = booleanProperty("tock_rest_connector_check_nlp_stats", false)
    }

    override fun hasFeature(
        feature: ConnectorFeature,
        targetConnectorType: ConnectorType,
    ): Boolean = getTargetConnector(targetConnectorType)?.hasFeature(feature, targetConnectorType) ?: false

    override fun register(controller: ConnectorController) {
        if (!disabled) {
            logger.info { "deploy rest connector to $path" }
            controller.registerServices(path) { router ->
                router.post("$path/:locale").blocking { context ->
                    if (!requestFilter.accept(context.request())) {
                        context.response().setStatusCode(403).end()
                        return@blocking
                    }
                    val message: MessageRequest = mapper.readValue(context.body().asString())
                    val action = transformMessage(message)
                    val locale = parseLocale(context.pathParam("locale"))
                    action.state.sourceConnectorType = message.connectorType
                    action.state.targetConnectorType = message.targetConnectorType
                    action.metadata.debugEnabled = message.debugEnabled
                    action.metadata.sourceWithContent = message.sourceWithContent
                    controller.handle(
                        action,
                        ConnectorData(
                            RestConnectorCallback(
                                applicationId,
                                message.targetConnectorType,
                                context,
                                if (message.test) controller.botDefinition.testBehaviour else null,
                                locale,
                                action,
                            ),
                        ),
                    )
                }
            }
        }
    }

    private fun transformMessage(message: MessageRequest): Action =
        with(message.message) {
            // choice nlp support
            val nlp = (this as? Choice)?.parameters?.get(SendChoice.NLP)
            val m = takeUnless { nlp != null } ?: Sentence(nlp)
            m.toAction(
                PlayerId(message.userId, PlayerType.user),
                applicationId,
                PlayerId(message.recipientId, PlayerType.bot),
            )
        }

    override fun send(
        event: Event,
        callback: ConnectorCallback,
        delayInMs: Long,
    ) {
        callback as RestConnectorCallback
        if (event is Action) {
            callback.actions.add(event)
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }

    override fun loadProfile(
        callback: ConnectorCallback,
        userId: PlayerId,
    ): UserPreferences {
        callback as RestConnectorCallback
        // register user as test user if applicable
        return UserPreferences().apply {
            locale = callback.locale
            callback.testContext?.setup(this, callback.connectorType, locale)
        }
    }

    private fun getTargetConnector(targetConnectorType: ConnectorType): Connector? = BotRepository.getController { it.connectorType == targetConnectorType }?.connector

    override fun addSuggestions(
        text: CharSequence,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
            when (targetConnectorType) {
                rest ->
                    MessageResponse(
                        listOf(
                            Sentence(
                                null,
                                mutableListOf(
                                    GenericMessage(
                                        texts = mapOf(TEXT_PARAM to text.toString()),
                                        choices = suggestions.map { Choice.fromText(it.toString()) },
                                    ),
                                ),
                            ),
                        ),
                        applicationId,
                    )
                else -> getTargetConnector(targetConnectorType)?.addSuggestions(text, suggestions)?.invoke(this)
            }
        }

    override fun addSuggestions(
        message: ConnectorMessage,
        suggestions: List<CharSequence>,
    ): BotBus.() -> ConnectorMessage? =
        {
            when (targetConnectorType) {
                rest -> {
                    val response = message as? MessageResponse
                    val sentence = response?.messages?.lastOrNull() as? Sentence
                    val lastMessage = sentence?.messages?.lastOrNull()
                    if (lastMessage?.choices?.isEmpty() == true) {
                        sentence.messages[sentence.messages.size - 1] =
                            lastMessage.copy(choices = suggestions.map { Choice.fromText(it.toString()) })
                    }
                    message
                }
                else -> getTargetConnector(targetConnectorType)?.addSuggestions(message, suggestions)?.invoke(this)
            }
        }

    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> =
        {
            when (targetConnectorType) {
                rest ->
                    listOfNotNull(
                        message.toGenericMessage()?.let {
                            MessageResponse(
                                listOf(
                                    Sentence(
                                        null,
                                        mutableListOf(it),
                                    ),
                                ),
                                applicationId,
                            )
                        },
                    )
                else -> getTargetConnector(targetConnectorType)?.toConnectorMessage(message)?.invoke(this) ?: emptyList()
            }
        }

    private fun parseLocale(rawLocale: String): Locale {
        // Accept both BCP47 (fr-FR) and legacy underscore (fr_FR) formats.
        val sanitized = rawLocale.replace('_', '-')
        val fromTag = Locale.forLanguageTag(sanitized)
        if (fromTag.language.isNotBlank()) {
            return fromTag
        }

        // Fallback: manual split for legacy or non-standard formats
        val legacyParts = rawLocale.replace('-', '_').split('_')
        if (legacyParts.size >= 2 && legacyParts[0].isNotBlank()) {
            return Locale(legacyParts[0], legacyParts[1])
        }

        //Last resort: use raw string or system default
        return if (rawLocale.isNotBlank()) Locale(rawLocale) else Locale.getDefault()
    }
}