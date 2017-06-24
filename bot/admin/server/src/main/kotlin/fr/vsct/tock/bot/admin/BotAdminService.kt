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

package fr.vsct.tock.bot.admin

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.admin.dialog.DialogReport
import fr.vsct.tock.bot.admin.dialog.DialogReportDAO
import fr.vsct.tock.bot.admin.model.BotDialogRequest
import fr.vsct.tock.bot.admin.model.BotDialogResponse
import fr.vsct.tock.bot.admin.model.UserSearchQuery
import fr.vsct.tock.bot.admin.user.UserReportDAO
import fr.vsct.tock.bot.admin.user.UserReportQueryResult
import fr.vsct.tock.bot.connector.rest.client.ConnectorRestClient
import fr.vsct.tock.bot.connector.rest.client.model.ClientMessageRequest
import fr.vsct.tock.bot.connector.rest.client.model.ClientSentence
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.service.applicationDAO
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.vertx.UnauthorizedException
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

/**
 *
 */
object BotAdminService {

    private val logger = KotlinLogging.logger {}

    val defaultRestConnectorBaseUrl = property("tock_bot_admin_rest_default_base_url", "please set base url of the bot")
    val front = FrontClient
    val userReportDAO: UserReportDAO  by injector.instance()
    val dialogReportDAO: DialogReportDAO  by injector.instance()
    val applicationConfigurationDAO: BotApplicationConfigurationDAO  by injector.instance()

    val restConnectorClientCache: MutableMap<String, ConnectorRestClient> = ConcurrentHashMap()

    fun getRestClient(conf: BotApplicationConfiguration): ConnectorRestClient {
        val baseUrl = conf.baseUrl?.let { if (it.isBlank()) null else it } ?: defaultRestConnectorBaseUrl
        return restConnectorClientCache.getOrPut(baseUrl) {
            ConnectorRestClient(baseUrl)
        }
    }

    fun getBotConfiguration(botApplicationConfigurationId: String, namespace: String): BotApplicationConfiguration {
        val conf = applicationConfigurationDAO.getConfigurationById(botApplicationConfigurationId)
        if (conf?.namespace != namespace) {
            throw UnauthorizedException()
        }
        return conf
    }

    fun searchUsers(query: UserSearchQuery): UserReportQueryResult {
        return userReportDAO.search(query.toSearchQuery(query.namespace, query.applicationName))
    }

    fun lastDialog(playerId: PlayerId): DialogReport {
        return dialogReportDAO.lastDialog(playerId)
    }

    fun deleteApplicationConfiguration(conf: BotApplicationConfiguration) {
        applicationConfigurationDAO.delete(conf)
    }

    fun getApplicationConfigurationById(id: String): BotApplicationConfiguration? {
        return applicationConfigurationDAO.getConfigurationById(id)
    }

    fun getConfigurationByApplicationIdAndBotId(applicationId: String, botId: String): BotApplicationConfiguration? {
        return applicationConfigurationDAO.getConfigurationByApplicationIdAndBotId(applicationId, botId)
    }

    fun saveApplicationConfiguration(conf: BotApplicationConfiguration) {
        applicationConfigurationDAO.save(conf.copy(manuallyModified = true))
    }

    fun getApplicationConfigurations(namespace: String, applicationName: String): List<BotApplicationConfiguration> {
        val app = applicationDAO.getApplicationByNamespaceAndName(namespace, applicationName)
        return applicationConfigurationDAO
                .getConfigurations()
                .filter {
                    it.namespace == app?.namespace
                            && it.nlpModel == app.name
                }
    }

    fun talk(request: BotDialogRequest): BotDialogResponse {
        val conf = getBotConfiguration(request.botApplicationConfigurationId, request.namespace)
        return try {
            val restClient = getRestClient(conf)
            val response = restClient.talk(conf.applicationId,
                    ClientMessageRequest(
                            "test_user",
                            "test_bot",
                            ClientSentence(request.text)))

            if (response.isSuccessful) {
                BotDialogResponse(response.body().messages)
            } else {
                BotDialogResponse(listOf(ClientSentence("technical error :(")))
            }
        } catch(throwable: Throwable) {
            logger.error(throwable)
            BotDialogResponse(listOf(ClientSentence("technical error :(")))
        }
    }
}