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

object MessageProvider {
    val INTENT_NOT_FOUND: (String) -> String = {
        "Intent $it not found in StateMachine"
    }

    val TRIGGER_NOT_FOUND: (String) -> String = {
        "Trigger $it not found in StateMachine"
    }

    val NO_SECONDARY_INTENT_ASSOCIATED_TO_CTX: (String) -> String = {
        "Intent $it is not secondary, it cannot be associated to contexts"
    }

    val INTENT_CTX_ASSOCIATION_NOT_FOUND: (String) -> String = {
        "Intent association context $it not found in declared contexts"
    }

    val INTENT_ACTION_ASSOCIATION_NOT_FOUND: (String) -> String = {
        "Intent association action $it not found in declared actions"
    }
    val TRANSITION_NOT_FOUND: (String) -> String = {
        "Transition $it not found in TickStory intents or triggers"
    }

    val ACTION_NOT_FOUND: (String) -> String = {
        "Action $it not found in StateMachine"
    }

    val STATE_NOT_FOUND: (String) -> String = {
        "State $it not found in TickStory actions"
    }

    val ACTION_HANDLER_NOT_FOUND: (String) -> String = {
        "Action handler $it not found in handlers repository"
    }

    val INPUT_CTX_NOT_FOUND: (Pair<String, String>) -> String = {
        "Input context ${it.first} of action ${it.second} not found in output contexts of others"
    }

    val OUTPUT_CTX_NOT_FOUND: (Pair<String, String>) -> String = {
        "Output context ${it.first} of action ${it.second} not found in input contexts of others"
    }

    val ACTION_CTX_NOT_FOUND: (String) -> String = {
        "Action context $it not found in declared contexts"
    }

    val DECLARED_CTX_NOT_FOUND: (String) -> String = {
        "Declared context $it not found in actions"
    }

    val ACTION_HANDLER_CTX_NAME_CONFLICT: (String) -> String = {
        "The same name $it is used for Action handler and context"
    }

    val UNKNOWN_ACTION_NOT_FOUND: (String) -> String = {
        "Action $it defined for unknown configuration is not found in StateMachine"
    }

    val UNKNOWN_INTENT_NOT_IN_SECONDARY_INTENTS: (String) -> String = {
        "Unknown intent $it is not defined as a secondary intent"
    }

    val ACTION_TARGET_STORY_NOT_FOUND: (Pair<String, String>) -> String = { p ->
        "Target story (${p.second}) provided for action ${p.first} does not exist"
    }
}