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

package ai.tock.bot.test

import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.test.TestPlanDAO
import ai.tock.bot.admin.user.UserReportDAO
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotAnswerInterceptor
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryHandlerListener
import ai.tock.bot.engine.dialog.DialogFlowDAO
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.nlp.NlpController
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserLock
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.nlp.api.client.NlpClient
import ai.tock.shared.defaultLocale
import ai.tock.shared.injector
import ai.tock.shared.sharedTestModule
import ai.tock.translator.I18nDAO
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.mockkClass
import java.util.Locale
import kotlin.reflect.KClass

/**
 * A test context initialized for each test.
 */
open class TestContext {
    /**
     * Creates a new mock.
     */
    inline fun <reified T : Any> newMock() = newMock(T::class)

    /**
     * Creates a new mock.
     */
    open fun <T : Any> newMock(kClass: KClass<T>): T = mockkClass(kClass, relaxed = true)

    /**
     * The context of the bus.
     */
    lateinit var botBusMockContext: BotBusMockContext

    /**
     * Default mocked [I18nDAO].
     */
    var mockedI18nDAO: I18nDAO = newMock()

    /**
     * Default mocked [NlpClient].
     */
    var mockedNlpClient: NlpClient = newMock()

    /**
     * Default newMocked [NlpController].
     */
    var mockedNlpController: NlpController = newMock()

    /**
     * Default mocked [BotApplicationConfigurationDAO].
     */
    var mockedBotApplicationConfigurationDAO: BotApplicationConfigurationDAO = newMock()

    /**
     * Default mocked [StoryDefinitionConfigurationDAO].
     */
    var mockedStoryDefinitionConfigurationDAO: StoryDefinitionConfigurationDAO = newMock()

    /**
     * Default mocked [UserTimelineDAO].
     */
    var mockedUserTimelineDAO: UserTimelineDAO = newMock()

    /**
     * Default mocked [UserReportDAO].
     */
    var mockedUserReportDAO: UserReportDAO = newMock()

    /**
     * Default mocked [DialogReportDAO].
     */
    var mockedDialogReportDAO: DialogReportDAO = newMock()

    /**
     * Default mocked [TestPlanDAO].
     */
    var mockedTestPlanDAO: TestPlanDAO = newMock()

    /**
     * Default mocked [UserLock].
     */
    var mockedUserLock: UserLock = newMock()

    /**
     * Default mocked [UserLock].
     */
    var mockedFeatureDAO: FeatureDAO = newMock()

    /**
     * Default mocked [UserLock].
     */
    var mockedDialogFlowDAO: DialogFlowDAO = newMock()

    /**
     * The test [Kodein] injected.
     */
    val testKodein: Kodein
        by lazy {
            Kodein {
                importModule().invoke(this)
            }
        }

    /**
     * The story handler listeners to apply.
     */
    val storyHandlerListeners: MutableList<StoryHandlerListener> = mutableListOf()

    /**
     * The bot answer interceptors to apply.
     */
    val botAnswerInterceptors: MutableList<BotAnswerInterceptor> = mutableListOf()

    /**
     * Default mocked Tock Ioc.
     */
    open fun importModule(): Kodein.Builder.() -> Unit =
        {
            import(sharedTestModule, true)
            import(testTranslatorModule, true)
            import(
                Kodein.Module {
                    bind<I18nDAO>() with provider { mockedI18nDAO }

                    bind<NlpClient>() with provider { mockedNlpClient }
                    bind<NlpController>() with provider { mockedNlpController }

                    bind<BotApplicationConfigurationDAO>() with provider { mockedBotApplicationConfigurationDAO }
                    bind<StoryDefinitionConfigurationDAO>() with provider { mockedStoryDefinitionConfigurationDAO }
                    bind<UserTimelineDAO>() with provider { mockedUserTimelineDAO }
                    bind<UserReportDAO>() with provider { mockedUserReportDAO }
                    bind<DialogReportDAO>() with provider { mockedDialogReportDAO }
                    bind<TestPlanDAO>() with provider { mockedTestPlanDAO }
                    bind<UserLock>() with provider { mockedUserLock }
                    bind<FeatureDAO>() with provider { mockedFeatureDAO }
                    bind<DialogFlowDAO>() with provider { mockedDialogFlowDAO }
                },
            )
            testModules.forEach { import(it, true) }
        }

    /**
     * [KodeinInjector] used in tests.
     */
    val testInjector: KodeinInjector by lazy { createTestInjector() }

    /**
     * Default test [testInjector] creation.
     */
    open fun createTestInjector(): KodeinInjector {
        injector.inject(testKodein)
        val newTestInjector = KodeinInjector()
        newTestInjector.inject(testKodein)
        return newTestInjector
    }

    internal fun isInitialized(): Boolean = ::botBusMockContext.isInitialized

    /**
     * Default [StoryDefinition] if none is provided.
     */
    open fun defaultStoryDefinition(botDefinition: BotDefinition): StoryDefinition =
        if (isInitialized()) {
            botBusMockContext.story.definition
        } else {
            botDefinition.defaultStory
        }

    /**
     * Default [ConnectorType] if none is provided.
     */
    open fun defaultConnectorType(): ConnectorType =
        if (isInitialized()) {
            botBusMockContext.connectorType
        } else {
            defaultTestConnectorType
        }

    /**
     * Default [Locale] if none is provided.
     */
    open fun defaultLocale(): Locale =
        if (isInitialized()) {
            botBusMockContext.userPreferences.locale
        } else {
            defaultLocale
        }

    /**
     * Default [PlayerId] if none is provided.
     */
    open fun defaultPlayerId(): PlayerId =
        if (isInitialized()) {
            botBusMockContext.firstAction.playerId
        } else {
            PlayerId("user")
        }
}
