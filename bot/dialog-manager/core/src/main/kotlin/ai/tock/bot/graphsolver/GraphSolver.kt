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
package ai.tock.bot.graphsolver

import ai.tock.bot.bean.TickAction
import ai.tock.bot.exception.NoClyngorResultsFound
import ai.tock.shared.property
import ai.tock.shared.resource
import jep.SharedInterpreter

/**
 * Class that handles the call to the Clyngor python library
 */
object GraphSolver {

    private val pythonPath = property("tock_bot_dialog_manager_python_path", resource("/python").path)
    private val pythonScriptPath = "$pythonPath/script"

    fun solve(
        debugEnabled: Boolean = false,
        currentState: String?,
        actions: Set<TickAction>,
        contexts: Map<String, String?>,
        objective: TickAction,
        ranHandlers: Set<String?>?
    ): List<String> {

        // Preparation of input data (Data conversion)
        val botActions = actions.map {
            PyAction(
                it.name,
                it.inputContextNames.toList(),
                it.outputContextNames.toList()
            )
        }
        val availableContexts = contexts.keys.map { PyContext(it) }
        val target = PyAction(
            objective.name,
            objective.inputContextNames.toList(),
            objective.outputContextNames.toList()
        )

        val results = SharedInterpreter().use { interp ->
            interp.set("pythonScriptPath", pythonScriptPath)

            interp.runScript("$pythonScriptPath/graph-solver.py")
            interp.invoke(
                "callClyngor",
                debugEnabled,
                currentState,
                botActions,
                target,
                availableContexts,
                ranHandlers
            )
        }

        return (results as List<String>).getIfNotEmpty()
    }

    private fun List<String>.getIfNotEmpty(): List<String>{
        if(this.isEmpty()) {
            throw NoClyngorResultsFound("No clyngor results found !")
        }
        return this
    }

}
