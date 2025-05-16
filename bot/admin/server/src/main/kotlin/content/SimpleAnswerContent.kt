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

package ai.tock.bot.admin.content

import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.model.BotMediaMessageDescriptor
import ai.tock.bot.admin.model.BotSimpleAnswer
import ai.tock.bot.admin.model.CreateI18nLabelRequest
import ai.tock.bot.engine.action.Footnote
import java.util.Locale

data class SimpleAnswerContent(
        val label: String,
        val delay: Long = 0,
        val mediaMessage: BotMediaMessageDescriptor? = null,
        val footnotes: List<Footnote>? = null
) {

    fun toBotSimpleAnswer(namespace: String, locale: Locale): BotSimpleAnswer =
            BotSimpleAnswer(
                    label = BotAdminService.createI18nRequest(
                            namespace,
                            CreateI18nLabelRequest(label, locale, AnswerConfigurationType.builtin.name)
                    ),
                    delay = delay,
                    mediaMessage = mediaMessage,
                    footnotes = footnotes
            )
}

