/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.definition

import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class DefinitionBuildersTest {

    enum class Step : StoryStep<StoryHandlerDefinition> {
        a, b
    }

    @Test
    fun `story with steps set the right intent for steps`() {
        val yeh = storyWithSteps<Step>("yeh") {
            end("yeh")
        }
        assertEquals("yeh", yeh.steps.first().intent?.wrappedIntent()?.name)
        assertEquals("yeh", yeh.steps.last().intent?.wrappedIntent()?.name)
        assertEquals(Step.a, yeh.steps.first())
        assertEquals(Step.b, yeh.steps.last())
    }
}