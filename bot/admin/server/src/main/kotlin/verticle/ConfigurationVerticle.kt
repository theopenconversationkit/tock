/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.admin.verticle

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.model.BotAdminConfiguration
import ai.tock.bot.admin.model.BotConnectorConfiguration
import ai.tock.bot.admin.service.BotAdminService
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.rest.addRestConnector
import ai.tock.bot.engine.BotRepository
import ai.tock.nlp.admin.model.ApplicationScopedQuery
import ai.tock.nlp.front.client.FrontClient
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.RequestSucceeded
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.toRequestHandler


class ConfigurationVerticle(val configuration: BotAdminConfiguration) : ChildVerticle<AdminException>{

    override fun configure(parent: WebVerticle<AdminException>) {
        with(parent) {

            blockingJsonGet("/configuration") {
                RequestSucceeded(configuration)
            }

            blockingJsonGet("/configuration/bots/:botId", setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context ->
                    BotAdminService.getBotConfigurationsByNamespaceAndBotId(context.organization, context.path("botId"))
                })

            blockingJsonPost("/configuration/bots", handler = toRequestHandler { context, query: ApplicationScopedQuery ->
                if (context.organization == query.namespace) {
                    BotAdminService.getBotConfigurationsByNamespaceAndNlpModel(query.namespace, query.applicationName)
                } else {
                    WebVerticle.unauthorized()
                }
            })

            blockingJsonPost(
                "/configuration/bot", TockUserRole.admin,
                logger = requestLogger<BotConnectorConfiguration, AdminException>("Create or Update Bot Connector Configuration") { _, c ->
                    c?.let { FrontClient.getApplicationByNamespaceAndName(it.namespace, it.nlpModel)?._id }
                },
                handler = toRequestHandler { context, bot: BotConnectorConfiguration ->
                    if (context.organization == bot.namespace) {
                        if (bot._id != null) {
                            val conf = BotAdminService.getBotConfigurationById(bot._id)
                            if (conf == null || bot.namespace != conf.namespace || bot.botId != conf.botId) {
                                WebVerticle.unauthorized()
                            }
                            if (BotAdminService.getBotConfigurationByApplicationIdAndBotId(
                                    bot.namespace,
                                    bot.applicationId,
                                    bot.botId
                                )
                                    ?.run { _id != conf._id } == true
                            ) {
                                WebVerticle.badRequest("Connector identifier already exists")
                            }
                        } else {
                            if (BotAdminService.getBotConfigurationByApplicationIdAndBotId(
                                    bot.namespace,
                                    bot.applicationId,
                                    bot.botId
                                ) != null
                            ) {
                                WebVerticle.badRequest("Connector identifier already exists")
                            }
                        }
                        bot.path?.let {
                            if (BotAdminService.getBotConfigurationsByNamespaceAndBotId(
                                    bot.namespace,
                                    bot.botId
                                ).any { conf -> conf._id != bot._id && conf.path?.lowercase() == it.lowercase() }
                            )
                                WebVerticle.badRequest("Connector path already exists (case-insensitive)")
                        }
                        val conf = bot.toBotApplicationConfiguration()
                        val connectorProvider = BotRepository.findConnectorProvider(conf.connectorType)
                        if (connectorProvider != null) {
                            val filledConf = if (bot.fillMandatoryValues) {
                                val additionalProperties = connectorProvider
                                    .configuration()
                                    .fields
                                    .filter { it.mandatory && !bot.parameters.containsKey(it.key) }
                                    .associate { it.key to "Please fill a value" }
                                conf.copy(parameters = conf.parameters + additionalProperties)
                            } else {
                                conf
                            }
                            connectorProvider.check(filledConf.toConnectorConfiguration())
                                .apply {
                                    if (isNotEmpty()) {
                                        WebVerticle.badRequest(joinToString())
                                    }
                                }
                            try {
                                BotAdminService.saveApplicationConfiguration(filledConf)
                                // add rest connector
                                if (bot._id == null && bot.connectorType != ConnectorType.rest) {
                                    addRestConnector(filledConf).apply {
                                        BotAdminService.saveApplicationConfiguration(
                                            BotApplicationConfiguration(
                                                connectorId,
                                                filledConf.botId,
                                                filledConf.namespace,
                                                filledConf.nlpModel,
                                                type,
                                                ownerConnectorType,
                                                getName(),
                                                getBaseUrl(),
                                                path = path,
                                                targetConfigurationId = conf._id
                                            )
                                        )
                                    }
                                }
                            } catch (t: Throwable) {
                                WebVerticle.badRequest("Error creating/updating configuration: ${t.message}")
                            }
                        } else {
                            WebVerticle.badRequest("unknown connector provider ${conf.connectorType}")
                        }
                    } else {
                        WebVerticle.unauthorized()
                    }
                })

            blockingJsonDelete(
                "/configuration/bot/:confId",
                setOf(TockUserRole.admin),
                simpleLogger("Delete Bot Configuration", { it.path("confId") to true }),
                handler = toRequestHandler { context ->
                    BotAdminService.getBotConfigurationById(context.pathId("confId"))
                        ?.let {
                            if (context.organization == it.namespace) {
                                BotAdminService.deleteApplicationConfiguration(it)
                                true
                            } else {
                                false
                            }
                        } ?: WebVerticle.unauthorized()
                })
        }
    }

}