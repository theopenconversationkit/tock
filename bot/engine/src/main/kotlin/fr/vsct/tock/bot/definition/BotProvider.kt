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

package fr.vsct.tock.bot.definition

/**
 * Provides a specific type of [BotDefinition].
 * Custom provider should usually not directly extend this class, but instead extend [BotProviderBase].
 */
interface BotProvider {

    /**
     * Provides the bot definition.
     */
    fun botDefinition(): BotDefinition

    /**
     * The bot unique identifier.
     */
    fun botId(): String = botDefinition().botId

    /**
     * Is this [BotProvider] specific to a configuration name?
     * If yes, this property stores the configuration name.
     * If no, this property returns null.
     * By default the¬ property returns null.
     */
    val configurationName: String? get() = null

    /**
     * Does this bot provider gets a configuration update ?
     */
    var configurationUpdated: Boolean
        get() = false
        set(v) {}


}