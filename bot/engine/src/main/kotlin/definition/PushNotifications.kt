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

package ai.tock.bot.definition

import ai.tock.bot.connector.ConnectorException
import ai.tock.bot.connector.NotifyBotStateModifier
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.event.SkippedEventException
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicReference

/**
 * Sends a notification to a connector.
 * A [Bus] is created and the corresponding story is called.
 *
 * @param applicationId the configuration connector id
 * @param namespace the configuration namespace
 * @param botId the configuration botId
 * @param applicationId the configuration connector id
 * @param recipientId the recipient identifier
 * @param intent the notification intent
 * @param step the optional step target
 * @param parameters the optional parameters
 * @param stateModifier allow the notification to bypass current user state
 * @param notificationType the notification type if any
 * @param errorListener called when a message has not been delivered
 */
fun notify(
    applicationId: String,
    namespace: String,
    botId: String,
    recipientId: PlayerId,
    intent: IntentAware,
    step: StoryStepDef? = null,
    parameters: Parameters = Parameters.EMPTY,
    stateModifier: NotifyBotStateModifier = NotifyBotStateModifier.KEEP_CURRENT_STATE,
    notificationType: ActionNotificationType? = null,
    errorListener: (Throwable) -> Unit = {},
) {
    runBlocking {
        BotRepository.notifyAsync(
            namespace = namespace,
            botId = botId,
            applicationId = applicationId,
            recipientId = recipientId,
            intent = intent,
            step = step,
            parameters = parameters.toMap(),
            stateModifier = stateModifier,
            notificationType = notificationType,
            errorListener = errorListener,
        )
    }
}

/**
 * Sends a notification to a connector.
 * A [ai.tock.bot.engine.Bus] is created and the corresponding story is called.
 *
 * @param connectorId the configuration connector id
 * @param recipientId the recipient identifier
 * @param intent the notification intent
 * @param step the optional step target
 * @param parameters the optional parameters
 * @param stateModifier allow the notification to bypass current user state
 * @param notificationType the notification type if any
 * @throws SkippedEventException if a concurrent request prevents processing of the pushed event
 * @throws ConnectorException
 */
@ExperimentalTockCoroutines
suspend fun BotDefinition.pushNotification(
    connectorId: String,
    recipientId: PlayerId,
    intent: IntentAware,
    step: StoryStepDef? = null,
    parameters: Parameters = Parameters.EMPTY,
    stateModifier: NotifyBotStateModifier = NotifyBotStateModifier.KEEP_CURRENT_STATE,
    notificationType: ActionNotificationType? = null,
) {
    val throwable = AtomicReference<Throwable?>(null)
    BotRepository.notifyAsync(
        applicationId = connectorId,
        recipientId = recipientId,
        intent = intent,
        step = step,
        parameters = parameters.toMap(),
        stateModifier = stateModifier,
        notificationType = notificationType,
        namespace = namespace,
        botId = botId,
        errorListener = throwable::set,
    )
    throwable.get()?.let { throw it }
}
