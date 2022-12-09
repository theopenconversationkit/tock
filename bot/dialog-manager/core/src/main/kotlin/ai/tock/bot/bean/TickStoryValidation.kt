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

import ai.tock.bot.handler.ActionHandlersRepository
import ai.tock.bot.statemachine.StateMachine

/**
 * The tick story validation service
 */
object TickStoryValidation {

    object MessageProvider {
        val INTENT_NOT_FOUND : (String) -> String =  {
            "Intent $it not found in StateMachine"
        }

        val TRIGGER_NOT_FOUND : (String) -> String =  {
            "Trigger $it not found in StateMachine"
        }

        val NOT_SECONDARY_INTENT_ASSOCIATED_TO_CTX : (String) -> String =  {
            "Intent $it is not secondary, it cannot be associated to contexts"
        }

        val INTENT_CTX_ASSOCIATION_NOT_FOUND : (String) -> String =  {
            "Intent association context $it not found in declared contexts"
        }

        val INTENT_ACTION_ASSOCIATION_NOT_FOUND : (String) -> String =  {
            "Intent association action $it not found in declared actions"
        }
        val TRANSITION_NOT_FOUND : (String) -> String =  {
            "Transition $it not found in TickStory intents or triggers"
        }

        val ACTION_NOT_FOUND : (String) -> String =  {
            "Action $it not found in StateMachine"
        }

        val STATE_NOT_FOUND : (String) -> String =  {
            "State $it not found in TickStory actions"
        }

        val ACTION_HANDLER_NOT_FOUND : (String) -> String =  {
            "Action handler $it not found in handlers repository"
        }

        val INPUT_CTX_NOT_FOUND : (Pair<String, String>) -> String =  {
            "Input context ${it.first} of action ${it.second} not found in output contexts of others"
        }

        val OUTPUT_CTX_NOT_FOUND : (Pair<String, String>) -> String =  {
            "Output context ${it.first} of action ${it.second} not found in input contexts of others"
        }

        val ACTION_CTX_NOT_FOUND : (String) -> String =  {
            "Action context $it not found in declared contexts"
        }

        val DECLARED_CTX_NOT_FOUND : (String) -> String =  {
            "Declared context $it not found in actions"
        }

        val ACTION_HANDLER_CTX_NAME_CONFLICT: (String) -> String = {
            "The same name $it is used for Action handler and context"
        }

    }

    fun validateIntents(tickStory: TickStory): List<String> {
        return with(tickStory) {
            val sm = StateMachine(stateMachine)
            // For each intent (primary and secondary) declared in the TickStory,
            // we must find a transition with the same name in the state machine
            val allIntents = listOf(mainIntent).union(primaryIntents).union(secondaryIntents)
            allIntents
                .filterNot { sm.containsTransition(it) }
                .map { MessageProvider.INTENT_NOT_FOUND(it) }
        }
    }

    /**
     * Validate tickStory triggers
     * @param tickStory: the tickStory to validate
     * @return list of error message for each trigger not specified as transition
     */
    fun validateTriggers(tickStory: TickStory): List<String> = with(tickStory) {
        triggers
            .filterNot { StateMachine(stateMachine).containsTransition(it) }
            .map { MessageProvider.TRIGGER_NOT_FOUND(it) }
    }

    fun validateTickIntentNames(tickStory: TickStory): List<String> {
        return with(tickStory) {
            // For each TickIntent, the Intent used must be secondary,
            intentsContexts
                .map { it.intentName }
                .filterNot { it in secondaryIntents }
                .map { MessageProvider.NOT_SECONDARY_INTENT_ASSOCIATED_TO_CTX(it) }
        }
    }

    fun validateDeclaredIntentContexts(tickStory: TickStory): List<String> {
        return with(tickStory) {
            // For each TickIntentAssociation, the context used must be declared,
            intentsContexts
                .flatMap { it.associations }
                .flatMap { it.contextNames }
                .filterNot { contextName -> contextName in contexts.map { it.name } }
                .map { MessageProvider.INTENT_CTX_ASSOCIATION_NOT_FOUND(it) }
        }
    }

    fun validateTickIntentAssociationActions(tickStory: TickStory): List<String> {
        return with(tickStory) {
            // For each TickIntentAssociation, the action used must be declared,
            intentsContexts
                .flatMap { it.associations }
                .map { it.actionName }
                .filterNot { actionName -> actionName in actions.map { it.name } }
                .map { MessageProvider.INTENT_ACTION_ASSOCIATION_NOT_FOUND(it) }
        }
    }

    fun validateTransitions(tickStory: TickStory): List<String> {
        return with(tickStory) {
            val sm = StateMachine(stateMachine)
            // For each transition in the state machine,
            // we must find an intent or trigger with the same name in the TickStory
            val allIntents = listOf(mainIntent).union(primaryIntents).union(secondaryIntents).union(triggers)
            sm.getAllTransitions()
                .filterNot { allIntents.contains(it) }
                .map { MessageProvider.TRANSITION_NOT_FOUND(it) }
        }
    }

