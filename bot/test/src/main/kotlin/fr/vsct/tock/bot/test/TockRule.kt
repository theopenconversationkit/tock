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

import com.github.salomonbrys.kodein.KodeinInjector
import com.nhaarman.mockito_kotlin.mock
import fr.vsct.tock.shared.tockInternalInjector
import fr.vsct.tock.translator.noop.noOpTranslatorModule
import mockedBotApplicationConfigurationDAO
import mockedDialogReportDAO
import mockedI18nDAO
import mockedNlpClient
import mockedNlpController
import mockedStoryDefinitionConfigurationDAO
import mockedTestPlanDAO
import mockedUserLock
import mockedUserReportDAO
import mockedUserTimelineDAO
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import testModules
import testTranslatorModule
import tockInternalTestInjector

/**
 * A JUnit rule to initialize the context for each call.
 */
open class TockRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        try {
            reset()
            return base
        } finally {
            reset()
        }
    }

    /**
     * Reset the scope at startup.
     */
    open protected fun reset() {
        tockInternalInjector = KodeinInjector()
        tockInternalTestInjector = KodeinInjector()

        testTranslatorModule = noOpTranslatorModule
        testModules.clear()

        mockedI18nDAO = mock()
        mockedNlpClient = mock()
        mockedNlpController = mock()
        mockedBotApplicationConfigurationDAO = mock()
        mockedStoryDefinitionConfigurationDAO = mock()
        mockedUserTimelineDAO = mock()
        mockedUserReportDAO = mock()
        mockedDialogReportDAO = mock()
        mockedTestPlanDAO = mock()
        mockedUserLock = mock()
    }
}