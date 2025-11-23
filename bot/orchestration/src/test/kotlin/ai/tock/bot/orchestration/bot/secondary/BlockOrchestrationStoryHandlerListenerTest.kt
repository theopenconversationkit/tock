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

package ai.tock.bot.orchestration.bot.secondary

import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionMetadata
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class BlockOrchestrationStoryHandlerListenerTest {
    private val listener =
        BlockOrchestrationStoryHandlerListener(
            BlockedIntentRegardlessPrimary(
                Intent("help"),
                Intent("human_request"),
            ),
            BlockedIntentForPrimary(
                "anonymousBot",
                Intent("my_account"),
                Intent("personalize"),
                message = "You can't see your account on this bot",
            ),
        )

    @Test
    internal fun `should continue action if not orchestrated`() {
        assertTrue(
            listener.startAction(
                mockBotBus(actionOrchestratedBy(null)),
                mockk(),
            ),
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["help", "human_request"])
    internal fun `should stop action if orchestrated and intent is blocked regardless primary`(blockedIntent: String) {
        val botBus = mockBotBus(actionOrchestratedBy("otherBot"), Intent(blockedIntent))
        assertFalse(listener.startAction(botBus, mockk()))
        verify { botBus.end(DEFAULT_ORCHESTRATION_BLOCKED_MESSAGE) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["my_account", "personalize"])
    internal fun `should stop action if orchestrated and intent is blocked for specific primary`(blockedIntent: String) {
        val botBus = mockBotBus(actionOrchestratedBy("anonymousBot"), Intent(blockedIntent))
        assertFalse(listener.startAction(botBus, mockk()))
        verify { botBus.end("You can't see your account on this bot") }
    }

    @ParameterizedTest
    @ValueSource(strings = ["passengers_details", "search", "my_account", "personalize"])
    internal fun `should continue action if orchestrated and intent not block`(authorizedIntent: String) {
        val botBus = mockBotBus(actionOrchestratedBy("otherBot"), Intent(authorizedIntent))
        assertTrue(listener.startAction(botBus, mockk()))
    }

    private fun actionOrchestratedBy(primaryBot: String?): Action =
        mockk {
            every { metadata } returns
                ActionMetadata(
                    orchestratedBy = primaryBot,
                )
        }

    private fun mockBotBus(
        busAction: Action,
        busIntent: IntentAware? = null,
    ): BotBus {
        return mockk(relaxed = true) {
            every { action } returns busAction
            every { intent } returns busIntent
        }
    }
}
