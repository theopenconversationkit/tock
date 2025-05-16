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

package ai.tock.analytics.chatbase.model

import com.fasterxml.jackson.annotation.JsonProperty

internal data class Message(
    @JsonProperty("api_key")
    val apiKey: String,
    val type: Type,
    val platform: String,
    @JsonProperty("user_id")
    val userId: String,
    val message: String,
    val intent: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    @JsonProperty("not_handled")
    val notHandled: Boolean? = null,
    val version: String = "1.0",
    @JsonProperty("session_id")
    val sessionId: String? = null
)
