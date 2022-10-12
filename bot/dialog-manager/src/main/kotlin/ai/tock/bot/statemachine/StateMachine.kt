package ai.tock.bot.statemachine

import mu.KotlinLogging

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


/**
 * Class that manages access to the state machine - Implementation of xstate.js.org
 */
class StateMachine(val root: State) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        require(hasNoLoop()) {
            "The state machine must have no loops"
        }
    }

    private fun getLoopAtNode(id: String, stateMachine: State): State? {
        if(id == stateMachine.id) return stateMachine

        val state = stateMachine.states?.firstNotNullOfOrNull {
            entry -> if(entry.value.id == id) entry.value else null
        }
        return state ?: stateMachine.states?.firstNotNullOfOrNull { getLoopAtNode(id, it.value) }
    }

    private fun getLoopForNode(stateMachine: State): State? {
        return stateMachine.states?.firstNotNullOfOrNull { getLoopAtNode(stateMachine.id, it.value) }
    }

    private fun getLoop(): State? {
        val state = getLoop(root)
        if(state != null) logger.warn { "A loop has been detected, caused by ${state.id}" }
        return state
    }

    private fun getLoop(stateMachine: State): State? {
        val state = getLoopForNode(stateMachine)
        return state ?: stateMachine.states?.firstNotNullOfOrNull { getLoop(it.value) }
    }

    private fun hasLoop(): Boolean = getLoop() != null

    private fun hasNoLoop(): Boolean = !hasLoop()

    fun getState(id: String): State? = getState(id, root)

    private fun getState(id: String, stateMachine: State): State? =
        when(stateMachine.id){
            id -> stateMachine
            else -> stateMachine.states?.firstNotNullOfOrNull { getState(id, it.value) }
        }

    // Get the initial state of state machine.
    fun getInitial(id: String): State? = getInitial(id, root)

    private fun getInitial(id: String, stateMachine: State): State? {
        val state = getState(id, stateMachine)
        // If initial state is a group (has substates), we get the initial of the group
        // A group always has an initial state
        return when(state?.states?.isEmpty()){
            null, true -> state
            else -> getInitial(state.initial!!, state)
        }
    }

    fun getParent(id: String): State? = getParent(id, root)

    private fun getParent(id: String, stateMachine: State): State? =
        when(stateMachine.states?.any { it.value.id == id }){
            true -> stateMachine
            else -> stateMachine.states?.firstNotNullOfOrNull { getParent(id, it.value) }
        }

    // Get the next state following the transition
    fun getNext(id: String, transition: String): State? = getNext(id, transition, root)

    private fun getNext(id: String, transition: String, stateMachine: State): State? {
        val currentState = getState(id, stateMachine)
        return currentState?.let {
            // Drop the first char (#), because the "on" data structure is :
            // "on": { "transition": "#ID", ... }
            when(val nextStateId = currentState.on?.get(transition)?.drop(1)){
                // Transition not found, continue search transition from parent
                null -> getParent(id)?.let {
                    getNext(it.id, transition, stateMachine)
                }
                // Transition found, get state from current or parent
                else -> getStateFromCurrentOrParent(nextStateId, stateMachine)?.let {
                    getInitial(nextStateId, it)
                }

            }
        }
    }

    /**
     * Search the state from current. If not exist, search it from the parent
     */
    private fun getStateFromCurrentOrParent(id: String, stateMachine: State): State? =
        when(val currentState = getState(id, stateMachine)){
            null -> getParent(id)?.let {
                getStateFromCurrentOrParent(id, it)
            }
            else -> currentState
        }

    // Check if state machine has a transition
    fun containsTransition(transition: String): Boolean = containsTransition(transition, root)

    private fun containsTransition(transition: String, stateMachine: State): Boolean =
        if(stateMachine.on?.get(transition) != null){
            true
        } else if(stateMachine.states == null){
            false
        } else {
            stateMachine.states.any { containsTransition(transition, it.value) }
        }

    fun getAllTransitions(): Set<String> = getAllTransitions(root)

    private fun getAllTransitions(stateMachine: State): Set<String> {
        val transitions = mutableSetOf<String>()

        transitions.addAll(stateMachine.on?.keys ?: emptySet())
        stateMachine.states?.values?.forEach {
            transitions.addAll(getAllTransitions(it))
        }

        return transitions.toSet()
    }

    fun getAllStatesNotGroup(): Set<String> = getAllStatesNotGroup(root)

    private fun getAllStatesNotGroup(stateMachine: State): Set<String> {
        val ids = mutableSetOf<String>()

        if(stateMachine.states == null || stateMachine.states.isEmpty()){
            ids.add(stateMachine.id)
        } else {
            stateMachine.states.values.forEach {
                ids.addAll(getAllStatesNotGroup(it))
            }
        }

        return ids.toSet()
    }
}
