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

package ai.tock.bot.handler

import ai.tock.bot.HandlerNamespace
import java.text.MessageFormat

const val ERR_INPUT_CONTEXT_NOT_PROVIDED  = "{0} - At least one declared context was not provided {1}"
const val ERR_OUTPUT_CONTEXT_NOT_DECLARED = "{0} - At least one computed context was not declared {1}"
const val ERR_NO_OUTPUT_CONTEXT_COMPUTED  = "{0} - No output context was computed. Expected {1}"

data class ActionHandler(
    val id: String,
    val namespace: HandlerNamespace,
    val name: String = "${namespace.key}:${id.lowercase()}",
    val description: String?,
    val inputContexts: Set<String>,
    val outputContexts: Set<String>,
    private val handler: (Map<String, String?>) -> Map<String, String?>
) {
    fun invokeHandler(providedInputContexts: Map<String, String?>): Map<String, String?> {
        // Check input contexts
        checkThatDeclaredInputContextsAreAllProvided(providedInputContexts.keys)

        // Invoke handler
        val computedOutputContexts = handler.invoke(providedInputContexts)

        // Check output contexts
        checkThatComputedOutputContextsAreAllDeclared(computedOutputContexts.keys)
        checkThatAtLeastOneContextIsComputed(computedOutputContexts.keys)

        return computedOutputContexts
    }

    private fun checkThatDeclaredInputContextsAreAllProvided(providedInputContextNames: Set<String>) {
        containsAll(providedInputContextNames, inputContexts, ERR_INPUT_CONTEXT_NOT_PROVIDED)
    }

    private fun checkThatComputedOutputContextsAreAllDeclared(computedOutputContextNames: Set<String>) {
        containsAll(outputContexts, computedOutputContextNames, ERR_OUTPUT_CONTEXT_NOT_DECLARED)
    }

    private fun containsAll(container: Set<String>, content: Set<String>, errorMessage : String) {
        require(container.containsAll(content)) {
            MessageFormat.format(
                errorMessage,
                name,
                content.minus(container)
            )
        }
    }

    private fun checkThatAtLeastOneContextIsComputed(computedOutputContextNames: Set<String>) {
        if(outputContexts.isNotEmpty()){
            require(computedOutputContextNames.isNotEmpty()) {
                MessageFormat.format(
                    ERR_NO_OUTPUT_CONTEXT_COMPUTED,
                    name,
                    outputContexts
                )
            }
        }
    }
}