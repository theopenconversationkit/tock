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

package ai.tock.bot.admin.test

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.test.model.BotDialogRequest
import ai.tock.bot.admin.test.model.BotDialogResponse
import ai.tock.bot.connector.rest.client.ConnectorRestClient
import ai.tock.bot.connector.rest.client.model.ClientMessageRequest
import ai.tock.bot.connector.rest.client.model.ClientSentence
import ai.tock.bot.engine.message.Message
import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import ai.tock.shared.exception.rest.UnauthorizedException
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.provide
import mu.KotlinLogging
import org.litote.kmongo.Id
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object TestTalkService {
    private val logger = KotlinLogging.logger {}
    private val defaultRestConnectorBaseUrl =
        property("tock_bot_admin_rest_default_base_url", "please set base url of the bot")
    private val restConnectorClientCache: MutableMap<String, ConnectorRestClient> = ConcurrentHashMap()

    fun talk(
        request: BotDialogRequest,
        debugEnabled: Boolean,
        sourceWithContent: Boolean,
    ): BotDialogResponse =
        talk(
            botApplicationConfigurationId = request.botApplicationConfigurationId,
            namespace = request.namespace,
            message = request.message,
            language = request.currentLanguage,
            userIdModifier = request.userIdModifier,
            debugEnabled = debugEnabled,
            sourceWithContent = sourceWithContent,
        )

    fun talk(
        botApplicationConfigurationId: Id<BotApplicationConfiguration>,
        namespace: String,
        message: Message,
        language: Locale?,
        userIdModifier: String,
        debugEnabled: Boolean,
        sourceWithContent: Boolean,
    ): BotDialogResponse {
        val conf = getBotConfiguration(botApplicationConfigurationId, namespace)
        return try {
            val restClient = getRestClient(conf)
            val response =
                restClient.talk(
                    conf.path ?: conf.applicationId,
                    language ?: defaultLocale,
                    ClientMessageRequest(
                        buildTestPlayerId(conf._id, language, userIdModifier),
                        buildTestBotId(conf._id, language),
                        message.toClientMessage(),
                        conf.targetConnectorType.toClientConnectorType(),
                        test = true,
                        debugEnabled = debugEnabled,
                        sourceWithContent = sourceWithContent,
                    ),
                )

            if (response.isSuccessful) {
                response.body()?.run {
                    BotDialogResponse(messages, userLocale, userActionId, hasNlpStats)
                } ?: BotDialogResponse(emptyList())
            } else {
                logger.error { "error with $conf : ${response.errorBody()?.string()}" }
                BotDialogResponse(listOf(ClientSentence("technical error :( ${response.errorBody()?.string()}]")))
            }
        } catch (throwable: Throwable) {
            logger.error(throwable)
            BotDialogResponse(listOf(ClientSentence("technical error :( ${throwable.message}")))
        }
    }

    fun getRestClient(conf: BotApplicationConfiguration): ConnectorRestClient {
        val baseUrl = conf.baseUrl?.let { if (it.isBlank()) null else it } ?: defaultRestConnectorBaseUrl
        return restConnectorClientCache.getOrPut(baseUrl) {
            ConnectorRestClient(baseUrl)
        }
    }

    fun buildTestPlayerId(
        botApplicationConfigurationId: Id<BotApplicationConfiguration>,
        language: Locale?,
        userIdModifier: String,
    ): String = "test_${botApplicationConfigurationId}_${language ?: defaultLocale}_$userIdModifier"

    fun buildTestBotId(
        botApplicationConfigurationId: Id<BotApplicationConfiguration>,
        language: Locale?,
    ): String = "test_bot_${botApplicationConfigurationId}_${language ?: defaultLocale}"

    fun getBotConfiguration(
        botApplicationConfigurationId: Id<BotApplicationConfiguration>,
        namespace: String,
    ): BotApplicationConfiguration {
        val applicationConfigurationDAO: BotApplicationConfigurationDAO = injector.provide()
        val conf = applicationConfigurationDAO.getConfigurationById(botApplicationConfigurationId)
        if (conf?.namespace != namespace) {
            throw UnauthorizedException()
        }
        return conf
    }
}
