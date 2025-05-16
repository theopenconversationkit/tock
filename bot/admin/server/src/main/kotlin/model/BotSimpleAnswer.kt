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

import ai.tock.bot.admin.answer.SimpleAnswer
import ai.tock.bot.engine.action.Footnote
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.Translator
import java.util.Locale

/**
 *
 */
data class BotSimpleAnswer(
    val label: I18nLabel,
    val delay: Long,
    val mediaMessage: BotMediaMessageDescriptor? = null,
    val footnotes: List<Footnote>? = null
) {

    constructor(answer: SimpleAnswer, locale: Locale?, readOnly: Boolean = false) :
        this(
            Translator.saveIfNotExist(answer.key, locale, readOnly),
            answer.delay,
            answer.mediaMessage?.let { BotMediaMessageDescriptor.fromDescriptor(it, readOnly) },
            answer.footnotes
        )

    fun toConfiguration(): SimpleAnswer =
        SimpleAnswer(I18nLabelValue(label), delay, mediaMessage?.toDescriptor(), footnotes)
}
