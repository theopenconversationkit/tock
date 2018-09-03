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

package fr.vsct.tock.bot.test

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfigurationDAO
import fr.vsct.tock.bot.admin.dialog.DialogReportDAO
import fr.vsct.tock.bot.admin.test.TestPlanDAO
import fr.vsct.tock.bot.admin.user.UserReportDAO
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.messenger.messengerConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.definition.StoryHandlerListener
import fr.vsct.tock.bot.engine.nlp.NlpController
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserLock
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.nlp.api.client.NlpClient
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.sharedTestModule
import fr.vsct.tock.translator.I18nDAO
import io.mockk.classMockk
import testModules
import testTranslatorModule
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
    open fun <T : Any> newMock(kClass: KClass<T>): T = classMockk(kClass, relaxed = true)

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
     * Default mocked Tock Ioc.
     */
    open fun importModule(): Kodein.Builder.() -> Unit = {
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
            })
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
        if (isInitialized()) botBusMockContext.story.definition
        else botDefinition.helloStory ?: botDefinition.stories.first()

    /**
     * Default [ConnectorType] if none is provided.
     */
    open fun defaultConnectorType(): ConnectorType =
        if (isInitialized()) botBusMockContext.connectorType
        else messengerConnectorType

    /**
     * Default [Locale] if none is provided.
     */
    open fun defaultLocale(): Locale =
        if (isInitialized()) botBusMockContext.userPreferences.locale
        else defaultLocale

    /**
     * Default [PlayerId] if none is provided.
     */
    open fun defaultPlayerId(): PlayerId =
        if (isInitialized()) botBusMockContext.firstAction.playerId
        else PlayerId("user")

}