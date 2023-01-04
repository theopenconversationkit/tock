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

package ai.tock.bot.bean.unknown

/**
 * UnknownHandlingStep represents a step while a unknown intent is being handled
 * @param repeated the number of repetition of this step
 * @param answerConfig the unknown answer configuration attached to this step
 */
data class UnknownHandlingStep(
    val repeated: Int = 1,
    val answerConfig: UnknownAnswerConfig
){
    fun increment(): UnknownHandlingStep = this.copy(repeated = repeated + 1)
}