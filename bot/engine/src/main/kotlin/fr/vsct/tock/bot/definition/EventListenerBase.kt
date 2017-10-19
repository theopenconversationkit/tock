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

import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.event.EndConversationEvent
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.event.NoInputEvent
import fr.vsct.tock.bot.engine.event.OneToOneEvent
import fr.vsct.tock.bot.engine.event.StartConversationEvent
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
    override fun listenEvent(controller: ConnectorController, event: Event): Boolean {
        logger.debug { "listen event $event" }

        fun StoryDefinition?.sendChoice(event: OneToOneEvent): Boolean =
                if (this == null) {
                    false
                } else {
                    controller.handle(
                            SendChoice(
                                    event.userId,
                                    event.applicationId,
                                    event.recipientId,
                                    mainIntent().name,
                                    state = event.state
                            )
                    )
                    true
                }

        with(controller.botDefinition) {
            return when (event) {
                is StartConversationEvent -> helloStory.sendChoice(event)
                is EndConversationEvent -> goodbyeStory.sendChoice(event)
                is NoInputEvent -> goodbyeStory.sendChoice(event)
                else -> false
            }
        }
    }


}