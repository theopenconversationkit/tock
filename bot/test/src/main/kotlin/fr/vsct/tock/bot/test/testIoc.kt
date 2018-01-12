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
import fr.vsct.tock.translator.TranslatorEngine
import fr.vsct.tock.translator.noop.noOpTranslatorModule


/**
 * Module containing [TranslatorEngine].
 */
var testTranslatorModule: Kodein.Module = noOpTranslatorModule

/**
 * Default mocked [I18nDAO].
 */
var mockedI18nDAO: I18nDAO = mock()

/**
 * Default mocked [NlpClient].
 */
var mockedNlpClient: NlpClient = mock()

/**
 * Default mocked [NlpController].
 */
var mockedNlpController: NlpController = mock()

/**
 * Default mocked [BotApplicationConfigurationDAO].
 */
var mockedBotApplicationConfigurationDAO: BotApplicationConfigurationDAO = mock()

/**
 * Default mocked [StoryDefinitionConfigurationDAO].
 */
var mockedStoryDefinitionConfigurationDAO: StoryDefinitionConfigurationDAO = mock()

/**
 * Default mocked [UserTimelineDAO].
 */
var mockedUserTimelineDAO: UserTimelineDAO = mock()

/**
 * Default mocked [UserReportDAO].
 */
var mockedUserReportDAO: UserReportDAO = mock()

/**
 * Default mocked [DialogReportDAO].
 */
var mockedDialogReportDAO: DialogReportDAO = mock()

/**
 * Default mocked [TestPlanDAO].
 */
var mockedTestPlanDAO: TestPlanDAO = mock()

/**
 * Default mocked [UserLock].
 */
var mockedUserLock: UserLock = mock()

/**
 * Test modules injected in [testInjector].
 */
val testModules: MutableList<Kodein.Module> = mutableListOf()

/**
 * The test [Kodein] injected.
 */
var testKodein: Kodein
    get() = if (internalTestKodein == null) {
        internalTestKodein = Kodein {
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
        internalTestKodein!!
    } else internalTestKodein!!
    set(kodein) {
        internalTestKodein = kodein
    }

private var internalTestKodein: Kodein? = null

/**
 * [KodeinInjector] used in tests.
 */
val testInjector: KodeinInjector
    get() = if (lastTestInjector !== tockInternalTestInjector) {
        injector.inject(testKodein)
        lastTestInjector = tockInternalTestInjector
        tockInternalTestInjector.inject(testKodein)
        tockInternalTestInjector
    } else {
        tockInternalTestInjector
    }

private var lastTestInjector: KodeinInjector? = null

/**
 * internal injector - reset only for tests.
 */
var tockInternalTestInjector: KodeinInjector = KodeinInjector()


