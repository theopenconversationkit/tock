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

package ai.tock.bot.connector.messenger.model.send

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.bot.engine.message.GenericMessage.Companion.TEXT_PARAM
import ai.tock.shared.security.TockObfuscatorService

class TextMessage(val text: String, quickReplies: List<QuickReply>? = null) : Message(quickReplies?.run { if (isEmpty()) null else this }) {

    override fun toGenericMessage(): GenericMessage? {
        val texts = mapOf(TEXT_PARAM to text)
        return if (quickReplies?.isNotEmpty() == true) {
            GenericMessage(
                texts = texts,
                choices = quickReplies.mapNotNull { it.toChoice() },
                locations = quickReplies.mapNotNull { it.toLocation() }
            )
        } else {
            GenericMessage(texts = texts)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TextMessage

        if (text != other.text) return false
        if (quickReplies != other.quickReplies) return false

        return true
    }

    override fun obfuscate(): ConnectorMessage {
        return TextMessage(TockObfuscatorService.obfuscate(text)!!, quickReplies)
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }

    override fun toString(): String {
        return "TextMessage(text='$text',quickReplies=$quickReplies)"
    }

    override fun copy(quickReplies: List<QuickReply>?): Message = TextMessage(text, quickReplies)
}
