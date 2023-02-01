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

package ai.tock.bot.processor

import ai.tock.bot.bean.*
import ai.tock.bot.graphsolver.GraphSolver
import ai.tock.bot.handler.ActionHandlersRepository
import ai.tock.bot.sender.TickSender
import ai.tock.bot.statemachine.StateMachine
import mu.KotlinLogging
import java.util.Stack

/**
 * A processor of tick story, it orchestrates the use of the state machine and the solver.
 * It takes a session (current state, contexts,... ), a sender and a tick configuration
 */
class TickStoryProcessor(
        session: TickSession,
        private val configuration: TickConfiguration,
        private val sender: TickSender,
        private val endingStoryRuleExists: Boolean) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private var contextNames = session.contexts.toMutableMap()
    private var objectivesStack = createStackFormList(session.objectivesStack)
    private var ranHandlers = session.ranHandlers.toMutableList()
    private val stateMachine: StateMachine = StateMachine(configuration.stateMachine)
    private var currentState = session.currentState ?: getGlobalState()
    private var unknownHandlingStep = session.unknownHandlingStep
    private var handlingStep = session.handlingStep ?: TickActionHandlingStep(0, currentState)

    /**
     * the main function to process the user action
     */
    fun process (tickUserAction : TickUserAction?): ProcessingResult {

        logger.debug("objectivesStack: $objectivesStack")

        // Handle unknown intent
        handleUnknown(tickUserAction)?.let {(session, redirectStoryId) ->
            if (session != null) return Success(session, false)
            if (redirectStoryId != null) return Redirect(redirectStoryId)
        }

        val primaryObjective: String = if(tickUserAction != null) {

            updateContexts(tickUserAction)

            // Call to state machine to get the next state
            val objectiveTemp = getStateMachineNextState(tickUserAction)
            objectivesStack.pushIfNotOnTop(objectiveTemp)
            objectiveTemp
        } else {
            currentState = objectivesStack.peek()
            currentState
        }

        // Call to clyngor to get the secondary objective.
        // Randomly choose one among the multiple results

        val secondaryObjective =  GraphSolver.solve(
            ranHandlers.lastOrNull(),
            configuration.actions,
            contextNames,
            getTickAction(primaryObjective),
            ranHandlers.toSet()
        ).random()

        /*
        Sets the current handling step
            If the current handling step has the same linked action as the secondary objective,
                then the current handling step is incremented
                    If the action is repeated more than the maximum number of repetitions
                        then a redirection is required
            Else, the current handling step is set to a new handling step with the secondary objective as linked action
        */
        handlingStep = if (handlingStep.action == secondaryObjective) {
            with(configuration.storySettings) {
                if (handlingStep.repeated > repetitionNb) {
                    return Redirect(redirectStory)
                }
            }
            handlingStep.next() as TickActionHandlingStep
        } else {
            TickActionHandlingStep(action =  secondaryObjective)
        }

        // TODO : End of recursion if tickUserAction = null and (primaryObjective,secondaryObjective) = last(primaryObjective,secondaryObjective)
        // TODO : A faire avec la JIRA DERCBOT-300  (voir commentaire sur la revue de code)

        // Execute the action corresponding of secondary objective.
        val executedAction = execute(secondaryObjective)

        // Update the current state
        updateCurrentState(primaryObjective, secondaryObjective)

        // TODO MASS à supprimer une fois le debug front est ok: c'est juste pour MAJ le graph
        GraphSolver.solve(
            ranHandlers.lastOrNull(),
            configuration.actions,
            contextNames,
            getTickAction(primaryObjective),
            ranHandlers.toSet()
        ).random()


        // If the action has a trigger, the process method should be called with a new [TickUserAction] that have an intent corresponding to the trigger
        // Else If the action is silent or if it should proceed, then we restart the processing again, otherwise we send the results
        return  if (executedAction.trigger != null) {
            process(TickUserAction(intentName = executedAction.trigger, emptyMap()))
        } else  if(executedAction.isSilent()){
            process(null)
        } else {
            Success(
                TickSession(currentState, contextNames, ranHandlers, objectivesStack.toList(), handlingStep = handlingStep),
                executedAction.final)
        }

    }

    /**
     * Call a state machine to get the Global state if it exists, or throws error exception
     */
    private fun getGlobalState() =
        stateMachine.getState("Global")?.id ?: error("Global state not found <Global>")

    private fun createStackFormList(elements: List<String>): Stack<String> {
        val stack = Stack<String>()
        elements.forEach(stack::push)
        return stack
    }
    /**
     * Execute a given tick action id
     */
    private fun execute(actionName: String): TickAction {
        val action = getTickAction(actionName)
        debugInput(action)

        // send answer if provided
        action.answerId?.let {
            if(endingStoryRuleExists && action.final || configuration.debug || action.isSilent()) {
                sender.sendById(it)
            } else {
                sender.endById(it)
            }
        }

        // invoke handler if provided
        if(!action.handler.isNullOrBlank()){
            contextNames.putAll(
                ActionHandlersRepository.invoke(action.handler, contextNames)
            )
        }

        debugOutput(action)

        // Add action name to already executed handlers
        ranHandlers.add(action.name)

        return action
    }

    /**
     * Debug the input and output contexts of the actions
     */
    private fun getDebugMessage(action: TickAction, type: String): String {
        val contexts = contextNames.map { (key, value) -> "$key : $value" }.joinToString(" | ")
        val message = "[DEBUG] ${action.name} : $type CONTEXTS [ $contexts ]"

        logger.info { message }

        return message
    }

    private fun debugInput(action: TickAction) {
        if(configuration.debug) {
            sender.sendPlainText(getDebugMessage(action, "INPUT"))
        }
    }

    private fun debugOutput(action: TickAction) {
        if(configuration.debug) {
            sender.sendPlainText(getDebugMessage(action, "OUTPUT"))
            sender.sendPlainText()
            if(!action.isSilent()){
                if(endingStoryRuleExists && action.final){
                    sender.sendPlainText("---")
                }else{
                    sender.endPlainText("---")
                }
            }
        }
    }

    private fun updateCurrentState(primaryObjective: String, secondaryObjective: String) {
        when(primaryObjective){
            secondaryObjective -> {
                currentState = objectivesStack.pop()
            }
            else -> currentState = secondaryObjective
        }
    }

    private fun getStateMachineNextState(tickUserAction : TickUserAction): String {
        val nextState = stateMachine.getNext(currentState, tickUserAction.intentName)

        nextState?.let {
            if(it.id == currentState && isADirectTransition(tickUserAction.intentName)){
                error("Next state shouldn't be equals to the current state <$currentState>")
            }
        }

        // if no state was found, then keep the current state
        return nextState?.id ?: currentState
    }

    // Check if a transition is a father-son transition
    private fun isADirectTransition(transition: String): Boolean
            = stateMachine.getState(currentState)?.on?.get(transition) != null

    private fun Stack<String>.pushIfNotOnTop(element: String) {
        if(this.isEmpty() || this.peek() != element) {
            this.push(element)
        }
    }

    /**
     * update contexts with [TickUserAction] data
     */
    private fun updateContexts(tickUserAction: TickUserAction){
        updateContextsByEntities(tickUserAction.entities)
        updateContextsByIntent(tickUserAction.intentName)
    }

    /**
     * Update contexts with entities value
     */
    private fun updateContextsByEntities(entities: Map<String, String?>) {
        configuration.contexts
            .filter { it.entityRole != null }
            .forEach {
                if(entities.contains(it.entityRole))
                    contextNames[it.name] = entities[it.entityRole]
            }
    }

    /**
     * Update contexts by intent
     */
    private fun updateContextsByIntent(intentName: String) {
        contextNames.putAll(configuration
            .intentsContexts
            .firstOrNull { it.intentName == intentName }
            ?.associations
            ?.firstOrNull { it.actionName == ranHandlers.last()}
            ?.contextNames
            ?.associateWith { null }
            ?: emptyMap()
        )
    }


    /**
     * Returns the tick action corresponding to the name,
     * or throws error exception if such a name is not present in the set.
     */
    private fun getTickAction(actionName: String): TickAction {
        val tickAction = configuration.actions.firstOrNull { it.name == actionName }
        return tickAction ?: error("TickAction <$actionName> not found")
    }

    private fun handleUnknown(action: TickUserAction?): Pair<TickSession?, String?>? =
        if (configuration.unknownHandleConfiguration.unknownIntents().contains(action?.intentName)) {
            val (step, redirectStoryId) = TickUnknownHandler.handle(
                lastExecutedActionName = ranHandlers.last(),
                unknownConfiguration = configuration.unknownHandleConfiguration,
                sender = sender,
                unknownHandlingStep = unknownHandlingStep,
                storySettings =  configuration.storySettings
            )

            if (step != null) {
                TickSession(currentState, contextNames, ranHandlers, objectivesStack.toList(), unknownHandlingStep = step, handlingStep = this.handlingStep) to null
            } else if (redirectStoryId != null) {
                 Pair(null, redirectStoryId)
            } else null
        } else {
            null
        }

}