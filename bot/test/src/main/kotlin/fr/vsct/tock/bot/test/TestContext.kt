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
import com.nhaarman.mockito_kotlin.mock
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfigurationDAO
import fr.vsct.tock.bot.admin.dialog.DialogReportDAO
import fr.vsct.tock.bot.admin.test.TestPlanDAO
import fr.vsct.tock.bot.admin.user.UserReportDAO
import fr.vsct.tock.bot.engine.nlp.NlpController
import fr.vsct.tock.bot.engine.user.UserLock
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.nlp.api.client.NlpClient
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.sharedModule
import fr.vsct.tock.translator.I18nDAO
import testModules
import testTranslatorModule

/**
 *
 */
open class TestContext {

    inline fun <reified T : Any> newMock() = mock<T>()

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

    open fun importModule(): Kodein.Builder.() -> Unit = {
        import(sharedModule, true)
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

    open fun createTestInjector(): KodeinInjector {
        injector.inject(testKodein)
        val newTestInjector = KodeinInjector()
        newTestInjector.inject(testKodein)
        return newTestInjector
    }

}