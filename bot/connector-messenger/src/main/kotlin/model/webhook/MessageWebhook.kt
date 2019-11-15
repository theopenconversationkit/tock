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

package ai.tock.bot.connector.messenger.model.webhook

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import ai.tock.bot.connector.messenger.model.Recipient
import ai.tock.bot.connector.messenger.model.Sender
import ai.tock.shared.Dice

data class MessageWebhook(
    override val sender: Sender,
    override val recipient: Recipient,
    override val timestamp: Long,
    val message: Message,
    @get:JsonProperty("prior_message")
    override val priorMessage: PriorMessage? = null
) : Webhook() {

    @JsonIgnore
    fun getMessageId(): String =
        message.mid.run {
            if (isEmpty()) {
                Dice.newId()
            } else {
                this
            }
        }
}