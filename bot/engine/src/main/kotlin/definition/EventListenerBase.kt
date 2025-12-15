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

import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.event.EndConversationEvent
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.event.FeedbackEvent
import ai.tock.bot.engine.event.NoInputEvent
import ai.tock.bot.engine.event.OneToOneEvent
import ai.tock.bot.engine.event.PassThreadControlEvent
import ai.tock.bot.engine.event.StartConversationEvent
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

/**
 * Base implementation of [EventListener].
 */
open class EventListenerBase : EventListener {
    private val logger = KotlinLogging.logger {}

    /**
     * Listen [StartConversationEvent] or [EndConversationEvent] by default
     * (if respectively [BotDefinition.helloStory] or [BotDefinition.goodbyeStory] are set).
     */
    override fun listenEvent(
        controller: ConnectorController,
        connectorData: ConnectorData,
        event: Event,
    ): Boolean {
        logger.debug { "listen event $event" }

        fun StoryDefinition?.sendChoice(
            event: OneToOneEvent,
            force: Boolean = false,
        ): Boolean =
            if (this == null && !force) {
                false
            } else {
                sendChoice(
                    event,
                    this?.mainIntent()
                        ?: controller.botDefinition.defaultStory.mainIntent(),
                    controller,
                    connectorData,
                )
                true
            }

        with(controller.botDefinition) {
            return when (event) {
                is StartConversationEvent -> helloStory.sendChoice(event, true)
                is EndConversationEvent -> goodbyeStory.sendChoice(event)
                is NoInputEvent -> goodbyeStory.sendChoice(event)
                is FeedbackEvent -> applyFeedback(controller, event)
                is PassThreadControlEvent -> passThreadControlEventListener(controller, connectorData, event)
                else -> false
            }
        }
    }

    /**
     * Applies feedback from a [FeedbackEvent] to the corresponding action in the user's timeline.
     *
     * This function attempts to find the action with the given `actionId` in the last valid dialog
     * of the user's timeline. If the action is found, it updates its metadata with the provided
     * feedback and saves the updated timeline. If the action is not found, a warning is logged.
     *
     * @param controller the [ConnectorController] containing bot definitions and story loaders.
     * @param event the [FeedbackEvent] carrying the user ID, action ID, and feedback to apply.
     */
    protected open fun applyFeedback(
        controller: ConnectorController,
        event: FeedbackEvent,
    ): Boolean {
        with(controller.botDefinition) {
            val userTimelineDAO: UserTimelineDAO by injector.instance()
            runBlocking {
                val timeline =
                    userTimelineDAO.loadWithLastValidDialog(
                        namespace,
                        event.userId,
                        storyDefinitionProvider = controller.storyDefinitionLoader(event.connectorId),
                    )

                val action =
                    timeline.currentDialog
                        ?.allActions()
                        ?.firstOrNull { it.id.toString() == event.actionId && PlayerType.bot == it.playerId.type }

                if (action != null) {
                    action.metadata.feedback = event.feedback
                    userTimelineDAO.save(timeline, controller.botDefinition)
                } else {
                    logger.warn("Feedback ignored: no action found with id ${event.actionId} in the current dialog.")
                }
            }
            return false
        }
    }

    protected open fun passThreadControlEventListener(
        controller: ConnectorController,
        connectorData: ConnectorData,
        event: PassThreadControlEvent,
    ): Boolean {
        with(controller.botDefinition) {
            val userTimelineDAO: UserTimelineDAO by injector.instance()
            runBlocking {
                val timeline = userTimelineDAO.loadWithoutDialogs(namespace, event.userId)
                timeline.userState.botDisabled = false
                userTimelineDAO.save(timeline, controller.botDefinition)
            }
            return true
        }
    }

    protected fun sendChoice(
        event: OneToOneEvent,
        intent: IntentAware,
        controller: ConnectorController,
        connectorData: ConnectorData,
    ) {
        controller.handle(
            SendChoice(
                event.userId,
                event.applicationId,
                event.recipientId,
                intent.wrappedIntent().name,
                state = event.state,
            ),
            connectorData,
        )
    }
}
