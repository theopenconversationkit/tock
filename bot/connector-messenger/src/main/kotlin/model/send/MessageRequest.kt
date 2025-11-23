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

import ai.tock.bot.connector.messenger.model.Recipient
import ai.tock.bot.connector.messenger.model.send.MessagingType.RESPONSE
import ai.tock.bot.connector.messenger.model.send.NotificationType.NO_PUSH
import com.fasterxml.jackson.annotation.JsonProperty

data class MessageRequest(
    val recipient: Recipient,
    val message: Message,
    @JsonProperty("messaging_type")
    val messagingType: MessagingType = RESPONSE,
    @JsonProperty("notification_type")
    val notificationType: NotificationType = NO_PUSH,
    @JsonProperty("tag")
    val tag: MessageTag? = null,
    @JsonProperty("persona_id")
    val personaId: String? = null,
)
