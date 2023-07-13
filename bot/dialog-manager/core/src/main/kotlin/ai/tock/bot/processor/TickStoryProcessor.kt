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
import ai.tock.bot.bean.TickActionHandlingStep
import ai.tock.bot.bean.TickConfiguration
import ai.tock.bot.bean.TickSession
import ai.tock.bot.bean.TickUserAction
import ai.tock.bot.bean.UnknownHandlingStep
import ai.tock.bot.exception.GlobalRootStateMachineNotFound
import ai.tock.bot.exception.InfiniteLoopException
import ai.tock.bot.exception.NextStateEquivalentException
import ai.tock.bot.exception.RetryExceededErrorException
import ai.tock.bot.exception.TickActionNotFoundException
import ai.tock.bot.graphsolver.GraphSolver
import ai.tock.bot.handler.ActionHandlersRepository
import ai.tock.bot.sender.TickSender
import ai.tock.bot.statemachine.StateMachine
import ai.tock.shared.booleanProperty
import mu.KotlinLogging
import java.util.Stack

// FIXME (WITH DERCBOT-321)
val debugEnabled = booleanProperty("tock_bot_dialog_manager_debug_enabled", false)

/**
 * A processor of tick story, it orchestrates the use of the state machine and the solver.
 * It takes a [TickSession] (current state, contexts,... ), a [TickSender] and a [T ickConfiguration]
 */
