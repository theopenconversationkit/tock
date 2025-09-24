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

import ai.tock.bot.engine.AsyncBus
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.translator.UserInterfaceType


/**
 * Creates a new coroutine-based story.
 */
@JvmName("asyncStoryDataDefWithSteps")
@ExperimentalTockCoroutines
inline fun <reified T : AsyncStoryHandling, D> storyDef(
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
    steps: List<AsyncStoryDataStep<T, in D, *>> = emptyList(),
    /**
     * Steps that do not use data from the main story execution
     */
    simpleSteps: List<AsyncStoryStep<T>> = emptyList(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null,
    /**
     * The [HandlerDef] creator. Defines [StoryHandlerBase.newHandlerDefinition].
     */
    handling: AsyncStoryHandlingCreator<T, D>,
    /**
     * Check preconditions. if [AsyncBus.end] is called in this function,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    noinline preconditionsChecker: suspend AsyncBus.() -> D
): AsyncStoryDefinitionBase<AsyncStoryStep<T>> =
    AsyncStoryDefinitionBase(
        intentName,
        AsyncConfigurableStoryHandler(Intent(intentName), handling, preconditionsChecker),
        otherStarterIntents,
        secondaryIntents,
        steps + simpleSteps.onEach { check(it !is AsyncStoryDataStep<*, *, *>) {
            "Story data steps must be provided in the steps parameter, not simpleSteps"
        } },
        unsupportedUserInterface
    )

/**
 * Creates a new coroutine-based story.
 */
@ExperimentalTockCoroutines
@JvmName("asyncStoryDefWithSteps")
inline fun <reified T : AsyncStoryHandling> storyDef(
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
    steps: List<AsyncStoryStep<T>> = emptyList(),
    /**
     * Is this story unsupported for a [UserInterfaceType]?
     */
    unsupportedUserInterface: UserInterfaceType? = null,
    /**
     * The [HandlerDef] creator. Defines [StoryHandlerBase.newHandlerDefinition].
     */
    handling: SimpleAsyncStoryHandlingCreator<T>,
    /**
     * Check preconditions. if [AsyncBus.end] is called in this function,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    noinline preconditionsChecker: suspend AsyncBus.() -> Unit = {}
): AsyncStoryDefinitionBase<AsyncStoryStep<T>> =
    AsyncStoryDefinitionBase(
        intentName,
        AsyncConfigurableStoryHandler(Intent(intentName), handling, preconditionsChecker),
        otherStarterIntents,
        secondaryIntents,
        steps,
        unsupportedUserInterface
    )

/**
 * Creates a new coroutine-based story with steps defined in an enum class.
 */
@ExperimentalTockCoroutines
@JvmName("asyncStoryDataDefWithSteps")
inline fun <reified T : AsyncStoryHandling, reified S, D> storyDefWithSteps(
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
    handling: AsyncStoryHandlingCreator<T, D>,
    /**
     * Check preconditions. if [AsyncBus.end] is called in this function,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    noinline preconditionsChecker: suspend AsyncBus.() -> D
): AsyncStoryDefinitionBase<S> where S : Enum<S>, S : AsyncStoryStep<T> =
    AsyncStoryDefinitionBase(
        name = intentName,
        storyHandler = AsyncConfigurableStoryHandler(
            Intent(intentName), handling,
            preconditionsChecker
        ),
        otherStarterIntents = otherStarterIntents,
        secondaryIntents = secondaryIntents,
        stepsList = enumValues<S>().toList(),
        unsupportedUserInterface = unsupportedUserInterface
    )

/**
 * Creates a new coroutine-based story with steps defined in an enum class.
 */
@ExperimentalTockCoroutines
@JvmName("asyncStoryDefWithSteps")
inline fun <reified T : AsyncStoryHandling, reified S> storyDefWithSteps(
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
    handling: SimpleAsyncStoryHandlingCreator<T>,
    /**
     * Check preconditions. if [AsyncBus.end] is called in this function,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    noinline preconditionsChecker: suspend AsyncBus.() -> Unit = {}
): AsyncStoryDefinitionBase<S> where S : Enum<S>, S : AsyncStoryStep<T> =
    AsyncStoryDefinitionBase(
        name = intentName,
        storyHandler = AsyncConfigurableStoryHandler(
            Intent(intentName), handling,
            preconditionsChecker
        ),
        otherStarterIntents = otherStarterIntents,
        secondaryIntents = secondaryIntents,
        stepsList = enumValues<S>().toList(),
        unsupportedUserInterface = unsupportedUserInterface
    )
