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

import ai.tock.bot.bean.TickAction
import ai.tock.bot.bean.TickStoryQuery
import ai.tock.bot.handler.ActionHandlersRepository
import ai.tock.bot.statemachine.StateMachine

/**
 * The tick story validation service
 */
object TickStoryValidation {

    /**
     * For each intent (primary and secondary) declared in the TickStory,
     * we must find a transition with the same name in the state machine
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
    */
    fun validateIntents(tickStoryQuery: TickStoryQuery): List<String> {
        return with(tickStoryQuery) {
            val sm = StateMachine(stateMachine)
            val allIntents = listOf(mainIntent)
                .union(primaryIntents)
                .union(secondaryIntents)
                .minus(unknownAnswerConfigs.map { it.intent }.toSet())

            allIntents
                .filterNot { sm.containsTransition(it) }
                .map { MessageProvider.INTENT_NOT_FOUND(it) }
        }
    }

    /**
     * Validate tickStory triggers
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message for each trigger not specified as transition
     */
    fun validateTriggers(tickStoryQuery: TickStoryQuery): List<String> = with(tickStoryQuery) {
        triggers
            .filterNot { StateMachine(stateMachine).containsTransition(it) }
            .map { MessageProvider.TRIGGER_NOT_FOUND(it) }
    }

    /**
     * For each TickIntent, the Intent used must be secondary
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateTickIntentNames(tickStoryQuery: TickStoryQuery): List<String> {
        return with(tickStoryQuery) {
            intentsContexts
                .map { it.intentName }
                .filterNot { it in secondaryIntents }
                .map { MessageProvider.NO_SECONDARY_INTENT_ASSOCIATED_TO_CTX(it) }
        }
    }

    /**
     * For each TickIntentAssociation, the context used must be declared
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateDeclaredIntentContexts(tickStoryQuery: TickStoryQuery): List<String> {
        return with(tickStoryQuery) {
            intentsContexts
                .flatMap { it.associations }
                .flatMap { it.contextNames }
                .filterNot { contextName -> contextName in contexts.map { it.name } }
                .map { MessageProvider.INTENT_CTX_ASSOCIATION_NOT_FOUND(it) }
        }
    }

    /**
     * For each TickIntentAssociation, the action used must be declared
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateTickIntentAssociationActions(tickStoryQuery: TickStoryQuery): List<String> {
        return with(tickStoryQuery) {
            intentsContexts
                .flatMap { it.associations }
                .map { it.actionName }
                .filterNot { actionName -> actionName in actions.map { it.name } }
                .map { MessageProvider.INTENT_ACTION_ASSOCIATION_NOT_FOUND(it) }
        }
    }

    /**
     * For each transition in the state machine,
     * we must find an intent or trigger with the same name in the TickStory
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateTransitions(tickStoryQuery: TickStoryQuery): List<String> {
        return with(tickStoryQuery) {
            val sm = StateMachine(stateMachine)
            val allIntents = listOf(mainIntent)
                .union(primaryIntents)
                .union(secondaryIntents)
                .union(triggers)

            sm.getAllTransitions()
                .filterNot { allIntents.contains(it) }
                .map { MessageProvider.TRANSITION_NOT_FOUND(it) }
        }
    }

    /**
     * For each action declared in the TickStory,
     * we must find a state with the same name in the state machine
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateActions(tickStoryQuery: TickStoryQuery): List<String> {
        return with(tickStoryQuery) {
            val sm = StateMachine(stateMachine)
            actions
                .filter { sm.getState(it.name) == null }
                .map { MessageProvider.ACTION_NOT_FOUND(it.name) }
        }
    }

    /**
     * For each state of the state machine that is not a grouping state,
     * there must be an action of the same name in the TickStory
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateStates(tickStoryQuery: TickStoryQuery): List<String> {
        return with(tickStoryQuery) {
            val sm = StateMachine(stateMachine)
            sm.getAllStatesNotGroup()
                .filterNot { state -> actions.any { it.name == state } }
                .map { MessageProvider.STATE_NOT_FOUND(it) }
        }
    }

    /**
     * For each action declaring to execute business code,
     * we must find a class bearing the name of the handler declared in the action
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateActionHandlers(tickStoryQuery: TickStoryQuery): List<String> {
        return with(tickStoryQuery) {
            actions
                .filter { !it.handler.isNullOrBlank() }
                .filterNot { ActionHandlersRepository.contains(it.handler!!) }
                .map { MessageProvider.ACTION_HANDLER_NOT_FOUND(it.handler!!) }
        }
    }

    /**
     * Input and output contexts validation
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateInputOutputContexts(tickStoryQuery: TickStoryQuery): List<String> {
        val errors = mutableListOf<String>()

        val intentContexts = getIntentContexts(tickStoryQuery)
        val outputContext: (TickAction) -> Set<String> = { it.outputContextNames.union(intentContexts) }
        val inputContext: (TickAction) -> Set<String> = { it.inputContextNames.union(intentContexts) }

        // For each context declared as output to an action (or intent),
        // there must be at least one other action requiring the same context as input.
        errors.addAll(
            validateContexts(tickStoryQuery.actions, inputContext, outputContext).map {
                MessageProvider.INPUT_CTX_NOT_FOUND(it)
            }
        )

        // For each context declared as input to an action,
        // there must be at least one other action (or intent) producing the same context as output.
        errors.addAll(
            validateContexts(tickStoryQuery.actions, outputContext, inputContext).map {
                MessageProvider.OUTPUT_CTX_NOT_FOUND(it)
            }
        )

        return errors
    }

    /**
     * Get all context names declared inside the intentsContexts
     * @param tickStoryQuery: the tickStory to validate
     */
    private fun getIntentContexts(tickStoryQuery: TickStoryQuery): Set<String> {
        return tickStoryQuery.intentsContexts
            .flatMap { it.associations }
            .flatMap { it.contextNames }
            .toSet()
    }

