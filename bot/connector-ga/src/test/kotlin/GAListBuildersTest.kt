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

package ai.tock.bot.connector.ga

import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.BotBus
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 */
class GAListBuildersTest {

    @Test
    fun `listItem can generate image url`() {
        val bus: BotBus = mockk(relaxed = true)
        val listItem = bus.listItem("title", Intent("test"), "description", "imageUrl")
        assertEquals("imageUrl", listItem.image?.url)
    }
}
