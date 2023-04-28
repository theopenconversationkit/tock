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

package ai.tock.bot.bean

import ai.tock.bot.bean.unknown.TickUnknownAnswerConfig
import mu.KotlinLogging

/**
 * TickHandlingStep represents a step
 * @param repeated the number of repetition of this step
 * @param key the step key
 */
sealed class TickHandlingStep(open val repeated: Int, val key: String) {
    private val logger = KotlinLogging.logger {}

    fun next(): TickHandlingStep {
        logger.debug { "increment number of repetition of $key ($repeated to ${repeated + 1})" }
        return when (this) {
            is TickActionHandlingStep -> this.copy(repeated = repeated + 1)
            is UnknownHandlingStep -> this.copy(repeated = repeated + 1)
        }
    }
}

/**
 * UnknownHandlingStep represents a step while an unknown intent is being handled
 * @param answerConfig the unknown answer configuration attached to this step
 */
data class UnknownHandlingStep(
    override val repeated: Int = 1,
    val answerConfig: TickUnknownAnswerConfig
) : TickHandlingStep(repeated, answerConfig.key())

/**
 * TickActionHandlingStep represents a step while an action is being handled
 * @param actionName the current action name
 */
data class TickActionHandlingStep(
    override val repeated: Int = 1,
    val actionName: String,
) : TickHandlingStep(repeated, actionName)
