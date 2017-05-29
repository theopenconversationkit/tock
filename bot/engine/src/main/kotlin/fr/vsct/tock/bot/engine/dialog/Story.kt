/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.engine.dialog

import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.definition.StoryHandler
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.action.Action

/**
 *
 */
data class Story(
        val definition: StoryDefinition,
        var currentIntent: Intent?,
        val actions: MutableList<Action> = mutableListOf()) {

    val lastAction: Action? get() = actions.lastOrNull()

    private fun StoryHandler.sendStartEvent(bus: BotBus) {
        BotRepository.storyHandlerListeners.forEach {
            it.startAction(bus, this)
        }
    }

    private fun StoryHandler.sendEndEvent(bus: BotBus) {
        BotRepository.storyHandlerListeners.forEach {
            it.endAction(bus, this)
        }
    }

    fun handle(bus: BotBus) {
        definition.storyHandler.apply {
            sendStartEvent(bus)
            handle(bus)
            sendEndEvent(bus)
        }
    }


}