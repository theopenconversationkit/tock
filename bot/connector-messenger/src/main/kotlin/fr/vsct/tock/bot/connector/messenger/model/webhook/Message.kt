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

package fr.vsct.tock.bot.connector.messenger.model.webhook

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import fr.vsct.tock.bot.connector.messenger.json.webhook.MessageDeserializer

@JsonDeserialize(using = MessageDeserializer::class)
open class Message(open val mid: String,
                   open val seq: Long,
                   open var text: String? = null,
                   open val attachments: List<Attachment> = emptyList()) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Message

        if (mid != other.mid) return false
        if (seq != other.seq) return false
        if (text != other.text) return false
        if (attachments != other.attachments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mid.hashCode()
        result = 31 * result + seq.hashCode()
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + attachments.hashCode()
        return result
    }

    override fun toString(): String {
        return "Message(mid='$mid', seq=$seq, text=$text, attachments=$attachments)"
    }


}