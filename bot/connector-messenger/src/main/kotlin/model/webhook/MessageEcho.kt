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

package ai.tock.bot.connector.messenger.model.webhook

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 */
data class MessageEcho(
    override val mid: String,
    override var text: String? = null,
    override val attachments: List<Attachment> = emptyList(),
    @JsonProperty("is_echo") val echo: Boolean = true,
    @JsonProperty("app_id") val appId: Long,
    val metadata: String? = null,
) : Message(mid, text, attachments)
