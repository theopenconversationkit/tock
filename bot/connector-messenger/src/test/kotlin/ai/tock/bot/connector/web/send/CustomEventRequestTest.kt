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
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

@Suppress("ktlint:standard:max-line-length")
class CustomEventRequestTest {
    @Test
    fun `GIVEN a CustomEventRequest WHEN serializing to JSON THEN gets the expected result`() {
        val customEventName = "MyCustomEventName"
        val pageId = "0123456789"
        val userId = UUID.randomUUID().toString()
        val customEventRequest = CustomEventRequest(CustomEvent(customEventName), pageId, userId)
        val serialized = mapper.writeValueAsString(customEventRequest)
        assertEquals(
            "{\"custom_events\":[{\"_eventName\":\"$customEventName\"}],\"page_id\":\"$pageId\",\"page_scoped_user_id\":\"$userId\",\"event\":\"CUSTOM_APP_EVENTS\",\"advertiser_tracking_enabled\":0,\"application_tracking_enabled\":1,\"extinfo\":\"[\\\"mb1\\\"]\"}",
            serialized,
        )
    }
}
