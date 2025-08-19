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

package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.genai.orchestratorclient.requests.PromptTemplate
import ai.tock.genai.orchestratorcore.models.em.OpenAIEMSetting
import ai.tock.genai.orchestratorcore.models.llm.OpenAILLMSetting
import ai.tock.shared.security.key.RawSecretKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 *
 */
internal class BotRAGConfigurationMongoDAOTest : AbstractTest() {
    @BeforeEach
    fun cleanup() {
        BotRAGConfigurationMongoDAO.col.drop()
    }

    @Test
    fun `save rag configuration`() {
        val config =
            BotRAGConfiguration(
                newId(),
                "namespace1",
                "botId1",
                false,
                questionAnsweringLlmSetting =
                    OpenAILLMSetting(
                        apiKey = RawSecretKey("apiKey1"),
                        model = "modelName1",
                        temperature = "1F",
                        baseUrl = "https://api.openai.com/v1",
                    ),
                questionAnsweringPrompt = PromptTemplate(template = "prompt template"),
                emSetting =
                    OpenAIEMSetting(
                        apiKey = RawSecretKey("apiKey1"),
                        model = "modelName1",
                        baseUrl = "https://api.openai.com/v1",
                    ),
            )

        BotRAGConfigurationMongoDAO.save(config)
        val configBDD = BotRAGConfigurationMongoDAO.findByNamespaceAndBotId("namespace1", "botId1")

        assertEquals(config, configBDD)
    }

    @Test
    fun `update rag configuration`() {
        val config1 =
            BotRAGConfiguration(
                newId(),
                "namespace1",
                "botId1",
                false,
                questionAnsweringLlmSetting =
                    OpenAILLMSetting(
                        apiKey = RawSecretKey("apiKey1"),
                        model = "modelName1",
                        baseUrl = "https://api.openai.com/v1",
                        temperature = "1F",
                    ),
                questionAnsweringPrompt = PromptTemplate(template = "prompt template"),
                emSetting =
                    OpenAIEMSetting(
                        apiKey = RawSecretKey("apiKey1"),
                        baseUrl = "https://api.openai.com/v1",
                        model = "modelName1",
                    ),
            )

        val config2 =
            BotRAGConfiguration(
                newId(),
                "namespace1",
                "botId2",
                false,
                questionAnsweringLlmSetting =
                    OpenAILLMSetting(
                        apiKey = RawSecretKey("apiKey1"),
                        model = "modelName1",
                        temperature = "1F",
                        baseUrl = "https://api.openai.com/v1",
                    ),
                questionAnsweringPrompt = PromptTemplate(template = "prompt template"),
                emSetting =
                    OpenAIEMSetting(
                        apiKey = RawSecretKey("apiKey1"),
                        baseUrl = "https://api.openai.com/v1",
                        model = "modelName1",
                    ),
            )

        assertNotEquals(config1, config2)

        BotRAGConfigurationMongoDAO.save(config1)
        BotRAGConfigurationMongoDAO.save(config2)

        BotRAGConfigurationMongoDAO.save(config1.copy(documentsRequired = true))

        val configBDD = BotRAGConfigurationMongoDAO.findByNamespaceAndBotId("namespace1", "botId1")

        assertEquals(config1.copy(documentsRequired = true), configBDD)
    }

    @Test
    fun `delete rag configuration`() {
        val config =
            BotRAGConfiguration(
                newId(),
                "namespace1",
                "botId1",
                false,
                questionAnsweringLlmSetting =
                    OpenAILLMSetting(
                        apiKey = RawSecretKey("apiKey1"),
                        model = "modelName1",
                        temperature = "1F",
                        baseUrl = "https://api.openai.com/v1",
                    ),
                questionAnsweringPrompt = PromptTemplate(template = "prompt template"),
                emSetting =
                    OpenAIEMSetting(
                        apiKey = RawSecretKey("apiKey1"),
                        baseUrl = "https://api.openai.com/v1",
                        model = "modelName1",
                    ),
            )

        BotRAGConfigurationMongoDAO.save(config)
        BotRAGConfigurationMongoDAO.delete(config._id)
        val configBDD = BotRAGConfigurationMongoDAO.findByNamespaceAndBotId("namespace1", "botId1")

        assertNull(configBDD)
    }
}
