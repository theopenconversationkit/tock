/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.admin

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfigurationDAO
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfigurationDAO
import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.admin.model.BotStoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.user.UserReportDAO
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.genai.orchestratorclient.services.VectorStoreProviderService
import ai.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import ai.tock.nlp.front.shared.ApplicationCodec
import ai.tock.nlp.front.shared.ApplicationConfiguration
import ai.tock.nlp.front.shared.ApplicationMonitor
import ai.tock.nlp.front.shared.ModelTester
import ai.tock.nlp.front.shared.ModelUpdater
import ai.tock.nlp.front.shared.Parser
import ai.tock.nlp.front.shared.codec.alexa.AlexaCodec
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale

abstract class AbstractTest {

    companion object {

        fun defaultModulesBinding(): Kodein.Module {
            val module = Kodein.Module {
                bind<ApplicationConfiguration>() with provider { mockk<ApplicationConfiguration>(relaxed = true) }
                bind<UserReportDAO>() with provider { mockk<UserReportDAO>(relaxed = true) }
                bind<DialogReportDAO>() with provider { mockk<DialogReportDAO>(relaxed = true) }
                bind<BotApplicationConfigurationDAO>() with provider { applicationConfigurationDAO }
                bind<ApplicationDefinitionDAO>() with provider { applicationDefininitionDAO }
                bind<FeatureDAO>() with provider { mockk<FeatureDAO>(relaxed = true) }
                bind<Parser>() with provider { mockk<Parser>(relaxed = true) }
                bind<ModelUpdater>() with provider { mockk<ModelUpdater>(relaxed = true) }
                bind<ApplicationCodec>() with provider { mockk<ApplicationCodec>(relaxed = true) }
                bind<AlexaCodec>() with provider { mockk<AlexaCodec>(relaxed = true) }
                bind<ApplicationMonitor>() with provider { mockk<ApplicationMonitor>(relaxed = true) }
                bind<ModelTester>() with provider { mockk<ModelTester>(relaxed = true) }
                bind<BotVectorStoreConfigurationDAO>() with provider { mockk<BotVectorStoreConfigurationDAO>(relaxed = true) }
                bind<VectorStoreProviderService>() with provider { mockk<VectorStoreProviderService>(relaxed = true) }
                bind<BotDocumentCompressorConfigurationDAO>() with provider { mockk<BotDocumentCompressorConfigurationDAO>(relaxed = true) }
            }
            return module
        }

        fun BotStoryDefinitionConfiguration.toStoryDefinitionConfiguration(): StoryDefinitionConfiguration {
            return StoryDefinitionConfiguration(
                storyId = storyId,
                botId = botId,
                intent = intent,
                currentType = currentType,
                namespace = namespace,
                answers = emptyList(),
                userSentenceLocale = userSentenceLocale,
                _id = _id
            )
        }

        fun newTestStory(
            storyId: String,
            type: AnswerConfigurationType,
            _id: Id<StoryDefinitionConfiguration> = newId(),
            name: String = storyId
        ): BotStoryDefinitionConfiguration {
            return BotStoryDefinitionConfiguration(
                storyId = storyId,
                botId = "testBotId",
                intent = IntentWithoutNamespace("testIntent"),
                currentType = type,
                namespace = "testNamespace",
                answers = emptyList(),
                userSentenceLocale = Locale.FRANCE,
                _id = _id,
                name = name
            )
        }

        val aApplication = BotApplicationConfiguration(
            applicationId = "testApplicationId",
            botId = "testBotId",
            namespace = "testNamespace",
            nlpModel = "testNlpModel",
            connectorType = ConnectorType.rest,
        )

        val aBuiltinStory = newTestStory("testBuiltinStory", AnswerConfigurationType.builtin)
        val aMessageStory = newTestStory("testMessageStory", AnswerConfigurationType.message)

        val storyDefinitionDAO: StoryDefinitionConfigurationDAO = mockk(relaxed = false)
        val applicationConfigurationDAO: BotApplicationConfigurationDAO = mockk(relaxed = false)
        val applicationDefininitionDAO: ApplicationDefinitionDAO = mockk(relaxed = false)

    }

    @BeforeEach
    internal open fun initMocks() {
        every { applicationConfigurationDAO.getConfigurationsByNamespaceAndBotId(any(), any()) } answers {
            listOf(
                aApplication
            )
        }
        every { storyDefinitionDAO.delete(any()) } returns Unit
        every { storyDefinitionDAO.save(any()) } returns Unit
    }

    @AfterEach
    internal fun clearMocks() {
        clearAllMocks()
    }
}

