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

import ai.tock.bot.bean.TickAction
import ai.tock.bot.bean.TickConfiguration
import ai.tock.bot.bean.TickSession
import ai.tock.bot.bean.TickUserAction
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
    private var ranHandlers = session.ranHandlers.toMutableSet()
    private val stateMachine: StateMachine = StateMachine(configuration.stateMachine)
    private var currentState = session.currentState ?: getGlobalState()

    private val sousObjectifKey = "SOUS-OBJECTIF"

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
        action.handler?.let{
            contextNames.putAll(ActionHandlersRepository.invoke(it, contextNames))
        }

        debugOutput(action)
        ranHandlers.add(action.name)

        return action
    }


    /**
     * Debug the input and output contexts of the actions
     */
    // TODO : to be improved to comply with all types of connector
    // TODO : frontend : make a component for debug messages
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

    /**
     * the main function to process the user action
     */
    fun process (tickUserAction : TickUserAction?): Pair<TickSession, Boolean> {
        logger.debug("objectivesStack: $objectivesStack")

        val primaryObjective: String = if(tickUserAction != null) {
            updateContexts(tickUserAction)

            // Call to state machine to get the next state
            val objectiveTemp = getStateMachineNextState(tickUserAction).checkNotCurrentStateObjective()
            objectivesStack.pushIfNotOnTop(objectiveTemp)
            objectiveTemp
        } else {
            currentState = objectivesStack.peek()
            currentState
        }

        // Call to clyngor to get the secondary objective.
        // Randomly choose one among the multiple results
        val secondaryObjective = GraphSolver.solve(
            ranHandlers.lastOrNull() ?: "INIT", // TODO MASS supp INIT
            configuration.actions,
            contextNames,
            getTickAction(primaryObjective),
            ranHandlers
        ).random()

        // TODO : End of recursion if tickUserAction = null and (primaryObjective,secondaryObjective) = last(primaryObjective,secondaryObjective)
        // TODO : A faire avec la JIRA DERCBOT-300  (voir commentaire sur la revue de code)

        // Execute the action corresponding of secondary objective.
        val executedAction = execute(secondaryObjective)

        // Update the current state
        updateCurrentState(primaryObjective, secondaryObjective)

        // TODO MASS à supprimer une fois le debug front est ok: c'est juste pour MAJ le graph
        GraphSolver.solve(
            ranHandlers.last(),
            configuration.actions,
            contextNames,
            getTickAction(primaryObjective),
            ranHandlers
        ).random()

        // Dercbot 329 - Handle event if exists
//        executedAction.trigger?.let {
//            process(TickUserAction(intentName = it))
//        }
        // TODO MASS : Code à reprendre avec la JIRA des events
        contextNames[sousObjectifKey]?.let {
            objectivesStack.push(it)
            contextNames.remove(sousObjectifKey)
        }

        // If the action is silent or if it should proceed, then we restart the processing again, otherwise we send the results
        return if(executedAction.isSilent() || executedAction.proceed){
            process(null)
        }
        else {
            Pair(
                TickSession(currentState, contextNames, ranHandlers, objectivesStack.toList()),
                executedAction.final)
        }
    }

    private fun updateCurrentState(primaryObjective: String, secondaryObjective: String) {
        when(primaryObjective){
            secondaryObjective -> {
                currentState = objectivesStack.pop()
                if(!objectivesStack.isEmpty()){
                    currentState = objectivesStack.peek()!!
                }
            }
            else -> currentState = secondaryObjective
        }
    }

    private fun getStateMachineNextState(tickUserAction : TickUserAction): String {
        val nextState = stateMachine.getNext(currentState, tickUserAction.intentName)
        return nextState?.id.checkNotNullObjective()
    }

    private var checkNotNullObjective: String?.() -> String = {
        this ?: error("Next state not found")
    }

    private var checkNotCurrentStateObjective: String.() -> String = {
        if(this == currentState) {
            error("Next state shouldn't be equals to the current state <$currentState>")
        } else {
            this
        }
    }

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
            ?.firstOrNull { it.actionName == ranHandlers.last() }
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
}