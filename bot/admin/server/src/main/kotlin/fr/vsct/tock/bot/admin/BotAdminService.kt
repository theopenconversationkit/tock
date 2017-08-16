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
import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.admin.answer.SimpleAnswer
import fr.vsct.tock.bot.admin.answer.SimpleAnswerConfiguration
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfigurationDAO
import fr.vsct.tock.bot.admin.dialog.DialogReportDAO
import fr.vsct.tock.bot.admin.dialog.DialogReportQueryResult
import fr.vsct.tock.bot.admin.model.BotDialogRequest
import fr.vsct.tock.bot.admin.model.BotDialogResponse
import fr.vsct.tock.bot.admin.model.BotIntent
import fr.vsct.tock.bot.admin.model.BotIntentSearchRequest
import fr.vsct.tock.bot.admin.model.CreateBotIntentRequest
import fr.vsct.tock.bot.admin.model.DialogsSearchQuery
import fr.vsct.tock.bot.admin.model.UpdateBotIntentRequest
import fr.vsct.tock.bot.admin.model.UserSearchQuery
import fr.vsct.tock.bot.admin.model.UserSearchQueryResult
import fr.vsct.tock.bot.admin.test.toClientConnectorType
import fr.vsct.tock.bot.admin.test.toClientMessage
import fr.vsct.tock.bot.admin.user.UserReportDAO
import fr.vsct.tock.bot.connector.rest.client.ConnectorRestClient
import fr.vsct.tock.bot.connector.rest.client.model.ClientMessageRequest
import fr.vsct.tock.bot.connector.rest.client.model.ClientSentence
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.nlp.admin.AdminService
import fr.vsct.tock.nlp.admin.model.SentenceReport
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.service.applicationDAO
import fr.vsct.tock.nlp.front.shared.config.Classification
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.vertx.UnauthorizedException
import mu.KotlinLogging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 *
 */
object BotAdminService {

    private val logger = KotlinLogging.logger {}

    val defaultRestConnectorBaseUrl = property("tock_bot_admin_rest_default_base_url", "please set base url of the bot")
    val userReportDAO: UserReportDAO  by injector.instance()
    val dialogReportDAO: DialogReportDAO  by injector.instance()
    val applicationConfigurationDAO: BotApplicationConfigurationDAO  by injector.instance()
    val storyDefinitionDAO: StoryDefinitionConfigurationDAO by injector.instance()
    val restConnectorClientCache: MutableMap<String, ConnectorRestClient> = ConcurrentHashMap()
    val front = FrontClient

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

    fun searchUsers(query: UserSearchQuery): UserSearchQueryResult {
        return UserSearchQueryResult(userReportDAO.search(query.toUserReportQuery()))
    }

    fun search(query: DialogsSearchQuery): DialogReportQueryResult {
        return dialogReportDAO.search(query.toDialogReportQuery())
    }

    fun deleteApplicationConfiguration(conf: BotApplicationConfiguration) {
        applicationConfigurationDAO.delete(conf)
    }

    fun getBotConfigurationById(id: String): BotApplicationConfiguration? {
        return applicationConfigurationDAO.getConfigurationById(id)
    }

    fun getBotConfigurationByApplicationIdAndBotId(applicationId: String, botId: String): BotApplicationConfiguration? {
        return applicationConfigurationDAO.getConfigurationByApplicationIdAndBotId(applicationId, botId)
    }

    fun getBotConfigurationsByNamespaceAndBotId(namespace: String, botId: String): List<BotApplicationConfiguration> {
        return applicationConfigurationDAO
                .getConfigurations()
                .filter { it.namespace == namespace && it.botId == botId }
    }

    fun getBotConfigurationsByNamespaceAndBotConfigurationId(namespace: String, botConfigurationId: String): List<BotApplicationConfiguration> {
        return applicationConfigurationDAO
                .getConfigurations()
                .filter { it.namespace == namespace && it._id == botConfigurationId }
    }

    fun getBotConfigurationsByNamespaceAndNlpModel(namespace: String, applicationName: String): List<BotApplicationConfiguration> {
        val app = applicationDAO.getApplicationByNamespaceAndName(namespace, applicationName)
        return applicationConfigurationDAO
                .getConfigurations()
                .filter {
                    it.namespace == app?.namespace
                            && it.nlpModel == app.name
                }
    }

    fun saveApplicationConfiguration(conf: BotApplicationConfiguration) {
        applicationConfigurationDAO.save(conf.copy(manuallyModified = true))
    }

