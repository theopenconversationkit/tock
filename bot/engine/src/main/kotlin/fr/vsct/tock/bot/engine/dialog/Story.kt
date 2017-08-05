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
import fr.vsct.tock.shared.error
import mu.KotlinLogging

/**
 *
 */
data class Story(
        val definition: StoryDefinition,
        var currentIntent: Intent?,
        var currentStep: String? = null,
        val actions: MutableList<Action> = mutableListOf()) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val lastAction: Action? get() = actions.lastOrNull()

    private fun StoryHandler.sendStartEvent(bus: BotBus) {
        BotRepository.storyHandlerListeners.forEach {
            try {
                it.startAction(bus, this)
            } catch(throwable: Throwable) {
                logger.error(throwable)
            }
        }
    }

    private fun StoryHandler.sendEndEvent(bus: BotBus) {
        BotRepository.storyHandlerListeners.forEach {
            try {
                it.endAction(bus, this)
            } catch(throwable: Throwable) {
                logger.error(throwable)
            }
        }
    }

    fun handle(bus: BotBus) {
        definition.storyHandler.apply {
            try {
                sendStartEvent(bus)
                handle(bus)
            } finally {
                sendEndEvent(bus)
            }
        }
    }


}