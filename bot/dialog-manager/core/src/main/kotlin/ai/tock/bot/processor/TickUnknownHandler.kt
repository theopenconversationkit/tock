/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.processor

import ai.tock.bot.bean.TickStorySettings
import ai.tock.bot.bean.UnknownHandlingStep
import ai.tock.bot.bean.unknown.ConfigMismatchedError
import ai.tock.bot.bean.unknown.RetryExceededError
import ai.tock.bot.bean.unknown.TickUnknownConfiguration
import ai.tock.bot.sender.TickSender

/**
 * Handler for an unknown intent detected
 */
object TickUnknownHandler {

    data class UnknownHandleResult(
        val handlingStep: UnknownHandlingStep? = null,
        val redirectStoryId: String? = null
    )

    fun handle(
        lastExecutedActionName: String,
        unknownConfiguration: TickUnknownConfiguration,
        sender: TickSender,
        unknownHandlingStep: UnknownHandlingStep?,
        storySettings: TickStorySettings
    ): UnknownHandleResult =
        /*
        Get the unknownAnswerConfig for the lastExecutedAction name
        */
        unknownConfiguration.unknownAnswerConfigs
            .firstOrNull { it.action == lastExecutedActionName }
            ?.let { answerConfig ->
                (
                        /*
                        If a not null unknownHandlingStep is provided
                        */
                        unknownHandlingStep?.let { step ->
                            /*
                            Check that the step (when it's not null) has an answerConfig
                            equal to the unknownAnswerConfig linked to the lastExecutedAction
                             */
                            if(step.answerConfig notEq answerConfig) throw ConfigMismatchedError()

                            /*
                            When the answer's retryNb is not exceeded,
                                increment the repeated property of the step by calling the next() method on it
                            Else set the step to null and return the redirectStoryId
                            */
                            if (storySettings.repetitionNb > step.repeated)
                                UnknownHandleResult(handlingStep = step.next() as UnknownHandlingStep)
                            else
                                storySettings.redirectStory?.let { UnknownHandleResult(redirectStoryId = it) } ?: throw RetryExceededError()

                        } ?:
                        /*
                        If no unknownHandlingStep has provided, create a new Step
                        */
                        UnknownHandleResult(handlingStep = UnknownHandlingStep(answerConfig =  answerConfig))
                        )
                    .also { result ->
                        result.handlingStep?.let { step -> sender.endById(step.answerConfig.answerId) }
                    }
            } ?: UnknownHandleResult()


}