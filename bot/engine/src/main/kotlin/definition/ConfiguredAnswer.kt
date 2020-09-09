/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType

sealed class ConfiguredAnswer(
    open val botConfiguration: String,
    val answerType: AnswerConfigurationType,
    open val answers: List<AnswerConfiguration>
) {

    data class ConfiguredBuiltinAnswer(
        override val botConfiguration: String,
        override val answers: List<AnswerConfiguration>
    ) : ConfiguredAnswer(botConfiguration, AnswerConfigurationType.builtin, answers)

    data class ConfiguredScriptAnswer(
        override val botConfiguration: String,
        override val answers: List<AnswerConfiguration>,
    ) : ConfiguredAnswer(botConfiguration, AnswerConfigurationType.script, answers)

    data class ConfiguredSimpleAnswer(
        override val botConfiguration: String,
        override val answers: List<AnswerConfiguration>
    ) : ConfiguredAnswer(botConfiguration, AnswerConfigurationType.simple, answers)

}