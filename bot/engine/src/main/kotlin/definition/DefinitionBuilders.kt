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

import ai.tock.bot.connector.NotifyBotStateModifier
import ai.tock.bot.definition.BotDefinition.Companion.findStoryDefinition
import ai.tock.bot.definition.BotDefinitionBase.Companion.defaultKeywordStory
import ai.tock.bot.definition.BotDefinitionBase.Companion.defaultUnknownStory
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.user.PlayerId
import ai.tock.translator.UserInterfaceType

/**
 * Creates a new bot.
 */
fun bot(
    /**
     * The (unique) bot identifier.
     */
    botId: String,
    /**
     * List of stories supported by the bot.
     */
    stories: List<StoryDefinition>,
    /**
     * The namespace of the app.
     */
    namespace: String = "app",
    /**
     * The NLP model name used - default is [botId].
     */
    nlpModelName: String = botId,
    /**
     * The story used when the bot does not know the answer.
     */
    unknownStory: StoryDefinition = defaultUnknownStory,
    /**
     * The story used to handle first user request, where no intent is defined.
     * If null, first item of [stories] is used.
     */
    hello: IntentAware? = null,
    /**
     * The story used to handle exit intent
     */
    goodbye: IntentAware? = null,
    /**
     * The story used when there is not input from the user after an significant amount of time.
     */
    noInput: IntentAware? = null,
    /**
     * The intent used to *disable* the bot.
     */
    botDisabled: IntentAware? = null,
    /**
     * The intent used to *enable* the bot.
     */
    botEnabled: IntentAware? = null,
    /**
     * The intent used to specify user location.
     */
    userLocation: IntentAware? = null,
    /**
     * The intent use to handle attachment sent by the user.
     */
    handleAttachment: IntentAware? = null,
    /**
     * The [EventListener] of the bot.
     */
    eventListener: EventListener = EventListenerBase(),
    /**
     * To handle keywords.
     */
    keywordStory: StoryDefinition = defaultKeywordStory,
    /**
     * The optional dialog flow.
     */
    conversation: DialogFlowDefinition? = null,
    /**
     * Listener invoked when bot is enabled.
     */
    botEnabledListener: (Action) -> Unit = {}
): SimpleBotDefinition {
    fun findStory(intent: IntentAware?): StoryDefinition? =
        intent as? StoryDefinition
            ?: findStoryDefinition(
                stories,
                intent?.wrappedIntent()?.name,
                unknownStory,
                keywordStory
            ).let {
                if (it == unknownStory || it == keywordStory) {
                    null
                } else {
                    it
                }
            }

    return SimpleBotDefinition(
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
        keywordStory,
        conversation,
        botEnabledListener,
    )
}

/**
 * Creates a new story.
 */