    fun loadBotIntents(request: BotIntentSearchRequest): List<BotIntent> {
        val botConf = getBotConfigurationsByNamespaceAndNlpModel(request.namespace, request.applicationName).firstOrNull()
        return if (botConf == null) {
            emptyList()
        } else {
            storyDefinitionDAO
                    .getStoryDefinitions(botConf.botId)
                    .mapNotNull {
                        val nlpApplication = front.getApplicationByNamespaceAndName(request.namespace, botConf.nlpModel)
                        val intent = front.getIntentByNamespaceAndName(request.namespace, it.intent.name)
                        if (intent != null) {
                            val query = SentencesQuery(
                                    nlpApplication!!._id!!,
                                    request.language,
                                    size = 3,
                                    intentId = intent._id)
                            val sentences =
                                    front.search(query)
                                            .sentences
                                            .map {
                                                SentenceReport(it)
                                            }
                            BotIntent(it, sentences)
                        } else {
                            logger.warn { "unknown intent: ${request.namespace} ${it.intent.name} - skipped" }
                            null
                        }
                    }
        }
    }

    fun deleteBotIntent(namespace: String, storyDefinitionId: String): Boolean {
        val story = storyDefinitionDAO.getStoryDefinitionById(storyDefinitionId)
        if (story != null) {
            val botConf = getBotConfigurationsByNamespaceAndBotId(namespace, story.botId).firstOrNull()
            if (botConf != null) {
                val nlpApplication = front.getApplicationByNamespaceAndName(namespace, botConf.nlpModel)
                val intent = front.getIntentByNamespaceAndName(namespace, story.intent.name)
                if (intent != null) {
                    front.removeIntentFromApplication(nlpApplication!!, intent._id!!)
                }
                storyDefinitionDAO.delete(story)
            }
        }
        return false
    }

    fun createBotIntent(
            namespace: String,
            request: CreateBotIntentRequest): IntentDefinition? {

        val botConf = getBotConfigurationsByNamespaceAndBotConfigurationId(namespace, request.botConfigurationId).firstOrNull()
        return if (botConf != null) {
            val nlpApplication = front.getApplicationByNamespaceAndName(namespace, botConf.nlpModel)
            val intentDefinition = AdminService.createOrUpdateIntent(
                    namespace,
                    IntentDefinition(
                            request.intent,
                            namespace,
                            setOf(nlpApplication!!._id!!),
                            emptySet()
                    )
            )
            if (intentDefinition != null) {
                storyDefinitionDAO.save(
                        StoryDefinitionConfiguration(
                                request.intent,
                                botConf.botId,
                                Intent(request.intent),
                                AnswerConfigurationType.simple,
                                listOf(
                                        SimpleAnswerConfiguration(
                                                listOf(SimpleAnswer(request.reply, 0))
                                        ))
                        )
                )
                request.firstSentences.forEach {
                    front.save(
                            ClassifiedSentence(
                                    it,
                                    request.language,
                                    nlpApplication._id!!,
                                    Instant.now(),
                                    Instant.now(),
                                    ClassifiedSentenceStatus.validated,
                                    Classification(intentDefinition._id!!, emptyList()),
                                    1.0,
                                    1.0
                            )
                    )
                }
            }
            intentDefinition
        } else {
            null
        }
    }

    fun updateBotIntent(
            namespace: String,
            request: UpdateBotIntentRequest): IntentDefinition? {

        val storyDefinition = storyDefinitionDAO.getStoryDefinitionById(request.storyDefinitionId)
        val botConf = getBotConfigurationsByNamespaceAndBotId(namespace, storyDefinition!!.botId).firstOrNull()
        return if (botConf != null) {
            storyDefinitionDAO.save(
                    storyDefinition.copy(answers = listOf(
                            SimpleAnswerConfiguration(
                                    listOf(SimpleAnswer(request.reply, 0))
                            ))))
            front.getIntentByNamespaceAndName(namespace, storyDefinition.intent.name)
        } else {
            null
        }
    }

    fun talk(request: BotDialogRequest): BotDialogResponse {
        val conf = getBotConfiguration(request.botApplicationConfigurationId, request.namespace)
        return try {
            val restClient = getRestClient(conf)
            val response = restClient.talk(conf.applicationId,
                    ClientMessageRequest(
                            "test_${conf._id}",
                            "test_bot_${conf._id}",
                            request.message.toClientMessage(),
                            conf.targetConnectorType.toClientConnectorType()))

            if (response.isSuccessful) {
                BotDialogResponse(response.body()?.messages ?: emptyList())
            } else {
                BotDialogResponse(listOf(ClientSentence("technical error :(")))
            }
        } catch (throwable: Throwable) {
            logger.error(throwable)
            BotDialogResponse(listOf(ClientSentence("technical error :(")))
        }
    }
}