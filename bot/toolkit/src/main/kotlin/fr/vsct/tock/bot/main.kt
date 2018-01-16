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

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.rest.addRestConnector
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.BotProvider
import fr.vsct.tock.bot.definition.BotProviderBase
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.nlp.NlpController
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.resource
import fr.vsct.tock.shared.resourceAsStream
import fr.vsct.tock.translator.I18nDAO
import fr.vsct.tock.translator.I18nLabel
import io.vertx.ext.web.Router

/**
 * Register a new bot.
 */
fun registerBot(botDefinition: BotDefinition) = registerBot(BotProviderBase(botDefinition))

/**
 * Register a new bot.
 */
fun registerBot(botProvider: BotProvider) = BotRepository.registerBotProvider(botProvider)

/**
 * Register and install a new bot.
 */
fun registerAndInstallBot(botDefinition: BotDefinition) {
    registerBot(botDefinition)
    installBots()
}

/**
 * Register and install a new bot.
 */
fun registerAndInstallBot(botProvider: BotProvider) {
    registerBot(botProvider)
    installBots()
}

/**
 * Install the bot(s) with the specified additional router handlers.
 */
fun installBots(vararg routerHandlers: (Router) -> Unit) {
    install(routerHandlers.toList(), true)
}

private fun install(routerHandlers: List<(Router) -> Unit>, installRestConnectors: Boolean) {
    BotIoc.setup()

    BotRepository.installBots(routerHandlers.toList()) { conf ->
        if (installRestConnectors && conf.connectorType != ConnectorType.rest) {
            addRestConnector(conf)
        } else {
            null
        }
    }
}

/**
 * Import a dump of a nlp model.
 * @path the dump path in the classpath
 */
fun importNlpDump(path: String) {
    val nlp: NlpController by injector.instance()
    nlp.importNlpDump(resourceAsStream(path))
}

/**
 * Import a dump of all i18n labels.
 * @path the dump path in the classpath
 */
fun importI18nDump(path: String) {
    val i18n: I18nDAO by injector.instance()
    val labels: List<I18nLabel> = mapper.readValue(resource(path))
    i18n.save(labels)
}
