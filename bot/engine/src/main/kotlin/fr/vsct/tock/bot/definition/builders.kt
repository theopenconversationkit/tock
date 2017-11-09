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

import fr.vsct.tock.bot.definition.BotDefinition.Companion.findStoryDefinition
import fr.vsct.tock.bot.definition.BotDefinitionBase.Companion.defaultKeywordStory
import fr.vsct.tock.bot.definition.BotDefinitionBase.Companion.defaultUnknownStory
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.translator.UserInterfaceType
import kotlin.reflect.KClass


/**
 * Create a new bot.
 */
fun bot(
        botId: String,
        stories: List<StoryDefinition>,
        namespace: String = botId,
        nlpModelName: String = botId,
        unknownStory: StoryDefinition = defaultUnknownStory,
        hello: IntentAware? = null,
        goodbye: IntentAware? = null,
        noInput: IntentAware? = null,
        botDisabled: IntentAware? = null,
        botEnabled: IntentAware? = null,
        userLocation: IntentAware? = null,
        handleAttachment: IntentAware? = null,
        eventListener: EventListener = EventListenerBase(),
        keywordStory: StoryDefinition = defaultKeywordStory)
        : BotDefinitionBase {

    fun findStory(intent: IntentAware?): StoryDefinition?
            = findStoryDefinition(stories, intent?.wrappedIntent()?.name, unknownStory, keywordStory)
            .let {
                if (it == unknownStory || it == keywordStory) {
                    null
                } else {
                    it
                }
            }


    return BotDefinitionBase(
            botId,
            namespace,
            stories,
            nlpModelName,
            unknownStory,
            findStory(hello),
            findStory(goodbye),
            findStory(noInput),
            findStory(botDisabled),
            findStory(botEnabled),
            findStory(userLocation),
            findStory(handleAttachment),
            eventListener,
            keywordStory
    )
}

/**
 * Create a new story.
 */
fun story(
        handler: SimpleStoryHandlerBase,
        otherStarterIntents: Set<IntentAware> = emptySet(),
        secondaryIntents: Set<IntentAware> = emptySet(),
        steps: List<StoryStep<out StoryHandlerDefinition>> = emptyList(),
        unsupportedUserInterface: UserInterfaceType? = null)
        : StoryDefinitionBase =
        StoryDefinitionBase(
                handler.wrappedIntent().name,
                handler,
                otherStarterIntents,
                secondaryIntents,
                steps,
                unsupportedUserInterface
        )

/**
 * Create a new story.
 */
fun story(
        intentName: String,
        otherStarterIntents: Set<IntentAware> = emptySet(),
        secondaryIntents: Set<IntentAware> = emptySet(),
        steps: List<StoryStep<out StoryHandlerDefinition>> = emptyList(),
        unsupportedUserInterface: UserInterfaceType? = null,
        handler: (BotBus) -> Unit)
        : StoryDefinitionBase =
        StoryDefinitionBase(
                intentName,
                object : SimpleStoryHandlerBase(intentName) {
                    override fun action(bus: BotBus) = handler.invoke(bus)
                },
                otherStarterIntents,
                secondaryIntents,
                steps,
                unsupportedUserInterface
        )

/**
 * Create a new story.
 */
inline fun <reified T : StoryHandlerDefinition> story(
        handler: StoryHandlerBase<T>,
        otherStarterIntents: Set<IntentAware> = emptySet(),
        secondaryIntents: Set<IntentAware> = emptySet(),
        steps: List<StoryStep<out StoryHandlerDefinition>> = emptyList(),
        unsupportedUserInterface: UserInterfaceType? = null)
        : StoryDefinitionBase =
        StoryDefinitionBase(
                handler.wrappedIntent().name,
                handler,
                otherStarterIntents,
                secondaryIntents,
                steps,
                unsupportedUserInterface
        )

/**
 * Create a new story.
 */
inline fun <reified T : StoryHandlerDefinition> story(
        intentName: String,
        otherStarterIntents: Set<IntentAware> = emptySet(),
        secondaryIntents: Set<IntentAware> = emptySet(),
        steps: List<StoryStep<out StoryHandlerDefinition>> = emptyList(),
        unsupportedUserInterface: UserInterfaceType? = null,
        //parameter added to bypass compiler limitation
        defClass: KClass<T> = T::class,
        crossinline handlerDefGenerator: (BotBus) -> T?
)
        : StoryDefinitionBase =
        StoryDefinitionBase(intentName,
                object : StoryHandlerBase<T>(intentName) {
                    override fun setupHandlerDef(bus: BotBus): T? = handlerDefGenerator.invoke(bus)
                },
                otherStarterIntents,
                secondaryIntents,
                steps,
                unsupportedUserInterface
        )

/**
 * Create a new story from a [StoryHandler].
 */
fun story(
        intent: IntentAware,
        storyHandler: StoryHandler,
        otherStarterIntents: Set<IntentAware> = emptySet(),
        secondaryIntents: Set<IntentAware> = emptySet(),
        steps: List<StoryStep<out StoryHandlerDefinition>> = emptyList(),
        unsupportedUserInterface: UserInterfaceType? = null):
        StoryDefinitionBase =
        StoryDefinitionBase(
                intent.wrappedIntent().name,
                storyHandler,
                otherStarterIntents,
                secondaryIntents,
                steps,
                unsupportedUserInterface)

/**
 * Create a new story from a [StoryHandlerBase].
 */
inline fun <reified T> storyWithSteps(
        handler: StoryHandlerBase<*>,
        otherStarterIntents: Set<IntentAware> = emptySet(),
        secondaryIntents: Set<IntentAware> = emptySet(),
        unsupportedUserInterface: UserInterfaceType? = null)
        : StoryDefinitionBase
        where T : Enum<T>, T : StoryStep<out StoryHandlerDefinition> =
        story(
                handler,
                handler,
                otherStarterIntents,
                secondaryIntents,
                enumValues<T>().toList(),
                unsupportedUserInterface
        )

/**
 * Create a new story from a [StoryHandler].
 */
inline fun <reified T> storyWithSteps(
        intent: IntentAware,
        storyHandler: StoryHandler,
        otherStarterIntents: Set<IntentAware> = emptySet(),
        secondaryIntents: Set<IntentAware> = emptySet(),
        unsupportedUserInterface: UserInterfaceType? = null)
        : StoryDefinitionBase  where T : Enum<T>, T : StoryStep<out StoryHandlerDefinition> =
        story(
                intent,
                storyHandler,
                otherStarterIntents,
                secondaryIntents,
                enumValues<T>().toList(),
                unsupportedUserInterface
        )

