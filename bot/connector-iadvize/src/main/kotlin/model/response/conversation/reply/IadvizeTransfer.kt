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

package ai.tock.bot.connector.iadvize.model.response.conversation.reply

import ai.tock.bot.connector.iadvize.model.response.conversation.Duration
import ai.tock.iadvize.client.graphql.ChatbotActionInput
import ai.tock.iadvize.client.graphql.ChatbotActionOrMessageInput
import ai.tock.iadvize.client.graphql.TransferMessageInput
import ai.tock.iadvize.client.graphql.TransferOptionsInput
import com.fasterxml.jackson.annotation.JsonIgnore

data class IadvizeTransfer(
    val distributionRule: String?,
    val transferOptions: TransferOptions) : IadvizeReply(ReplyType.transfer) {

    /**
     * Convert a timeout in seconds
     * @param timeout the [Duration]
     */
    data class TransferOptions(val timeout: Duration) {

        @JsonIgnore
        fun getTimeoutInSeconds(): Int = when(timeout.unit){
            Duration.TimeUnit.minutes -> (timeout.value * 60).toInt()
            Duration.TimeUnit.seconds -> timeout.value.toInt()
            Duration.TimeUnit.millis -> (timeout.value / 1000).toInt()
        }
    }

    /**
     * When an IadvizeTransfer is created on a story, distribution rule is not known.
     * Distribution rule is added when response is built.
     */
    constructor(timeoutInSeconds: Long)
            : this(null, TransferOptions(Duration(timeoutInSeconds, Duration.TimeUnit.seconds)))

    /**
     * When an IadvizeTransfer is created on a story, distribution rule is not known.
     * Distribution rule is added when response is built.
     */
    constructor(timeout: Duration)
            : this(null, TransferOptions(timeout))

    override fun toChatBotActionOrMessageInput() =
        ChatbotActionOrMessageInput(
            chatbotAction = ChatbotActionInput(
                transferMessage = TransferMessageInput(
                    routingRuleId = distributionRule ?: "unknown_distribution_rule",
                    transferOptions = TransferOptionsInput(
                        timeout = transferOptions.getTimeoutInSeconds()
                    )
                )
            )
        )
}
