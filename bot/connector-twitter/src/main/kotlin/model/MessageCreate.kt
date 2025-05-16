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

package ai.tock.bot.connector.twitter.model

import com.fasterxml.jackson.annotation.JsonProperty

data class MessageCreate(
    val target: Recipient,
    @JsonProperty("sender_id") val senderId: String,
    @JsonProperty("source_app_id") val sourceAppId: String? = null,
    @JsonProperty("message_data") val messageData: MessageData
)
