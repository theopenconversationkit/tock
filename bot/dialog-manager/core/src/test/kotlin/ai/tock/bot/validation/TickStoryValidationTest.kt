/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.validation

import ai.tock.bot.DialogManagerTest
import ai.tock.bot.handler.ActionHandlersRepository
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TickStoryValidationTest : DialogManagerTest() {

    @Test
    fun tickStoryWithoutValidationErrors() {
        val tickStory = getTickStoryFromFile("validation", "tickStory-valid")
        val errors = TickStoryValidation.validateTickStory(tickStory) { true }

        assertTrue { errors.isEmpty() }
    }

    @Nested
    inner class ConsistencyBetweenDeclaredIntentsAndStateMachineTransitions {
        @Test
        fun validIntents() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-intents")
            val errors = TickStoryValidation.validateIntents(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun validTriggers() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-triggers")
            val errors = TickStoryValidation.validateTriggers(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidIntents() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-intents")
            val errors = TickStoryValidation.validateIntents(tickStory)


            val expectedErrors = listOf(
                "mainIntent",
                "primaryIntents_1",
                "primaryIntents_2",
                "primaryIntents_3",
                "secondaryIntents_1",
                "secondaryIntents_2"
            ).map { MessageProvider.INTENT_NOT_FOUND(it) }

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test
        fun invalidTriggers() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-triggers")
            val errors = TickStoryValidation.validateTriggers(tickStory)


            val expectedErrors = listOf(
                "e_trigger"
            ).map { MessageProvider.TRIGGER_NOT_FOUND(it) }

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test
        fun validTransitions() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-transitions")
            val errors = TickStoryValidation.validateTransitions(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidTransitions() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-transitions")
            val errors = TickStoryValidation.validateTransitions(tickStory)

            val expectedErrors = setOf(
                "mainIntent-test",
                "primaryIntents_1-test",
                "primaryIntents_2-test",
                "primaryIntents_3-test",
                "secondaryIntents_1-test",
                "secondaryIntents_2-test",
                "trigger-test"
            ).map { MessageProvider.TRANSITION_NOT_FOUND(it) }

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }
    }

    @Nested
    inner class ConsistencyBetweenDeclaredActionsAndStateMachineStates {
        @Test
        fun validActions() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-actions")
            val errors = TickStoryValidation.validateActions(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidActions() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-actions")
            val errors = TickStoryValidation.validateActions(tickStory)

            val expectedErrors = setOf(
                "HELLO-TEST",
                "BYE-TEST",
                "INTRODUCTION-TEST",
                "ACTION_1-TEST",
                "ACTION_2-TEST",
                "ACTION_3-TEST"
            ).map { MessageProvider.ACTION_NOT_FOUND(it) }

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test
        fun validStates() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-states")
            val errors = TickStoryValidation.validateStates(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidStates() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-states")
            val errors = TickStoryValidation.validateStates(tickStory)

            val expectedErrors = setOf(
                "HELLO",
                "BYE",
                "INTRODUCTION",
                "ACTION_1",
                "ACTION_2",
                "ACTION_3"
            ).map { MessageProvider.STATE_NOT_FOUND(it) }

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }
    }

    @Nested
    inner class ConsistencyOfActionHandlers {
        @Test
        fun validActionHandlers() {

            mockkObject(ActionHandlersRepository)
            every { ActionHandlersRepository.contains("BYE_handler") } returns true
            every { ActionHandlersRepository.contains("ACTION_2_handler") } returns true

            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-action-handlers")
            val errors = TickStoryValidation.validateActionHandlers(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidActionHandlers() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-action-handlers")
            val errors = TickStoryValidation.validateActionHandlers(tickStory)

            val expectedErrors = setOf(
                "BYE_handler-TEST",
                "ACTION_2_handler-TEST"
            )
                .map { MessageProvider.ACTION_HANDLER_NOT_FOUND(it) }

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }
    }

    @Nested
    inner class ConsistencyOfContexts {
        @Test
        fun validInputOutputContexts() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-input-output-contexts")
            val errors = TickStoryValidation.validateInputOutputContexts(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidInputOutputContexts() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-input-output-contexts")
            val errors = TickStoryValidation.validateInputOutputContexts(tickStory)

            val expectedErrors = setOf(
                MessageProvider.INPUT_CTX_NOT_FOUND("CONTEXT_4" to "ACTION_2"),
                MessageProvider.OUTPUT_CTX_NOT_FOUND("CONTEXT_5" to "ACTION_3")
            )

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test
        fun validDeclaredActionContexts() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-declared-contexts")
            val errors = TickStoryValidation.validateInputOutputContexts(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidDeclaredActionContexts() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-declared-contexts")
            val errors = TickStoryValidation.validateDeclaredActionContexts(tickStory)

            val expectedErrors = setOf(
                MessageProvider.ACTION_CTX_NOT_FOUND("CONTEXT_3"),
                MessageProvider.DECLARED_CTX_NOT_FOUND("CONTEXT_20")
            )

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }
    }

    @Nested
    inner class ConsistencyOfNames {
        @Test
        fun validNames() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-names")
            val errors = TickStoryValidation.validateNames(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidNames() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-names")
            val errors = TickStoryValidation.validateNames(tickStory)

            val expectedErrors = setOf(
                "CONTEXT_1",
                "INTRODUCTION"
            ).map { MessageProvider.ACTION_HANDLER_CTX_NAME_CONFLICT(it) }

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }
    }

    @Nested
    inner class ConsistencyOfTickIntent {
        @Test
        fun validTickIntentNames() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-tick-intent-names")
            val errors = TickStoryValidation.validateTickIntentNames(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidTickIntentNames() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-tick-intent-names")
            val errors = TickStoryValidation.validateTickIntentNames(tickStory)

            val expectedErrors = setOf(
                "primaryIntents_1",
                "secondaryIntents_3"
            ).map { MessageProvider.NO_SECONDARY_INTENT_ASSOCIATED_TO_CTX(it) }

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test
        fun validTickIntentAssociationActions() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-tick-intent-actions")
            val errors = TickStoryValidation.validateTickIntentAssociationActions(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidTickIntentAssociationActions() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-tick-intent-actions")
            val errors = TickStoryValidation.validateTickIntentAssociationActions(tickStory)

            val expectedErrors = setOf(
                MessageProvider.INTENT_ACTION_ASSOCIATION_NOT_FOUND("ACTION_20"),
            )

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test
        fun validDeclaredIntentContexts() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-tick-intent-contexts")
            val errors = TickStoryValidation.validateDeclaredIntentContexts(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidDeclaredIntentContexts() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-tick-intent-contexts")
            val errors = TickStoryValidation.validateDeclaredIntentContexts(tickStory)

            val expectedErrors = setOf(
                MessageProvider.INTENT_CTX_ASSOCIATION_NOT_FOUND("CONTEXT_3"),
            )

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test
        fun invalidUnknownConfigAction() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-unknown-config")
            val errors = TickStoryValidation.validateUnknownConfigActions(tickStory)

            val expectedErrors = setOf(
                MessageProvider.UNKNOWN_ACTION_NOT_FOUND("unknown-action")
            )
            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test
        fun validUnknownConfigAction() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-unknown-config")
            val errors = TickStoryValidation.validateUnknownConfigActions(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun validUnknownConfigIntent() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-unknown-config")
            val errors = TickStoryValidation.validateUnknownConfigIntents(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidUnknownConfigIntent() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-unknown-config")
            val errors = TickStoryValidation.validateUnknownConfigIntents(tickStory)

            val expectedErrors = setOf(
                MessageProvider.UNKNOWN_INTENT_NOT_IN_SECONDARY_INTENTS("unknown")
            )
            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

    }

    @Nested
    inner class ConsistencyOfTargetStory {
        @Test
        fun validTickAction() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-with-target-story")
            val errors = TickStoryValidation.validateTargetStory(tickStory) { true }

            assertTrue { errors.isEmpty() }
        }

        @Test
        fun invalidTickAction() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-with-target-story")
            val errors = TickStoryValidation.validateTargetStory(tickStory) { false }

            assertFalse { errors.isEmpty() }
            assertEquals(1, errors.size)
            assertEquals(MessageProvider.ACTION_TARGET_STORY_NOT_FOUND("HELLO" to "targetStory"), errors[0])
        }


    }
}