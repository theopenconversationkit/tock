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
     * The bot provider unique identifier.
     */
    val botProviderId: BotProviderId
        get() = botDefinition().let { BotProviderId(it.botId, it.namespace) }

    /**
     * Does this bot provider gets a configuration update ?
     */
    var configurationUpdated: Boolean
        get() = false
        set(_) {}
}
