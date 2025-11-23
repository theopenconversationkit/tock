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

package ai.tock.bot.jackson

import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ActionVisibilityDeserializationTest {
    @Test
    fun `GIVEN upper case visibility THEN deserialization is ok`() {
        val request = """{"lastAnswer":false,"priority":"normal","visibility":"PUBLIC","replyMessage":"UNKNOWN","quoteMessage":"UNKNOWN"}"""
        assertEquals(ActionVisibility.PUBLIC, mapper.readValue<ActionMetadata>(request).visibility)
    }
}
