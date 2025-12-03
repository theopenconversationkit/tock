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

package ai.tock.bot

import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.BotProvider
import ai.tock.bot.definition.BotProviderBase
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.nlp.NlpController
import ai.tock.nlp.api.client.model.dump.ApplicationDump
import ai.tock.nlp.api.client.model.dump.IntentDefinition
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.resource
import ai.tock.shared.resourceAsStream
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import io.vertx.ext.web.Router
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Register a new bot.
 */
private fun registerBot(botDefinition: BotDefinition) = registerBot(BotProviderBase(botDefinition))

/**
 * Register a new bot.
 */
private fun registerBot(botProvider: BotProvider) = BotRepository.registerBotProvider(botProvider)

/**
 * Register and install a new bot.
 */
fun registerAndInstallBot(
    botDefinition: BotDefinition,
    additionalModules: List<Kodein.Module> = emptyList(),
    vararg routerHandlers: (Router) -> Unit,
) {
    registerBot(botDefinition)
    installBots(routerHandlers.toList(), additionalModules)
}

/**
 * Register and install a new bot.
 */
fun registerAndInstallBot(
    botProvider: BotProvider,
    additionalModules: List<Kodein.Module> = emptyList(),
    vararg routerHandlers: (Router) -> Unit,
) {
    registerBot(botProvider)
    installBots(routerHandlers.toList(), additionalModules)
}

/**
 * Install the bot(s) with the specified additional router handlers and additional Tock Modules
 */
private fun installBots(
    routerHandlers: List<(Router) -> Unit>,
    additionalModules: List<Kodein.Module> = emptyList(),
) {
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
fun getIntentsByNamespaceAndName(
    namespace: String,
    name: String,
): List<IntentDefinition>? {
    val nlp: NlpController by injector.instance()
    nlp.waitAvailability()
    return nlp.getIntentsByNamespaceAndName(namespace, name)
}

/**
 * Import a dump of a full nlp model.
 * @path the dump path in the classpath
 */
fun importNlpDump(path: String) {
    val nlp: NlpController by injector.instance()
    nlp.waitAvailability()
    nlp.importNlpDump(resourceAsStream(path))
}

/**
 * Import a dump of a list of qualified sentences to a nlp model.
 * @path the dump path in the classpath
 */
fun importNlpSentencesDump(path: String) {
    val nlp: NlpController by injector.instance()
    nlp.waitAvailability()
    nlp.importNlpSentencesDump(resourceAsStream(path))
}

/**
 * Import a dump of all i18n labels.
 * @path the dump path in the classpath
 * @replaceAllLabels should preexisting labels be replaced?
 */
fun importI18nDump(
    path: String,
    replaceAllLabels: Boolean = false,
) {
    val i18n: I18nDAO by injector.instance()
    val labels: List<I18nLabel> = mapper.readValue(resource(path))

    if (replaceAllLabels) {
        i18n.save(labels)
    } else {
        i18n.saveIfNotExist(labels)
    }
}

/**
 * Import a dump of a full application.
 *
 * @path the dump path in the classpath
 */
fun importApplicationDump(path: String) {
    val nlp: NlpController by injector.instance()
    val application = mapper.readValue<ApplicationDump>(resource(path))
    val name = application.application.name
    logger.info { "Importing application '$name' to namespace '${application.application.namespace}'..." }

    if (nlp.importNlpPlainDump(application)) {
        logger.info { "Application '$name' successfully imported." }
    } else {
        logger.warn { "Application '$name' not imported (application might already exist)." }
    }
}
