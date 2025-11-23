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

package ai.tock.bot.engine.action

import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.security.MapObfuscator
import ai.tock.shared.security.TockObfuscatorService
import ai.tock.shared.security.TockObfuscatorService.obfuscate
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class SendChoiceTest {
    class TestParamObfuscator : MapObfuscator {
        override fun obfuscate(map: Map<String, String>): Map<String, String> {
            return map.mapValues { "" }
        }
    }

    @Test
    fun encodeChoiceId_shouldNotFail_whenCurrentIntentIsNull() {
        assertEquals("test", SendChoice.encodeChoiceId(Intent("test"), null))
    }

    @Test
    fun `GIVEN a sendchoice with parameters WHEN obfuscate the sendchoice THEN obfuscates the parameters`() {
        val testParameterObfuscator = spyk(TestParamObfuscator())
        TockObfuscatorService.registerMapObfuscator(testParameterObfuscator)

        val sendChoice =
            SendChoice(
                PlayerId(
                    UUID.randomUUID().toString(),
                ),
                "",
                PlayerId(
                    UUID.randomUUID().toString(),
                ),
                "intent",
                mapOf(
                    "p1" to "paramValue",
                    "p2" to "paramValue",
                ),
            )
        val obfuscatedMap = obfuscate((sendChoice.toMessage() as Choice).parameters)

        assertTrue(
            obfuscatedMap.all {
                it.value.isEmpty()
            },
        )
        verify(exactly = 1) { testParameterObfuscator.obfuscate(any()) }
        TockObfuscatorService.deregisterObfuscators()
    }
}
