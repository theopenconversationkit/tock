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

package ai.tock.bot.connector.slack.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * An event callback.
 */
data class CallbackEvent(
    val token: String,
    @JsonProperty("team_id")
    val teamId: String,
    @JsonProperty("api_app_id")
    val apiAppId: String,
    val event: MessageEvent,
) : EventApiMessage()
