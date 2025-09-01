/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

package ai.tock.bot.definition

import ai.tock.translator.UserInterfaceType

/**
 * Helper methods for StoryDefinition implementation.
 * Usually direct implementations are enums.
 * This interface add a starter (and main) intent with intent name equals to the property of value [name]
 * to the [StoryDefinition].
 * Warning: advanced usage only.
 */
interface StoryDefinitionExtended : StoryDefinition {

    val otherStarterIntents: Set<IntentAware> get() = emptySet()
    val secondaryIntents: Set<IntentAware> get() = emptySet()

    /**
     * StoryStep implementation could be an enum
     */
    val stepsArray: Array<out StoryStepDef> get() = emptyArray()
    override val steps: Set<StoryStepDef> get() = stepsArray.toSet()

    val unsupportedUserInterface: UserInterfaceType? get() = null
    override val unsupportedUserInterfaces: Set<UserInterfaceType> get() = listOfNotNull(unsupportedUserInterface).toSet()

    val name: String
    override val id: String get() = name
    override val starterIntents: Set<Intent> get() = setOf(Intent(name)) + otherStarterIntents.map { it.wrappedIntent() }.toSet()
    override val intents: Set<Intent> get() = setOf(Intent(name)) + (otherStarterIntents + secondaryIntents).map { it.wrappedIntent() }.toSet()
}
