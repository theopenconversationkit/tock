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

import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.nlp.api.client.model.NlpEngineType

/**
 * Base implementation of [BaseDefinition].
 */
open class BotDefinitionBase(override val botId: String,
                             override val namespace: String,
                             override val stories: List<StoryDefinition>,
                             override val nlpModelName: String = botId,
                             override val engineType: NlpEngineType = NlpEngineType.opennlp,
                             override val unknownStory: StoryDefinition = defaultUnknownStory,
                             override val botDisabledStory: StoryDefinition? = null,
                             override val botEnabledStory: StoryDefinition? = null,
                             override val userLocationStory: StoryDefinition? = null) : BotDefinition {

    companion object {
        private val defaultUnknownStory =
                StoryDefinitionBase(
                        "tock_unknown_story",
                        object : StoryHandlerBase() {
                            override fun action(bus: BotBus) {
                                bus.end("Sorry, I didn't understand :(")
                            }
                        },
                        setOf(Intent.unknown))
    }
}