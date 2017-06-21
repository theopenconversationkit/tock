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

package fr.vsct.tock.bot.connector.messenger.model.send

import fr.vsct.tock.bot.engine.message.SentenceElement

/**
 *
 */
//TODO check 640 char text limit
data class ButtonPayload(val text: String, val buttons: List<Button>) : ModelPayload(PayloadType.button) {

    override fun toSentenceElement(): SentenceElement? {
        return SentenceElement(
                texts = mapOf(ButtonPayload::text.name to text),
                choices = buttons.map { it.toChoice() }
        )
    }
}