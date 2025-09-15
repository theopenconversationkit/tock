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

/**
 * An [AsyncStoryHandler] that checks preconditions and dispatches calls to [AsyncStoryStep] and [AsyncStoryHandling]
 */
@ExperimentalTockCoroutines
abstract class AsyncDelegatingStoryHandlerBase<T : AsyncStoryHandling, D>(
    mainIntent: Intent?,
) : AsyncStoryHandlerBase(mainIntent) {

    /**
     * Checks preconditions - if [AsyncBus.end] is called,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    abstract fun checkPreconditions(): suspend AsyncBus.() -> D

    /**
     * Selects step from [HandlerDef], optional data and [StoryDefinition].
     */
    @Suppress("UNCHECKED_CAST")
    open fun selectStepFromContext(
        bus: AsyncBus,
        context: T,
        preconditionResult: D,
        storyDefinition: StoryDefinition?,
    ): AsyncStoryStep<T>? {
        storyDefinition?.steps?.also { steps ->
            for (s in steps) {
                if (shouldSelectStep(s as AsyncStoryStep<T>, context, preconditionResult)) {
                    bus.step = s
                    break
                }
            }
        }
        return bus.step as AsyncStoryStep<T>?
    }

    protected open fun shouldSelectStep(
        s: AsyncStoryStep<T>,
        context: T,
        preconditionResult: D,
    ): Boolean {
        return with (context) {
            if (s is AsyncStoryDataStep<*, *, *>) {
                @Suppress("UNCHECKED_CAST")
                with(s as AsyncStoryDataStep<T, D, *>) {
                    selectFromContextAndData(preconditionResult)
                }
            } else {
                with(s) {
                    selectFromContext()
                }
            }
        }
    }

    override suspend fun action(bus: AsyncBus) {
        val preconditionResult = checkPreconditions().invoke(bus)
        if (!bus.isEndCalled()) {
            val storyDefinition = findStoryDefinition(bus)
            val handler: T = newHandlerDefinition(bus, preconditionResult)

            // final round of step selection (after Story.computeCurrentStep)
            val step = selectStepFromContext(bus, handler, preconditionResult, storyDefinition)

            if (step != null) {
                handler.handleStep(bus, step, preconditionResult)
            }

            if (!bus.isEndCalled()) {
                handler.handle()
            }
        }
    }

    abstract fun newHandlerDefinition(bus: AsyncBus, preconditionResult: D): T

    protected open suspend fun T.handleStep(bus: AsyncBus, step: AsyncStoryStep<T>, preconditionResult: D) {
        if (step is AsyncStoryDataStep<*, *, *>) {
            @Suppress("UNCHECKED_CAST")
            handleDataStep(
                step as AsyncStoryDataStep<T, D, *>,
                preconditionResult,
                bus
            )
        } else {
            with (step) {
                answer()
            }
        }
    }

    private suspend fun <TD> T.handleDataStep(
        step: AsyncStoryDataStep<T, D, TD>,
        preconditionResult: D,
        bus: AsyncBus
    ) {
        with (step) {
            val data = checkPreconditions(preconditionResult)

            if (!bus.isEndCalled()) {
                answer(data)
            }
        }
    }
}
