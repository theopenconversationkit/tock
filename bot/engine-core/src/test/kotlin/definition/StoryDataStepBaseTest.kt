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

package ai.tock.bot.definition

import ai.tock.bot.connector.ConnectorType.Companion.none
import ai.tock.bot.engine.BotBus
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StoryDataStepBaseTest {
    @Test
    fun `GIVEN StoryDataStepBase with no setup specified THEN execute does not fail`() {
        val bus: BotBus =
            mockk(relaxed = true) {
                every { targetConnectorType } returns none
            }
        val result = Step3.execute(Def2(bus, StoryData()), StoryData())
        assertEquals("ok", result)
    }
}
