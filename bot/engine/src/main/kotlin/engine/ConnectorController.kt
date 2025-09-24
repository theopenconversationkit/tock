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
import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import io.vertx.ext.web.Router

/**
 * Controller to connect [Connector] and [BotDefinition].
 */
interface ConnectorController {

    /**
     * The bot definition served by the controller.
     */
    val botDefinition: BotDefinition

    /**
     * The connector used by the controller.
     */
    val connector: Connector

    /**
     * The type of connector used by the controller.
     */
    val connectorType: ConnectorType get() = connector.connectorType

    val botConfiguration: BotApplicationConfiguration

    /**
     * Sends a notification to the connector.
     * A [BotBus] is created and the corresponding story is called.
     *
     * @param recipientId the recipient identifier
     * @param intent the notification intent
     * @param step the optional step target
     * @param parameters the optional parameters
     * @param notificationType notification type if any
     * @param errorListener called when a message has not been delivered
     */
    fun notify(
        recipientId: PlayerId,
        intent: IntentAware,
        step: StoryStepDef? = null,
        parameters: Map<String, String> = emptyMap(),
        notificationType: ActionNotificationType?,
        errorListener: (Throwable) -> Unit = {}
    ) {
        connector.notify(this, recipientId, intent, step, parameters, notificationType, errorListener)
    }

    /**
     * Handles an event sent by the connector. the primary goal of this controller.
     *
     * This method may return before the event is actually processed.
     *
     * @param event the event to handle
     * @param data the optional additional data from the connector
     */
    fun handle(
        event: Event,
        data: ConnectorData = ConnectorData(ConnectorCallbackBase(event.applicationId, connector.connectorType))
    )

    /**
     * Return a probability of the support by the bot of this action
     * - by default returns the nlp intent probability.
     *
     * @return a probability between 0.0 (not supported) and 1.0 (supported!)
     */
    fun support(
        action: Action,
        data: ConnectorData = ConnectorData(ConnectorCallbackBase(action.applicationId, connector.connectorType))
    ): Double

    /**
     * Register services at startup.
     */
    fun registerServices(serviceIdentifier: String, installer: (Router) -> Unit)

    /**
     * Unregister services when [Connector] is unregistered.
     */
    fun unregisterServices()

    /**
     * Returns an error message (technical error).
     */
    fun errorMessage(playerId: PlayerId, applicationId: String, recipientId: PlayerId): Action {
        val errorAction = botDefinition.errorAction(playerId, applicationId, recipientId)
        errorAction.metadata.lastAnswer = true
        return errorAction
    }

    /**
     * Return a story definition provider for this controller.
     */
    fun storyDefinitionLoader(applicationId: String): (String) -> StoryDefinition = {
        botDefinition.findStoryDefinitionById(it, applicationId)
    }
}
