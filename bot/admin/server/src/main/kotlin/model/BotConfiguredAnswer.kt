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

package ai.tock.bot.admin.model

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.DedicatedAnswerConfiguration
import java.util.Locale

/**
 *
 */
class BotConfiguredAnswer(
    val botConfiguration: String,
    val currentType: AnswerConfigurationType,
    val answers: List<BotAnswerConfiguration>
) {
    constructor(conf: DedicatedAnswerConfiguration, locale: Locale?, readOnly: Boolean = false) : this(
        conf.botConfiguration,
        conf.currentType,
        conf.answers.mapAnswers(locale, readOnly)
    )
}
