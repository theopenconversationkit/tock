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

package ai.tock.bot.engine

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfigurationDAO
import ai.tock.bot.admin.bot.observability.BotObservabilityConfigurationDAO
import ai.tock.bot.admin.bot.rag.BotRAGConfigurationDAO
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.connector.Connector
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.engine.TestStoryDefinition.test
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.config.StoryConfigurationMonitor
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.nlp.NlpController
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserLock
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.nlp.api.client.NlpClient
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.api.client.model.EntityType
import ai.tock.nlp.api.client.model.NlpEntityValue
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.nlp.entity.StringValue
import ai.tock.shared.defaultLocale
import ai.tock.shared.injector
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import ai.tock.translator.I18nDAO
import ai.tock.translator.TranslatorEngine
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

/**
 *
 */
internal abstract class BotEngineTest {
    val userLock: UserLock = mockk(relaxed = true)
    val userTimelineDAO: UserTimelineDAO = mockk(relaxed = true)
    val userId = PlayerId("id")
    val botId = PlayerId("bot", PlayerType.bot)
    open val botDefinition: BotDefinition = BotDefinitionTest()
    val dialog = Dialog(setOf(userId, botId))
    val botApplicationConfiguration: BotApplicationConfiguration = mockk(relaxed = true)
    val connectorConfiguration: ConnectorConfiguration = mockk(relaxed = true)
    val story by lazy { Story(botDefinition.stories.first(), test.mainIntent()) }
    val connectorCallback: ConnectorCallback = mockk(relaxed = true)
    val connectorData = ConnectorData(connectorCallback)

    val botConfDAO: BotApplicationConfigurationDAO = mockk(relaxed = true)
    val botRAGConfigurationDAO: BotRAGConfigurationDAO = mockk(relaxed = true)
    val botVectorStoreConfigurationDAO: BotVectorStoreConfigurationDAO = mockk(relaxed = true)
    val i18nDAO: I18nDAO = mockk(relaxed = true)
    val translator: TranslatorEngine = mockk(relaxed = true)
    val storyDefinitionConfigurationDAO: StoryDefinitionConfigurationDAO = mockk(relaxed = true)
    val featureDAO: FeatureDAO = mockk(relaxed = true)
    val botObservabilityConfigurationDAO: BotObservabilityConfigurationDAO = mockk(relaxed = true)
    val botDocumentCompressorConfigurationDAO: BotDocumentCompressorConfigurationDAO = mockk(relaxed = true)
    val storyConfigurationMonitor: StoryConfigurationMonitor = spyk(StoryConfigurationMonitor(storyDefinitionConfigurationDAO))

    val entityA = Entity(EntityType("a"), "a")
    val entityAValue = NlpEntityValue(0, 1, entityA, null, false)
    val entityB = Entity(EntityType("a"), "b")
    val entityBValue = NlpEntityValue(2, 3, entityB, null, false)
    val entityC = Entity(EntityType("c"), "c")
    val entityCValue = NlpEntityValue(4, 5, entityC, null, false)
    val entityWithMergeSupport = Entity(EntityType("entityWithMergeSupport"), "entityWithMergeSupport")
    val entityWithMergeSupportValue1 = NlpEntityValue(6, 7, entityWithMergeSupport, StringValue("d"), mergeSupport = true)
    val entityWithMergeSupportValue2 = NlpEntityValue(8, 9, entityWithMergeSupport, StringValue("e"), mergeSupport = true)

    val nlpResult =
        NlpResult(
            test.name,
            "test",
            defaultLocale,
            listOf(entityAValue, entityBValue, entityCValue, entityWithMergeSupportValue1, entityWithMergeSupportValue2),
            emptyList(),
            1.0,
            1.0,
            "a b c d e",
            emptyMap(),
        )

    val nlpClient: NlpClient = mockk(relaxed = true)
    val nlp: NlpController = mockk(relaxed = true)
    val connector: Connector = mockk(relaxed = true)
    val userTimeline = UserTimeline(userId)

    var userAction = action(Sentence("ok computer"))

    val bus: BotBus by lazy {
        fillTimeline()
        TockBotBus(connectorController, userTimeline, dialog, userAction, connectorData, botDefinition)
    }

    open fun baseModule(): Kodein.Module {
        return Kodein.Module {
            import(sharedTestModule)
            bind<NlpClient>() with provider { nlpClient }
            bind<NlpController>() with provider { nlp }
            bind<UserLock>() with provider { userLock }
            bind<UserTimelineDAO>() with provider { userTimelineDAO }
            bind<I18nDAO>() with provider { i18nDAO }
            bind<TranslatorEngine>() with provider { translator }
            bind<BotApplicationConfigurationDAO>() with provider { botConfDAO }
            bind<StoryDefinitionConfigurationDAO>() with provider { storyDefinitionConfigurationDAO }
            bind<FeatureDAO>() with provider { featureDAO }
            bind<BotRAGConfigurationDAO>() with provider { botRAGConfigurationDAO }
            bind<BotVectorStoreConfigurationDAO>() with provider { botVectorStoreConfigurationDAO }
            bind<BotObservabilityConfigurationDAO>() with provider { botObservabilityConfigurationDAO }
            bind<BotDocumentCompressorConfigurationDAO>() with provider { botDocumentCompressorConfigurationDAO }
            bind<StoryConfigurationMonitor>() with provider { storyConfigurationMonitor }
        }
    }

    @BeforeEach
    fun before() {
        tockInternalInjector = KodeinInjector()
        injector.inject(
            Kodein {
                import(baseModule(), allowOverride = true)
            },
        )

        every { connector.loadProfile(any(), any()) } returns null
        every { connector.connectorType } returns ConnectorType("1")
    }

    @AfterEach
    fun after() {
        BotRepository.botProviders.clear()
        BotRepository.connectorProviders.clear()
        BotRepository.connectorControllerMap.clear()
        tockInternalInjector = KodeinInjector()
    }

    fun action(message: Message): Action = message.toAction(userId, "applicationId", botId)

    val registeredBus: BotBus? get() = (story.definition as TestStoryDefinition).registeredBus

    internal val bot: Bot by lazy {
        fillTimeline()
        Bot(botDefinition, botApplicationConfiguration)
    }
    internal val connectorController: TockConnectorController by lazy {
        TockConnectorController(
            bot,
            connector,
            BotVerticle(false, false),
            botDefinition,
            connectorConfiguration,
        )
    }

    private var timelineFilled = false

    fun fillTimeline() {
        if (!timelineFilled) {
            timelineFilled = true
            story.actions.add(userAction)
            dialog.stories.add(story)
            userTimeline.dialogs.add(dialog)
        }
    }
}