fun story(
    /**
     * A simple handler for the story. Defines also implicitly the [StoryDefinition.mainIntent].
     */
    handler: SimpleStoryHandlerBase,
    /**
     * The optionals other [StoryDefinition.starterIntents].
     */
    otherStarterIntents: Set<IntentAware> = emptySet(),
    /**
     * The others [StoryDefinition.intents] - ie the "secondary" intents.
     */
    secondaryIntents: Set<IntentAware> = emptySet(),
    /**
     * The [StoryStep] of the story if any.
     */
    steps: List<StoryStep<*>> = emptyList(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null
): StoryDefinitionBase =
    StoryDefinitionBase(
        handler.wrappedIntent().name,
        handler,
        otherStarterIntents,
        secondaryIntents,
        steps,
        unsupportedUserInterface
    )

/**
 * Creates a new story.
 */
fun story(
    /**
     * The [StoryDefinition.mainIntent] name.
     */
    intentName: String,
    /**
     * The optionals other [StoryDefinition.starterIntents].
     */
    otherStarterIntents: Set<IntentAware> = emptySet(),
    /**
     * The others [StoryDefinition.intents] - ie the "secondary" intents.
     */
    secondaryIntents: Set<IntentAware> = emptySet(),
    /**
     * The [StoryStep] of the story if any.
     */
    steps: List<StoryStep<*>> = emptyList(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null,
    /**
     * The handler for the story.
     */
    handler: (BotBus).() -> Unit
): StoryDefinitionBase =
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
 * Creates a new story.
 */
inline fun <reified T : StoryHandlerDefinition> story(
    /**
     * The handler for the story. Defines also implicitly the [StoryDefinition.mainIntent].
     */
    handler: StoryHandlerBase<T>,
    /**
     * The optionals other [StoryDefinition.starterIntents].
     */
    otherStarterIntents: Set<IntentAware> = emptySet(),
    /**
     * The others [StoryDefinition.intents] - ie the "secondary" intents.
     */
    secondaryIntents: Set<IntentAware> = emptySet(),
    /**
     * The [StoryStep] of the story if any.
     */
    steps: List<StoryStep<*>> = emptyList(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null
): StoryDefinitionBase =
    StoryDefinitionBase(
        handler.wrappedIntent().name,
        handler,
        otherStarterIntents,
        secondaryIntents,
        steps,
        unsupportedUserInterface
    )

/**
 * Creates a new story.
 */
@JvmName("storyDataDefWithSteps")
inline fun <reified T : StoryHandlerDefinition, D> storyDef(
    /**
     * The [StoryDefinition.mainIntent] name.
     */
    intentName: String,
    /**
     * The optionals other [StoryDefinition.starterIntents].
     */
    otherStarterIntents: Set<IntentAware> = emptySet(),
    /**
     * The others [StoryDefinition.intents] - ie the "secondary" intents.
     */
    secondaryIntents: Set<IntentAware> = emptySet(),
    /**
     * The [StoryStep] of the story if any.
     */
    steps: List<StoryStep<*>> = emptyList(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null,
    /**
     * The [HandlerDef] creator. Defines [StoryHandlerBase.newHandlerDefinition].
     */
    handlerDefCreator: HandlerStoryDefinitionCreator<T> = defaultHandlerStoryDefinitionCreator(),
    /**
     * Check preconditions. if [BotBus.end] is called in this function,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    noinline preconditionsChecker: BotBus.() -> D
): StoryDefinitionBase =
    StoryDefinitionBase(
        intentName,
        ConfigurableStoryHandler(intentName, handlerDefCreator, preconditionsChecker),
        otherStarterIntents,
        secondaryIntents,
        steps,
        unsupportedUserInterface
    )

/**
 * Creates a new story.
 */
inline fun <reified T : StoryHandlerDefinition> storyDef(
    /**
     * The [StoryDefinition.mainIntent] name.
     */
    intentName: String,
    /**
     * The optionals other [StoryDefinition.starterIntents].
     */
    otherStarterIntents: Set<IntentAware> = emptySet(),
    /**
     * The others [StoryDefinition.intents] - ie the "secondary" intents.
     */
    secondaryIntents: Set<IntentAware> = emptySet(),
    /**
     * The [StoryStep] of the story if any.
     */
    steps: List<StoryStep<*>> = emptyList(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null,
    /**
     * The [HandlerDef] creator. Defines [StoryHandlerBase.newHandlerDefinition].
     */
    handlerDefCreator: HandlerStoryDefinitionCreator<T> = defaultHandlerStoryDefinitionCreator(),
    /**
     * Check preconditions. if [BotBus.end] is called in this function,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    noinline preconditionsChecker: BotBus.() -> Unit
): StoryDefinitionBase =
    StoryDefinitionBase(
        intentName,
        ConfigurableStoryHandler(intentName, handlerDefCreator, preconditionsChecker),
        otherStarterIntents,
        secondaryIntents,
        steps,
        unsupportedUserInterface
    )

/**
 * Creates a new story.
 */
@JvmName("storyDataDefWithSteps")
inline fun <reified T : StoryHandlerDefinition, reified S, D> storyDefWithSteps(
    /**
     * The [StoryDefinition.mainIntent] name.
     */
    intentName: String,
    /**
     * The optionals other [StoryDefinition.starterIntents].
     */
    otherStarterIntents: Set<IntentAware> = emptySet(),
    /**
     * The others [StoryDefinition.intents] - ie the "secondary" intents.
     */
    secondaryIntents: Set<IntentAware> = emptySet(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null,
    /**
     * The [HandlerDef] creator. Defines [StoryHandlerBase.newHandlerDefinition].
     */
    handlerDefCreator: HandlerStoryDefinitionCreator<T> = defaultHandlerStoryDefinitionCreator(),
    /**
     * Check preconditions. if [BotBus.end] is called in this function,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    noinline preconditionsChecker: BotBus.() -> D
): StoryDefinitionBase where S : Enum<S>, S : StoryStep<*> =
    StoryDefinitionBase(
        intentName,
        ConfigurableStoryHandler(intentName, handlerDefCreator, preconditionsChecker),
        otherStarterIntents,
        secondaryIntents,
        enumValues<S>().toList(),
        unsupportedUserInterface
    )

/**
 * Creates a new story.
 */
inline fun <reified T : StoryHandlerDefinition, reified S> storyDefWithSteps(
    /**
     * The [StoryDefinition.mainIntent] name.
     */
    intentName: String,
    /**
     * The optionals other [StoryDefinition.starterIntents].
     */
    otherStarterIntents: Set<IntentAware> = emptySet(),
    /**
     * The others [StoryDefinition.intents] - ie the "secondary" intents.
     */
    secondaryIntents: Set<IntentAware> = emptySet(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null,
    /**
     * The [HandlerDef] creator. Defines [StoryHandlerBase.newHandlerDefinition].
     */
    handlerDefCreator: HandlerStoryDefinitionCreator<T> = defaultHandlerStoryDefinitionCreator(),
    /**
     * Check preconditions. if [BotBus.end] is called in this function,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    noinline preconditionsChecker: BotBus.() -> Unit
): StoryDefinitionBase where S : Enum<S>, S : StoryStep<*> =
    StoryDefinitionBase(
        intentName,
        ConfigurableStoryHandler(intentName, handlerDefCreator, preconditionsChecker),
        otherStarterIntents,
        secondaryIntents,
        enumValues<S>().toList(),
        unsupportedUserInterface
    )

/**
 * Creates a new story from a [StoryHandler].
 */
fun story(
    /**
     * The [StoryDefinition.mainIntent].
     */
    intent: IntentAware,
    /**
     * The handler of the story.
     */
    storyHandler: StoryHandler,
    /**
     * The optionals other [StoryDefinition.starterIntents].
     */
    otherStarterIntents: Set<IntentAware> = emptySet(),
    /**
     * The others [StoryDefinition.intents] - ie the "secondary" intents.
     */
    secondaryIntents: Set<IntentAware> = emptySet(),
    /**
     * The [StoryStep] of the story if any.
     */
    steps: List<StoryStep<*>> = emptyList(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null
): StoryDefinitionBase =
    StoryDefinitionBase(
        intent.wrappedIntent().name,
        storyHandler,
        otherStarterIntents,
        secondaryIntents,
        steps,
        unsupportedUserInterface
    )

/**
 * Creates a new story from a [StoryHandlerBase].
 */
inline fun <reified T> storyWithSteps(
    /**
     * The handler for the story. Defines also implicitly the [StoryDefinition.mainIntent].
     */
    handler: StoryHandlerBase<*>,
    /**
     * The optionals other [StoryDefinition.starterIntents].
     */
    otherStarterIntents: Set<IntentAware> = emptySet(),
    /**
     * The others [StoryDefinition.intents] - ie the "secondary" intents.
     */
    secondaryIntents: Set<IntentAware> = emptySet(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null
): StoryDefinitionBase
        where T : Enum<T>, T : StoryStep<*> =
    story(
        handler,
        handler,
        otherStarterIntents,
        secondaryIntents,
        enumValues<T>().toList(),
        unsupportedUserInterface
    )

/**
 * Creates a new story from a [StoryHandler].
 */
inline fun <reified T> storyWithSteps(
    /**
     * The [StoryDefinition.mainIntent].
     */
    intent: IntentAware,
    /**
     * The handler of the story.
     */
    storyHandler: StoryHandler,
    /**
     * The optionals other [StoryDefinition.starterIntents].
     */
    otherStarterIntents: Set<IntentAware> = emptySet(),
    /**
     * The others [StoryDefinition.intents] - ie the "secondary" intents.
     */
    secondaryIntents: Set<IntentAware> = emptySet(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null
): StoryDefinitionBase where T : Enum<T>, T : StoryStep<*> =
    story(
        intent,
        storyHandler,
        otherStarterIntents,
        secondaryIntents,
        enumValues<T>().toList(),
        unsupportedUserInterface
    )

/**
 * Creates a new story.
 */
inline fun <reified T> storyWithSteps(
    /**
     * The [StoryDefinition.mainIntent] name.
     */
    intentName: String,
    /**
     * The optionals other [StoryDefinition.starterIntents].
     */
    otherStarterIntents: Set<IntentAware> = emptySet(),
    /**
     * The others [StoryDefinition.intents] - ie the "secondary" intents.
     */
    secondaryIntents: Set<IntentAware> = emptySet(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null,
    /**
     * The handler for the story.
     */
    noinline handler: (BotBus).() -> Unit
): StoryDefinitionBase where T : Enum<T>, T : StoryStep<*> =
    story(
        intentName,
        otherStarterIntents,
        secondaryIntents,
        enumValues<T>().toList(),
        unsupportedUserInterface,
        handler
    )

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
    step: StoryStep<*>? = null,
    parameters: Parameters = Parameters.EMPTY,
    stateModifier: NotifyBotStateModifier = NotifyBotStateModifier.KEEP_CURRENT_STATE,
    notificationType: ActionNotificationType? = null,
    errorListener: (Throwable) -> Unit = {}
) = BotRepository.notify(
    applicationId = applicationId,
    recipientId = recipientId,
    intent = intent,
    step = step,
    parameters = parameters.toMap(),
    stateModifier = stateModifier,
    notificationType = notificationType,
    namespace = namespace,
    botId = botId,
    errorListener = errorListener
)
