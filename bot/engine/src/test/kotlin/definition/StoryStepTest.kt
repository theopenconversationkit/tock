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

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.UserTimeline
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryStepTest {
    private val userTimeline: UserTimeline = mockk()
    private val dialog: Dialog = mockk()
    private val action: Action = mockk()

    private object Step1 : StoryStep<StoryHandlerDefinition> {
        override val name: String = "step1"
        override val intent: IntentAware? = Intent("test")
        override val entityStepSelection: EntityStepSelection? = EntityStepSelection("a", "role", "type")
    }

    private object Step2 : StoryStep<StoryHandlerDefinition> {
        override val name: String = "step2"
        override val intent: IntentAware? = Intent("test")
        override val entityStepSelection: EntityStepSelection? = EntityStepSelection(null, "role", "type")
    }

    @Test
    fun `GIVEN step with not null entityStepSelection WHEN the entity value is set by action THEN step is selected`() {
        every { action.hasEntityPredefinedValue("role", "a") } returns true

        val result = Step1.selectFromAction(userTimeline, dialog, action, Intent("test"))

        assertTrue(result)
    }

    @Test
    fun `GIVEN step with not null entityStepSelection WHEN the entity value is not set by action THEN step is not selected`() {
        every { action.hasEntityPredefinedValue("role", "a") } returns false

        val result = Step1.selectFromAction(userTimeline, dialog, action, Intent("test"))

        assertFalse(result)
    }

    @Test
    fun `GIVEN step with not null role WHEN the entity role is set by action THEN step is selected`() {
        every { action.hasEntity("role") } returns true

        val result = Step2.selectFromAction(userTimeline, dialog, action, Intent("test"))

        assertTrue(result)
    }

    @Test
    fun `GIVEN step with not null role WHEN the entity role is not set by action THEN step is not selected`() {
        every { action.hasEntity("role") } returns false

        val result = Step2.selectFromAction(userTimeline, dialog, action, Intent("test"))

        assertFalse(result)
    }
}
