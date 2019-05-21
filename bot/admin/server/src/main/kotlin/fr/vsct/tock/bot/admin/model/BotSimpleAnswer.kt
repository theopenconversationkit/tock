/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.admin.model

import fr.vsct.tock.bot.admin.answer.SimpleAnswer
import fr.vsct.tock.translator.I18nLabel
import fr.vsct.tock.translator.I18nLabelValue
import fr.vsct.tock.translator.Translator

/**
 *
 */
data class BotSimpleAnswer(val label: I18nLabel, val delay: Long, val mediaMessage: BotMediaMessageDescriptor? = null) {

    constructor(answer: SimpleAnswer) :
        this(
            Translator.saveIfNotExists(answer.key),
            answer.delay,
            answer.mediaMessage?.let { BotMediaMessageDescriptor.fromDescriptor(it) }
        )

    fun toConfiguration(): SimpleAnswer =
        SimpleAnswer(I18nLabelValue(label), delay, mediaMessage?.toDescriptor())
}