    /**
     * Utility methode that returns the elements of the first set, except those that existed in the second
     *
     * @param actions: set ot [TickAction]
     * @param firstSet: first set
     * @param secondSet: second set
     */
    private fun validateContexts(
        actions: Set<TickAction>,
        firstSet: (TickAction) -> Set<String>,
        secondSet: (TickAction) -> Set<String>
    ): List<Pair<String, String>> {
        return actions.flatMap { action ->
            val contexts = firstSet(action)
            val contextsCompared = actions.filter { it.name != action.name }.flatMap { secondSet(it) }.toSet()
            contexts.minus(contextsCompared).map { Pair(it, action.name) }
        }
    }

    /**
     * Declared action contexts validation
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateDeclaredActionContexts(tickStoryQuery: TickStoryQuery): List<String> {
        val errors = mutableListOf<String>()

        val contextsUsed = tickStoryQuery.actions
            .flatMap { it.inputContextNames.union(it.outputContextNames) }
            .toSet()
        val contextDeclared = tickStoryQuery.contexts
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

    /**
     * Context names could not be used for Action handler name
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateNames(tickStoryQuery: TickStoryQuery): List<String> {
        return with(tickStoryQuery) {
            contexts
                .map { it.name }
                .intersect(actions.map { it.name }.toSet())
                .map { MessageProvider.ACTION_HANDLER_CTX_NAME_CONFLICT(it) }
        }
    }

    /**
     * Unknown config actions validation
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateUnknownConfigActions(tickStoryQuery: TickStoryQuery): List<String> =
        with(tickStoryQuery.unknownAnswerConfigs) {
            filterNot { tickStoryQuery.actions.map { act -> act.name }.contains(it.action) }
                .map { it.action }
                .map { MessageProvider.UNKNOWN_ACTION_NOT_FOUND(it) }
        }

    /**
     * Unknown config intents validation
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateUnknownConfigIntents(tickStoryQuery: TickStoryQuery) = with(tickStoryQuery.unknownAnswerConfigs) {
        filterNot { tickStoryQuery.secondaryIntents.contains(it.intent) }
            .map { it.intent }
            .map { MessageProvider.UNKNOWN_INTENT_NOT_IN_SECONDARY_INTENTS(it) }
    }

    /**
     * Target story validation
     *
     * @param tickStoryQuery: the tickStory to validate
     * @return list of error message
     */
    fun validateTargetStory(tickStoryQuery: TickStoryQuery, storyExistFn: (String) -> Boolean): List<String> =
        with(tickStoryQuery.actions) {
            filterNot { isValidTargetStory(it.targetStory, storyExistFn) }
                .map { MessageProvider.ACTION_TARGET_STORY_NOT_FOUND(it.name to it.targetStory!!) }
        }

    /**
     * The target story is valid only if the story exists
     *
     * @param storyExistFn the function to check if a storyDefinition exists by its storyId
     */
    private fun isValidTargetStory(targetStory: String?, storyExistFn: (String) -> Boolean): Boolean =
        targetStory?.let { storyExistFn(it) } ?: true

    /**
     * Apply all available validations
     */
    fun validateTickStory(tick: TickStoryQuery, storyExistFn: (String) -> Boolean): Set<String> =
        setOf(
            // Consistency between declared intents/triggers and the state machine transitions :
            validateIntents(tick),
            validateTriggers(tick),
            validateTransitions(tick),

            // Consistency between declared actions and the state machine states:
            validateActions(tick),
            validateStates(tick),

            // Consistency of action handlers :
            validateActionHandlers(tick),

            // Consistency of contexts :
            validateInputOutputContexts(tick),
            validateDeclaredActionContexts(tick),

            // Consistency of tick intents :
            validateTickIntentNames(tick),
            validateTickIntentAssociationActions(tick),
            validateDeclaredIntentContexts(tick),

            // Consistency of names :
            validateNames(tick),

            // Consistency of unknown config
            validateUnknownConfigActions(tick),

            validateUnknownConfigIntents(tick),

            // Consistency of targetStory
            validateTargetStory(tick, storyExistFn),
        ).flatten().toSet()

}





