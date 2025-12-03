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

package ai.tock.bot.connector.messenger.model.handover

import ai.tock.bot.connector.messenger.model.Recipient
import ai.tock.bot.connector.messenger.model.Sender
import ai.tock.bot.connector.messenger.model.webhook.Webhook
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * See [https://developers.facebook.com/docs/messenger-platform/reference/webhook-events/messaging_handovers#take_thread_control].
 */
data class TakeThreadControlWebhook(
    override val sender: Sender,
    override val recipient: Recipient,
    override val timestamp: Long,
    @JsonProperty("take_thread_control") val takeThreadControl: TakeThreadControl,
) : Webhook()

data class TakeThreadControl(
    @JsonProperty("previous_owner_app_id")
    val previousOwnerAppId: String,
    val metadata: String? = null,
)