    fun validateActions(tickStory: TickStory): List<String> {
        return with(tickStory) {
            val sm = StateMachine(stateMachine)
            // For each action declared in the TickStory,
            // we must find a state with the same name in the state machine
            actions
                .filter { sm.getState(it.name) == null }
                .map { MessageProvider.ACTION_NOT_FOUND(it.name) }
        }
    }

    fun validateStates(tickStory: TickStory): List<String> {
        return with(tickStory) {
            val sm = StateMachine(stateMachine)
            // For each state of the state machine that is not a grouping state,
            // there must be an action of the same name in the TickStory
            sm.getAllStatesNotGroup()
                .filterNot { state -> actions.any { it.name == state } }
                .map { MessageProvider.STATE_NOT_FOUND(it) }
        }
    }

    fun validateActionHandlers(tickStory: TickStory): List<String> {
        return with(tickStory) {
            // For each action declaring to execute business code,
            // we must find a class bearing the name of the handler declared in the action
            actions
                .filter { !it.handler.isNullOrBlank() }
                .filterNot { ActionHandlersRepository.contains(it.handler!!) }
                .map { MessageProvider.ACTION_HANDLER_NOT_FOUND(it.handler!!) }
        }
    }

    fun validateInputOutputContexts(tickStory: TickStory): List<String> {
        val errors = mutableListOf<String>()

        val intentContexts = getIntentContexts(tickStory)
        val outputContext: (TickAction)->Set<String> = { it.outputContextNames.union(intentContexts) }
        val inputContext: (TickAction)->Set<String> = { it.inputContextNames.union(intentContexts) }

        // For each context declared as output to an action (or intent),
        // there must be at least one other action requiring the same context as input.
        errors.addAll(
            validateContexts(tickStory.actions, inputContext, outputContext).map {
                MessageProvider.INPUT_CTX_NOT_FOUND(it)
            }
        )

        // For each context declared as input to an action,
        // there must be at least one other action (or intent) producing the same context as output.
        errors.addAll(
            validateContexts(tickStory.actions, outputContext, inputContext).map {
                MessageProvider.OUTPUT_CTX_NOT_FOUND(it)
            }
        )

        return errors
    }

    private fun getIntentContexts(tickStory: TickStory): Set<String> {
        return tickStory.intentsContexts
            .flatMap { it.associations }
            .flatMap { it.contextNames }
            .toSet()
    }

    private fun validateContexts(actions: Set<TickAction>,
                                            first: (TickAction)->Set<String>,
                                            second: (TickAction)->Set<String>): List<Pair<String, String>> {
        // Returns the elements of the first set, except those that existed in the second
        return actions.flatMap { action ->
            val contexts = first(action)
            val contextsCompared = actions.filter { it.name != action.name }.flatMap { second(it) }.toSet()
            contexts.minus(contextsCompared).map { Pair(it, action.name) }
        }
    }

    fun validateDeclaredActionContexts(tickStory: TickStory): List<String> {
        val errors = mutableListOf<String>()

        val contextsUsed = tickStory.actions
            .flatMap { it.inputContextNames.union(it.outputContextNames) }
            .toSet()
        val contextDeclared = tickStory.contexts
            .map { it.name }
            .toSet()

        errors.addAll(
            contextsUsed
                .minus(contextDeclared)
                .map { MessageProvider.ACTION_CTX_NOT_FOUND(it) }
        )

        errors.addAll(
            contextDeclared
                .minus(contextsUsed)
                .map { MessageProvider.DECLARED_CTX_NOT_FOUND(it) }
        )

        return errors
    }

    fun validateNames(tickStory: TickStory): List<String> {
        return with(tickStory) {
            // Context names could not be used for Action handler name
            contexts
                .map { it.name }
                .intersect(actions.map { it.name }.toSet())
                .map { MessageProvider.ACTION_HANDLER_CTX_NAME_CONFLICT(it) }
        }
    }

    fun validateTickStory(tick: TickStory): Set<String> {
        val errors = mutableSetOf<String>()

        // Consistency between declared intents/triggers and the state machine transitions :
        errors.addAll(validateIntents(tick))
        errors.addAll(validateTriggers(tick))
        errors.addAll(validateTransitions(tick))

        // Consistency between declared actions and the state machine states:
        errors.addAll(validateActions(tick))
        errors.addAll(validateStates(tick))

        // Consistency of action handlers :
        errors.addAll(validateActionHandlers(tick))

        // Consistency of contexts :
        errors.addAll(validateInputOutputContexts(tick))
        errors.addAll(validateDeclaredActionContexts(tick))

        // Consistency of tick intents :
        errors.addAll(validateTickIntentNames(tick))
        errors.addAll(validateTickIntentAssociationActions(tick))
        errors.addAll(validateDeclaredIntentContexts(tick))

        // Consistency of names :
        errors.addAll(validateNames(tick))

        return errors
    }
}





