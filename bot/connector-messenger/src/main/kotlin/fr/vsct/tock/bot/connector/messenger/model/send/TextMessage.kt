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

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.engine.message.SentenceElement
import fr.vsct.tock.shared.security.StringObfuscatorMode
import fr.vsct.tock.shared.security.StringObfuscatorService.obfuscate

/**
 *
 */
//TODO check 640 char text limit https://developers.facebook.com/docs/messenger-platform/send-api-reference/text-message
class TextMessage(val text: String, quickReplies: List<QuickReply>? = null) : Message(quickReplies?.run { if (isEmpty()) null else this }) {

    override fun toSentenceElement(): SentenceElement? {
        val texts = mapOf("title" to text)
        return if (quickReplies?.isNotEmpty() == true) {
            SentenceElement(
                    texts = texts,
                    choices = quickReplies.mapNotNull { it.toChoice() },
                    locations = quickReplies.mapNotNull { it.toLocation() })
        } else {
            SentenceElement(texts = texts)
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

    override fun obfuscate(mode: StringObfuscatorMode): ConnectorMessage {
        return TextMessage(obfuscate(text, mode)!!, quickReplies)
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }

    override fun toString(): String {
        return "TextMessage(text='$text',quickReplies=$quickReplies)"
    }


}