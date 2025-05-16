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

import ai.tock.bot.engine.BotEngineTest
import ai.tock.bot.engine.TestStoryDefinition
import ai.tock.nlp.entity.date.DateEntityRange
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 *
 */
class DefinitionBuildersTest : BotEngineTest() {

    enum class Step : StoryStep<StoryHandlerDefinition> {
        a, b
    }

    val unknownStory = storyWithSteps<Step>("unknown") {
        end("I said unknown")
    }

    override val botDefinition: BotDefinition = object : BotDefinitionBase(
        "test",
        "namespace",
        stories = enumValues<TestStoryDefinition>().toList(),
        unknownStory = unknownStory
    ) {}

    @Test
    fun `story with steps set the right base intent for steps`() {
        val yeh = storyWithSteps<Step>("yeh") {
            end("yeh")
        }
        assertEquals("yeh", yeh.steps.first().baseIntent.wrappedIntent().name)
        assertEquals("yeh", yeh.steps.last().baseIntent.wrappedIntent().name)
        assertEquals(Step.a, yeh.steps.first())
        assertEquals(Step.b, yeh.steps.last())
    }

    @Test
    fun `unknown story can be a story with steps`() {
        bus.step = Step.a
        unknownStory.handle(bus)
    }

    @Test
    fun `story with preconditions is ok`() {
        val s = storyDef<Def, StoryData>("yeh") {
            StoryData(
                entityText("entity"),
                entityValue<DateEntityRange>("date")?.start()
            )
        }
        assertNotNull(s)
    }

    @Test
    fun `story with preconditions and steps is ok`() {
        val s = storyDefWithSteps<Def2, Step2, StoryData>("yeh") {
            StoryData(
                entityText("entity"),
                entityValue<DateEntityRange>("date")?.start()
            )
        }
        assertNotNull(s)
    }

    @Test
    fun `story with preconditions and no exhaustive conditions is ok`() {
        val s = storyDef<Def>("yeh") {
            when {
                entityText("entity") == null -> end("For which destination?")
            }
        }
        assertNotNull(s)
    }
}
