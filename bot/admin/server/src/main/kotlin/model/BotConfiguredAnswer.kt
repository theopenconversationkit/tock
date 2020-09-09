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

package ai.tock.bot.admin.model

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.definition.ConfiguredAnswer
import ai.tock.bot.definition.ConfiguredAnswer.ConfiguredBuiltinAnswer
import ai.tock.bot.definition.ConfiguredAnswer.ConfiguredScriptAnswer
import ai.tock.bot.definition.ConfiguredAnswer.ConfiguredSimpleAnswer
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 *
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "answerType"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = BotSimpleConfiguredAnswer::class, name = "0"),
    JsonSubTypes.Type(value = BotSimpleConfiguredAnswer::class, name = "simple"),
    JsonSubTypes.Type(value = BotScriptConfiguredAnswer::class, name = "2"),
    JsonSubTypes.Type(value = BotScriptConfiguredAnswer::class, name = "script"),
    JsonSubTypes.Type(value = BotBuiltinConfiguredAnswer::class, name = "3"),
    JsonSubTypes.Type(value = BotBuiltinConfiguredAnswer::class, name = "builtin")
)
abstract class BotConfiguredAnswer(
    open val botConfiguration: String,
    val answerType: AnswerConfigurationType,
    open val answers: List<BotAnswerConfiguration>
)

data class BotBuiltinConfiguredAnswer(
    override val botConfiguration: String,
    override val answers: List<BotAnswerConfiguration>
) : BotConfiguredAnswer(botConfiguration, AnswerConfigurationType.builtin, answers) {

    constructor(configuredAnswer: ConfiguredBuiltinAnswer)
        : this(configuredAnswer.botConfiguration, configuredAnswer.answers.mapAnswers())
}

data class BotScriptConfiguredAnswer(
    override val botConfiguration: String,
    override val answers: List<BotAnswerConfiguration>
) : BotConfiguredAnswer(botConfiguration, AnswerConfigurationType.script, answers) {
    constructor(configuredAnswer: ConfiguredScriptAnswer)
        : this(configuredAnswer.botConfiguration, configuredAnswer.answers.mapAnswers())
}

data class BotSimpleConfiguredAnswer(
    override val botConfiguration: String,
    override val answers: List<BotAnswerConfiguration>
) : BotConfiguredAnswer(botConfiguration, AnswerConfigurationType.simple, answers) {
    constructor(configuredAnswer: ConfiguredSimpleAnswer)
        : this(configuredAnswer.botConfiguration, configuredAnswer.answers.mapAnswers())
}

fun List<ConfiguredAnswer>.mapAnswers(): List<BotConfiguredAnswer> =
    map {
        when (it) {
            is ConfiguredSimpleAnswer -> BotSimpleConfiguredAnswer(it)
            is ConfiguredScriptAnswer -> BotScriptConfiguredAnswer(it)
            is ConfiguredBuiltinAnswer -> BotBuiltinConfiguredAnswer(it)
            else -> error("unsupported conf $it")
        }
    }

