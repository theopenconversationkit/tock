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

package ai.tock.bot.definition

/**
 * A simple [BotDefinition].
 */
class SimpleBotDefinition(
        botId: String,
        namespace: String,
        stories: List<StoryDefinition>,
        nlpModelName: String = botId,
        unknownStory: StoryDefinition = BotDefinitionBase.defaultUnknownStory,
        helloStory: StoryDefinition? = null,
        goodbyeStory: StoryDefinition? = null,
        noInputStory: StoryDefinition? = null,
        botDisabledStory: StoryDefinition? = null,
        botEnabledStory: StoryDefinition? = null,
        userLocationStory: StoryDefinition? = null,
        handleAttachmentStory: StoryDefinition? = null,
        eventListener: EventListener = EventListenerBase(),
        keywordStory: StoryDefinition = BotDefinitionBase.defaultKeywordStory,
        conversation: DialogFlowDefinition? = null
) :
        BotDefinitionBase(
                botId,
                namespace,
                stories,
                nlpModelName,
                unknownStory,
                helloStory,
                goodbyeStory,
                noInputStory,
                botDisabledStory,
                botEnabledStory,
                userLocationStory,
                handleAttachmentStory,
                eventListener,
                keywordStory,
                conversation
        ) {

    //set namespace for story handler
    init {
        (stories
                + listOfNotNull(
                unknownStory,
                helloStory,
                goodbyeStory,
                noInputStory,
                botDisabledStory,
                botEnabledStory,
                userLocationStory,
                handleAttachmentStory,
                keywordStory
        )).forEach {
            (it.storyHandler as? StoryHandlerBase<*>)?.apply {
                i18nNamespace = namespace
            }
        }
    }
}