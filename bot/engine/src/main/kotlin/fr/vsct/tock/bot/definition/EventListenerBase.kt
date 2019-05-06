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

package fr.vsct.tock.bot.definition

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.connector.ConnectorData
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.event.EndConversationEvent
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.event.NoInputEvent
import fr.vsct.tock.bot.engine.event.OneToOneEvent
import fr.vsct.tock.bot.engine.event.PassThreadControlEvent
import fr.vsct.tock.bot.engine.event.StartConversationEvent
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.injector
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
    override fun listenEvent(controller: ConnectorController, connectorData: ConnectorData, event: Event): Boolean {
        logger.debug { "listen event $event" }

        fun StoryDefinition?.sendChoice(event: OneToOneEvent, force: Boolean = false): Boolean =
                if (this == null && !force) {
                    false
                } else {
                    controller.handle(
                            SendChoice(
                                    event.userId,
                                    event.applicationId,
                                    event.recipientId,
                                    (this?.mainIntent() ?: controller.botDefinition.stories.first().mainIntent()).name,
                                    state = event.state
                            ),
                            connectorData
                    )
                    true
                }

        with(controller.botDefinition) {
            return when (event) {
                is StartConversationEvent -> helloStory.sendChoice(event, true)
                is EndConversationEvent -> goodbyeStory.sendChoice(event)
                is NoInputEvent -> goodbyeStory.sendChoice(event)
                is PassThreadControlEvent -> {
                    val userTimelineDAO: UserTimelineDAO by injector.instance()
                    val timeline = userTimelineDAO.loadWithoutDialogs(event.userId)
                    timeline.userState.botDisabled = false
                    userTimelineDAO.save(timeline)
                    true
                }
                else -> false
            }
        }
    }


}