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

package ai.tock.bot.api.service

import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.api.model.configuration.ClientConfiguration
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.BotProvider
import ai.tock.bot.definition.BotProviderId
import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.BotRepository
import ai.tock.nlp.api.client.NlpClient
import ai.tock.nlp.api.client.model.dump.ApplicationDump
import ai.tock.shared.Executor
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.withoutNamespace
import mu.KotlinLogging

internal class BotApiDefinitionProvider(private val configuration: BotConfiguration) : BotProvider {

    private val logger = KotlinLogging.logger {}

    @Volatile
    private var lastConfiguration: ClientConfiguration? = null

    @Volatile
    private var bot: BotDefinition
    private val handler: BotApiHandler = BotApiHandler(this, configuration)

    private val executor: Executor get() = injector.provide()
    private val nlpClient: NlpClient get() = injector.provide()

    init {
        bot = BotApiDefinition(configuration, lastConfiguration, handler)
        handler.configuration {
            if (it != null) {
                updateIfConfigurationChange(it)
            }
            lastConfiguration = it
        }
    }

    fun updateIfConfigurationChange(conf: ClientConfiguration) {
        logger.debug { "check conf $conf" }
        if (conf != lastConfiguration) {
            logger.debug { "reload configuration" }
            this.lastConfiguration = conf
            logger.info { "configuration version :${lastConfiguration?.version}" }
            bot = BotApiDefinition(configuration, conf, handler)
            configurationUpdated = true
            registerBuiltinStoryIntents()
            BotRepository.registerBuiltInStoryDefinitions(this)
            BotRepository.checkBotConfigurations()
        }
    }

    private fun registerBuiltinStoryIntents() {
        executor.executeBlocking {
            with(botDefinition()) {
                val application = nlpClient.getApplicationByNamespaceAndName(namespace, nlpModelName)
                if (application != null) {
                    val applicationId = application._id
                    val intents = nlpClient.getIntentsByNamespaceAndName(namespace, botId)
                    if (!intents.isNullOrEmpty()) {
                        logger.info { "import builtin story intents for application $applicationId :$intents" }
                        nlpClient.importNlpPlainDump(
                            ApplicationDump(
                                application = application,
                                intents = intents.filter { it.name.withoutNamespace() != Intent.unknown.name.withoutNamespace() }
                                    .map {
                                        it.copy(
                                            name = it.name.withoutNamespace(),
                                            namespace = namespace,
                                            applications = it.applications.toMutableSet().apply { add(applicationId) },
                                            description = "Intent created automatically for built-in story.",
                                            category = "builtin"
                                        )
                                    }
                            )
                        )
                    }
                }
            }
        }
    }

    override fun botDefinition(): BotDefinition = bot

    override fun equals(other: Any?): Boolean = botProviderId == (other as? BotProvider)?.botProviderId

    override fun hashCode(): Int = botProviderId.hashCode()

    override val botProviderId: BotProviderId =
        BotProviderId(configuration.botId, configuration.namespace, configuration.name)

    @Volatile
    override var configurationUpdated: Boolean = true
}
