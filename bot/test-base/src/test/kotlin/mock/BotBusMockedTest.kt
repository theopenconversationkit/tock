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

package ai.tock.bot.test.mock

import ai.tock.bot.engine.BotBus
import ai.tock.translator.raw
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BotBusMockedTest {
    private val botBus = mockk<BotBus>()

    @Test
    fun shouldTranslateString() {
        mockBus(botBus) {
            assertEquals("Tock - The (Best) Open Conversation Kit", botBus.translate("Tock - The (Best) Open Conversation Kit").toString())
        }
    }

    @Test
    fun shouldTranslateTranslatedCharSequence() {
        mockBus(botBus) {
            assertEquals("Tock - The (Best) Open Conversation Kit", botBus.translate("Tock - The (Best) Open Conversation Kit".raw).toString())
        }
    }
}
