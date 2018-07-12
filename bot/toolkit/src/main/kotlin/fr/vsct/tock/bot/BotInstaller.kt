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
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.BotProvider
import fr.vsct.tock.bot.definition.BotProviderBase
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.nlp.NlpController
import fr.vsct.tock.nlp.api.client.model.dump.IntentDefinition
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
 * Install the bot(s) with the specified additional router handlers and additional Tock Modules
 */
fun installBots(vararg routerHandlers: (Router) -> Unit, additionalModules: List<Kodein.Module> = emptyList()) {
    install(routerHandlers.toList(), additionalModules)
}

private fun install(routerHandlers: List<(Router) -> Unit>, additionalModules: List<Kodein.Module> = emptyList()) {
    BotIoc.setup(additionalModules)

    BotRepository.installBots(routerHandlers.toList())
}

/**
 * Export list of IntentDefinition
 *
 * @namespace Application Namespace
 * @name Application Name
 *
 * @return List of IntentDefinition
 */
fun getIntentsByNamespaceAndName(namespace: String, name: String): List<IntentDefinition>? {
    val nlp: NlpController by injector.instance()
    return nlp.getIntentsByNamespaceAndName(namespace, name)
}

/**
 * Import a dump of a full nlp model.
 * @path the dump path in the classpath
 */
fun importNlpDump(path: String) {
    val nlp: NlpController by injector.instance()
    nlp.importNlpDump(resourceAsStream(path))
}

/**
 * Import a dump of a list of qualified sentences to a nlp model.
 * @path the dump path in the classpath
 */
fun importNlpSentencesDump(path: String) {
    val nlp: NlpController by injector.instance()
    nlp.importNlpSentencesDump(resourceAsStream(path))
}

/**
 * Import a dump of all i18n labels.
 * @path the dump path in the classpath
 * @replaceAllLabels should preexisting labels be replaced?
 */
fun importI18nDump(path: String, replaceAllLabels: Boolean = false) {
    val i18n: I18nDAO by injector.instance()
    val labels: List<I18nLabel> = mapper.readValue(resource(path))
    if (replaceAllLabels) {
        i18n.save(labels)
    } else {
        i18n.saveIfNotExist(labels)
    }
}
