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

package ai.tock.bot.connector.messenger.json.model.handover

import ai.tock.bot.connector.messenger.model.handover.ThreadOwnerResponse
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.test.Test
import kotlin.test.assertEquals

class ThreadOwnerDeserializationTest {

    @Test
    fun `GIVEN Thread owner data THEN deserialization is ok`() {
        val t = """{"data":[{"thread_owner":{"app_id":"655869374944173"}}]}"""
        val threadOwner: ThreadOwnerResponse = mapper.readValue(t)
        assertEquals("655869374944173", threadOwner.data.firstOrNull()?.threadOwner?.appId)
    }
}
