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

import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.annotation.JsonProperty

data class CustomEventRequest(
    @JsonProperty("custom_events") val customEvents: List<CustomEvent>,
    @JsonProperty("page_id") val pageId: String,
    @JsonProperty("page_scoped_user_id") val pageScopedUserId: String,
    val event: String = "CUSTOM_APP_EVENTS",
    @JsonProperty("advertiser_tracking_enabled") val advertiserTrackingEnabled: Short = 0,
    @JsonProperty("application_tracking_enabled") val applicationTrackingEnabled: Short = 1,
    val extinfo: String = mapper.writeValueAsString(listOf("mb1")),
) {
    constructor(
        customEvent: CustomEvent,
        pageId: String,
        pageScopedUserId: String,
        event: String = "CUSTOM_APP_EVENTS",
        advertiserTrackingEnabled: Short = 0,
        applicationTrackingEnabled: Short = 1,
        extinfo: String = mapper.writeValueAsString(listOf("mb1")),
    ) : this(
        listOf(customEvent),
        pageId,
        pageScopedUserId,
        event,
        advertiserTrackingEnabled,
        applicationTrackingEnabled,
        extinfo,
    )
}
