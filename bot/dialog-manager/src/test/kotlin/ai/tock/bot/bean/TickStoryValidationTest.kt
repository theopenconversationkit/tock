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

package ai.tock.bot.bean

import ai.tock.bot.DialogManagerTest
import ai.tock.bot.handler.ActionHandlersRepository
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TickStoryValidationTest : DialogManagerTest() {

    @Test fun tickStoryWithoutValidationErrors() {
        val tickStory = getTickStoryFromFile("validation", "tickStory-valid")
        val errors = TickStoryValidation.validateTickStory(tickStory)

        assertTrue { errors.isEmpty() }
    }

    @Nested inner class ConsistencyBetweenDeclaredIntentsAndStateMachineTransitions {
        @Test
        fun validIntents() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-intents")
            val errors = TickStoryValidation.validateIntents(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test fun invalidIntents() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-intents")
            val errors = TickStoryValidation.validateIntents(tickStory)

            val expectedErrors = setOf(
                "Intent mainIntent not found in StateMachine",
                "Intent primaryIntents_1 not found in StateMachine",
                "Intent primaryIntents_2 not found in StateMachine",
                "Intent primaryIntents_3 not found in StateMachine",
                "Intent secondaryIntents_1 not found in StateMachine",
                "Intent secondaryIntents_2 not found in StateMachine")

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test fun validTransitions() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-transitions")
            val errors = TickStoryValidation.validateTransitions(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test fun invalidTransitions() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-transitions")
            val errors = TickStoryValidation.validateTransitions(tickStory)

            val expectedErrors = setOf(
                "Transition mainIntent-test not found in TickStory intents",
                "Transition primaryIntents_1-test not found in TickStory intents",
                "Transition primaryIntents_2-test not found in TickStory intents",
                "Transition primaryIntents_3-test not found in TickStory intents",
                "Transition secondaryIntents_1-test not found in TickStory intents",
                "Transition secondaryIntents_2-test not found in TickStory intents"
            )

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }
    }

    @Nested inner class ConsistencyBetweenDeclaredActionsAndStateMachineStates {
        @Test fun validActions() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-actions")
            val errors = TickStoryValidation.validateActions(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test fun invalidActions() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-actions")
            val errors = TickStoryValidation.validateActions(tickStory)

            val expectedErrors = setOf(
                "Action HELLO-TEST not found in StateMachine",
                "Action BYE-TEST not found in StateMachine",
                "Action INTRODUCTION-TEST not found in StateMachine",
                "Action ACTION_1-TEST not found in StateMachine",
                "Action ACTION_2-TEST not found in StateMachine",
                "Action ACTION_3-TEST not found in StateMachine")

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test fun validStates() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-states")
            val errors = TickStoryValidation.validateStates(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test fun invalidStates() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-states")
            val errors = TickStoryValidation.validateStates(tickStory)

            val expectedErrors = setOf(
                "State HELLO not found in TickStory actions",
                "State BYE not found in TickStory actions",
                "State INTRODUCTION not found in TickStory actions",
                "State ACTION_1 not found in TickStory actions",
                "State ACTION_2 not found in TickStory actions",
                "State ACTION_3 not found in TickStory actions")

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }
    }

    @Nested inner class ConsistencyOfActionHandlers {
        @Test fun validActionHandlers() {

            mockkObject(ActionHandlersRepository)
            every { ActionHandlersRepository.contains("BYE_handler") } returns true
            every { ActionHandlersRepository.contains("ACTION_2_handler") } returns true

            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-action-handlers")
            val errors = TickStoryValidation.validateActionHandlers(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test fun invalidActionHandlers() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-action-handlers")
            val errors = TickStoryValidation.validateActionHandlers(tickStory)

            val expectedErrors = setOf(
                "Action handler BYE_handler-TEST not found in handlers repository",
                "Action handler ACTION_2_handler-TEST not found in handlers repository")

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }
    }

    @Nested inner class ConsistencyOfContexts {
        @Test fun validInputOutputContexts() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-input-output-contexts")
            val errors = TickStoryValidation.validateInputOutputContexts(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test fun invalidInputOutputContexts() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-input-output-contexts")
            val errors = TickStoryValidation.validateInputOutputContexts(tickStory)

            val expectedErrors = setOf(
                "Input context CONTEXT_4 of action ACTION_2 not found in output contexts of others",
                "Output context CONTEXT_5 of action ACTION_3 not found in input contexts of others")

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test fun validDeclaredContexts() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-declared-contexts")
            val errors = TickStoryValidation.validateInputOutputContexts(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test fun invalidDeclaredContexts() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-declared-contexts")
            val errors = TickStoryValidation.validateDeclaredContexts(tickStory)

            val expectedErrors = setOf(
                "Action context CONTEXT_3 not found in declared contexts")

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }
    }

    @Nested inner class ConsistencyOfNames {
        @Test fun validNames() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-names")
            val errors = TickStoryValidation.validateNames(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test fun invalidNames() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-names")
            val errors = TickStoryValidation.validateNames(tickStory)

            val expectedErrors = setOf(
                "The same name CONTEXT_1 is used for Action handler and context",
                "The same name INTRODUCTION is used for Action handler and context")

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }
    }

    @Nested inner class ConsistencyOfTickIntent {
        @Test
        fun validTickIntentNames() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-tick-intent-names")
            val errors = TickStoryValidation.validateTickIntentNames(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test fun invalidTickIntentNames() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-tick-intent-names")
            val errors = TickStoryValidation.validateTickIntentNames(tickStory)

            val expectedErrors = setOf(
                "Intent primaryIntents_1 is not secondary, it cannot be associated to contexts",
                "Intent secondaryIntents_3 is not secondary, it cannot be associated to contexts")

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test fun validTickIntentAssociationContexts() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-tick-intent-contexts")
            val errors = TickStoryValidation.validateTickIntentAssociationContexts(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test fun invalidTickIntentAssociationContexts() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-tick-intent-contexts")
            val errors = TickStoryValidation.validateTickIntentAssociationContexts(tickStory)

            val expectedErrors = setOf(
                "Intent association context CONTEXT_3 not found in declared contexts",
            )

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }

        @Test fun validTickIntentAssociationActions() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-valid-tick-intent-actions")
            val errors = TickStoryValidation.validateTickIntentAssociationActions(tickStory)

            assertTrue { errors.isEmpty() }
        }

        @Test fun invalidTickIntentAssociationActions() {
            val tickStory = getTickStoryFromFile("validation", "tickStory-invalid-tick-intent-actions")
            val errors = TickStoryValidation.validateTickIntentAssociationActions(tickStory)

            val expectedErrors = setOf(
                "Intent association action ACTION_20 not found in declared actions",
            )

            assertEquals(expectedErrors.size, errors.size)
            assertTrue { errors.containsAll(expectedErrors) }
        }
    }
}