class TickStoryProcessor(
        private val session: TickSession,
        private val configuration: TickConfiguration,
        private val sender: TickSender,
        private val endingStoryRuleExists: Boolean = false) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val GLOBAL_STATE: String = "Global"
    }

    private var contexts = session.contexts.toMutableMap()
    private var objectivesStack = createStackFromList(session.objectivesStack)
    private var ranHandlers = session.ranHandlers.toMutableList()
    private val stateMachine: StateMachine = StateMachine(configuration.stateMachine)
    private var currentState = session.currentState ?: getInitialGlobalState()
    private var handlingStep = session.handlingStep
    private var unknownHandlingStep = session.unknownHandlingStep

    /**
     * the main function to process the user action, this is the core process.
     * @param tickUserAction [TickUserAction]
     * @return [ProcessingResult]
     */
    fun process (tickUserAction : TickUserAction?): ProcessingResult {
        logger.debug { "start of processing..." }
        logger.debug { "session: $session" }
        logger.debug { "tickUserAction: $tickUserAction" }

        // Handle unknown intent
        handleUnknown(tickUserAction)?.let { return it }

        // Update contexts by entities and intents
        tickUserAction?.let {
            updateContexts(it)
        }

        // Calculate the primary objective
        val primaryObjective: String = computePrimaryObjective(tickUserAction)
        logger.debug { "primaryObjective : $primaryObjective" }

        // Calculate the secondary objective
        val secondaryObjective = computeSecondaryObjective(primaryObjective)
        logger.debug { "secondaryObjective : $secondaryObjective" }

        try {
            updateHandlingStep(secondaryObjective, tickUserAction)
        } catch (e: InfiniteLoopException){
            logger.warn("abnormal end of processing. (infinite loop !)", e)
            return Success(computeNewSession())
        } catch (e: RetryExceededErrorException){
            logger.warn("end of processing with redirect result. (Configured redirect story: ${e.redirectedStoryId})", e)
            // when a redirection is performed, the current story state is considered as finished
            return Redirect(computeNewSession(finished = true), e.redirectedStoryId)
        }
        logger.debug { "lastExecutedAction : $handlingStep" }

        // Get the corresponding action
        val tickAction = getTickAction(secondaryObjective)

        // Execute the action corresponding of the secondary objective.
        execute(tickAction)

        // Update the current state
        updateCurrentState(primaryObjective, secondaryObjective)
        logger.debug { "currentState : $currentState" }

        // If the executed action has a non-null target story, then a redirection is required
        if(!tickAction.targetStory.isNullOrBlank()) {
            logger.debug { "end of processing with redirect result. (Action target story: ${tickAction.targetStory})" }
            return Redirect(
                // when a redirection is performed, the current story state is considered as finished
                session = computeNewSession(finished = true),
                storyId = tickAction.targetStory
            )
        }

        // FIXME (WITH DERCBOT-321)
        // To update graph. Not needed after DERCBOT-321
        if(debugEnabled) {
            GraphSolver.solve(
                true,
                ranHandlers.lastOrNull(),
                configuration.actions,
                contexts,
                getTickAction(primaryObjective),
                ranHandlers.toSet()
            ).random()
        }

        return  if (!tickAction.trigger.isNullOrBlank()) {
            // If the action has a trigger, the process method should be called with
            // a new [TickUserAction] that have an intent corresponding to the trigger
            logger.debug { "event ${tickAction.trigger} triggered. Restart a new processing round..." }
            process(TickUserAction(intentName = tickAction.trigger))
        } else  if(tickAction.isSilent()){
            // Else If the action is silent,
            // then we restart the processing again
            logger.debug { "silent action. Restart a new processing round..." }
            process(null)
        } else {
            // otherwise we send the results
            val updatedSession = computeNewSession(finished = tickAction.final)
            logger.debug { "end of processing with success result. session updated : $updatedSession" }
            Success(updatedSession)
        }
    }

    private fun updateHandlingStep(
        secondaryObjective: String,
        tickUserAction: TickUserAction?
    ) {
        logger.debug { "manage the last executed action..." }
        handlingStep = handlingStep?.let {
            // If the last executed action equals to the secondary objective,
            if (it.actionName == secondaryObjective) {
                when (tickUserAction) {
                    null -> {
                        // And If the user action is null then an infinite loop is detected
                        sender.end()
                        throw InfiniteLoopException("An infinite loop is detected.")
                    }
                    else -> {
                        with(configuration.storySettings) {
                            if (it.repeated > repetitionNb) {
                                throw RetryExceededErrorException("Action (${it.actionName}) is executed more than the allowed number of repetitions", redirectStory)
                            }
                        }
                        // Then the current handling step is incremented
                        it.next() as TickActionHandlingStep
                    }
                }
            } else null
            // Else, the last executed action is the computed secondary objective
        } ?: TickActionHandlingStep(1, secondaryObjective)
    }

    private fun computeNewSession(unknownHandlingStep: UnknownHandlingStep? = null, finished: Boolean =  false) =
        TickSession(
            currentState,
            contexts,
            ranHandlers,
            objectivesStack.toList(),
            session.initDate, // keep same init date
            unknownHandlingStep,
            handlingStep,
            finished
        )

    /**
     * Use of state machine to calculate the primary objective
     */
    private fun computePrimaryObjective(tickUserAction: TickUserAction?) =
        if (tickUserAction != null) {
            // Call to state machine to get the next state
            logger.debug { "get objective from the state machine" }
            val objectiveTemp = getStateMachineNextState(tickUserAction)
            objectivesStack.pushIfNotOnTop(objectiveTemp)
            objectiveTemp
        } else {
            logger.debug { "peek objective from the stack" }
            currentState = objectivesStack.peek()
            currentState
        }

    /**
     * Call to clyngor to get the secondary objective.
     * Randomly choose one among the multiple results
     */
    private fun computeSecondaryObjective(primaryObjective: String): String {
        logger.debug { "call clyngor graph solver to get potential objectives..." }

        val potentialObjectives: List<String> = GraphSolver.solve(
            debugEnabled,
            ranHandlers.lastOrNull(),
            configuration.actions,
            contexts,
            getTickAction(primaryObjective),
            ranHandlers.toSet()
        )

        logger.debug { "choosing the secondaryObjective randomly from potential objectives: $potentialObjectives" }
        return potentialObjectives.random()
    }

    /**
     * Call a state machine to get the Initial of Global state if it exists, or throws error exception
     */
    private fun getInitialGlobalState() =
        stateMachine.getInitial(GLOBAL_STATE)?.id ?: error("Initial of Global state not found <Global>")

    /**
     * Util function to create a [Stack] from a [List]
     * @param elements list elements
     * @return the created [Stack]
     */
    private fun createStackFromList(elements: List<String>): Stack<String> {
        val stack = Stack<String>()
        elements.forEach(stack::push)
        return stack
    }
    /**
     * Execute a given `action` [TickAction]
     * @param action [TickAction]
     */
    fun execute(action: TickAction) {
        debugInput(action)

        // manage answer
        if(!action.answerId.isNullOrBlank()) {
            logger.debug { "send action answer... (id : ${action.answerId})" }
            // send answer if provided
            if(endingStoryRuleExists && action.final || debugEnabled || action.isSilent()) {
                sender.sendById(action.answerId)
            } else {
                sender.endById(action.answerId)
            }
        } else if(!endingStoryRuleExists && action.final && !debugEnabled){
            // end the conversation if the final action has no ending story and no answer
            sender.end()
        }

        // invoke handler if provided
        if(!action.handler.isNullOrBlank()){
            logger.debug { "invoke action handler ${action.handler} with contexts $contexts" }
            contexts.putAll(
                ActionHandlersRepository.invoke(action.handler, contexts)
            )
        }

        debugOutput(action)

        // Add action name to already executed handlers
        ranHandlers.add(action.name)
    }

    /**
     * Debug the input and output contexts of the actions
     * @param action the tick action
     * @param type INPUT or OUTPUT
     */
    private fun getDebugMessage(action: TickAction, type: String): String {
        val contexts = contexts.map { (key, value) -> "$key : $value" }.joinToString(" | ")
        val message = "[DEBUG] ${action.name} : $type CONTEXTS [ $contexts ]"

        logger.info { message }

        return message
    }

    private fun debugInput(action: TickAction) {
        if(debugEnabled) {
            sender.sendPlainText(getDebugMessage(action, "INPUT"))
        }
    }

    private fun debugOutput(action: TickAction) {
        if(debugEnabled) {
            sender.sendPlainText(getDebugMessage(action, "OUTPUT"))
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
     * Update the current state according to a primary or secondary objective from the objective stack
     * @param primaryObjective primary objective name
     * @param secondaryObjective secondary objective name
     */
    private fun updateCurrentState(primaryObjective: String, secondaryObjective: String) {
        logger.debug { "Updating current state..." }
        currentState = when(primaryObjective){
            secondaryObjective -> {
                logger.debug { "pop the objective from the stack, because the primaryObjective is equals to secondaryObjective" }
                objectivesStack.pop()
            }
            else -> {
                logger.debug { "the secondaryObjective becomes the current state, because it is not equals to primaryObjective" }
                secondaryObjective
            }
        }
    }

    /**
     * Retrieve the next state machine state name from a [tickUserAction][TickUserAction]
     */
    private fun getStateMachineNextState(tickUserAction : TickUserAction): String {
        val nextState = stateMachine.getNext(currentState, tickUserAction.intentName)

        nextState?.let {
            if(it.id == currentState && isADirectTransition(tickUserAction.intentName)){
                throw NextStateEquivalentException("Next state shouldn't be equals to the current state <$currentState>")
            }
        }

        // if no state was found, then keep the current state
        val state = nextState?.id ?: currentState
        logger.debug { "next state : $state" }
        return state
    }

    /**
     *  Check if a transition is a father-son transition
     */
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
        logger.debug { "updating contexts by entities..." }
        updateContextsByEntities(tickUserAction.entities)
        logger.debug { "updating contexts by intents..." }
        updateContextsByIntent(tickUserAction.intentName)
        logger.debug { "contexts : $contexts" }
    }

    /**
     * Update contexts with entities value
     */
    private fun updateContextsByEntities(entities: Map<String, String?>) {
        configuration.contexts
            .filter { it.entityRole != null }
            .forEach {
                if(entities.contains(it.entityRole))
                    contexts[it.name] = entities[it.entityRole]
            }
    }

    /**
     * Update contexts by intent
     */
    private fun updateContextsByIntent(intentName: String) {
        contexts.putAll(configuration
            .intentsContexts
            .firstOrNull { it.intentName == intentName }
            ?.associations
            ?.firstOrNull { it.actionName == ranHandlers.lastOrNull()}
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
        return tickAction ?: throw TickActionNotFoundException("TickAction <$actionName> not found")
    }

    private fun handleUnknown(action: TickUserAction?): ProcessingResult? =
        if (ranHandlers.isNotEmpty() && configuration.unknownHandleConfiguration.unknownIntents().contains(action?.intentName)) {
            logger.debug { "handle unknown intent..." }
            val (step, redirectStoryId) = TickUnknownHandler.handle(
                lastExecutedActionName = ranHandlers.last(),
                unknownConfiguration = configuration.unknownHandleConfiguration,
                sender = sender,
                unknownHandlingStep = unknownHandlingStep,
                storySettings =  configuration.storySettings
            )

            if (step != null) {
                logger.debug { "handle unknown intent... success" }
                Success(computeNewSession(step))
            } else if (redirectStoryId != null) {
                logger.debug { "handle unknown intent... redirect to $redirectStoryId" }
                Redirect(
                    session = computeNewSession(),
                    storyId = redirectStoryId
                )
            } else null
        } else {
            null
        }
}