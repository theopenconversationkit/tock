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

package fr.vsct.tock.bot.connector.rest

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.connector.ConnectorData
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.media.MediaMessage
import fr.vsct.tock.bot.connector.rest.model.MessageRequest
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.security.RequestFilter
import fr.vsct.tock.shared.vertx.blocking
import mu.KotlinLogging
import java.util.Locale

/**
 *
 */
class RestConnector(
    val applicationId: String,
    private val path: String,
    private val requestFilter: RequestFilter
) : ConnectorBase(ConnectorType.rest) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val disabled = booleanProperty("tock_rest_connector_disabled", false)
    }

    override fun register(controller: ConnectorController) {
        if (!disabled) {
            logger.info { "deploy rest connector to $path" }
            controller.registerServices(path) { router ->
                router.post("$path/:locale").blocking { context ->
                    if (!requestFilter.accept(context.request())) {
                        context.response().setStatusCode(403).end()
                        return@blocking
                    }
                    val message: MessageRequest = mapper.readValue(context.bodyAsString)
                    val action = message.message.toAction(
                        PlayerId(message.userId, PlayerType.user),
                        applicationId,
                        PlayerId(message.recipientId, PlayerType.bot)
                    )
                    val locale = Locale.forLanguageTag(context.pathParam("locale"))
                    action.state.targetConnectorType = message.targetConnectorType
                    controller.handle(
                        action,
                        ConnectorData(
                            RestConnectorCallback(
                                applicationId,
                                message.targetConnectorType,
                                context,
                                if (message.test) controller.botDefinition.testBehaviour else null,
                                locale,
                                action
                            )
                        )
                    )
                }
            }
        }
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        callback as RestConnectorCallback
        if (event is Action) {
            callback.actions.add(event)
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }

    override fun loadProfile(callback: ConnectorCallback, userId: PlayerId): UserPreferences {
        callback as RestConnectorCallback
        //register user as test user if applicable
        return UserPreferences().apply {
            locale = callback.locale
            callback.testContext?.setup(this, callback.connectorType, locale)
        }
    }

    private fun getTargetConnector(targetConnectorType: ConnectorType): Connector? =
        BotRepository.getController { it.connectorType == targetConnectorType }?.connector


    override fun addSuggestions(text: CharSequence, suggestions: List<CharSequence>): BotBus.() -> ConnectorMessage? = {
        getTargetConnector(targetConnectorType)?.addSuggestions(text, suggestions)?.invoke(this)
    }

    override fun addSuggestions(message: ConnectorMessage, suggestions: List<CharSequence>): BotBus.() -> ConnectorMessage? = {
        getTargetConnector(targetConnectorType)?.addSuggestions(message, suggestions)?.invoke(this)
    }


    override fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> = {
        getTargetConnector(targetConnectorType)?.toConnectorMessage(message)?.invoke(this) ?: emptyList()
    }

}
