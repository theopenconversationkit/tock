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

package fr.vsct.tock.bot

import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.BotProvider
import fr.vsct.tock.bot.definition.BotProviderBase
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.Nlp
import fr.vsct.tock.shared.resourceAsStream

/**
 * Register a new bot.
 */
fun registerBot(botDefinition: BotDefinition) = registerBot(BotProviderBase(botDefinition))

/**
 * Register a new bot.
 */
fun registerBot(botProvider: BotProvider) = BotRepository.registerBotProvider(botProvider)

/**
 * Install the bot(s).
 */
fun installBots() {
    BotIoc.setup()
    BotRepository.installBots()
}

/**
 * Import a dump of a nlp model.
 * @path the dump path in the classpath
 */
fun importNlpDump(path: String) {
    Nlp.importNlpDump(resourceAsStream(path))
}
