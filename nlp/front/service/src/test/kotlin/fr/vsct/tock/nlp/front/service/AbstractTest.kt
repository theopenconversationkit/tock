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

package fr.vsct.tock.nlp.front.service

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.nhaarman.mockito_kotlin.mock
import fr.vsct.tock.nlp.core.NlpCore
import fr.vsct.tock.nlp.front.service.storage.ParseRequestLogDAO
import fr.vsct.tock.nlp.front.shared.ApplicationConfiguration
import fr.vsct.tock.nlp.front.shared.config.Classification
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.tockInternalInjector
import org.junit.After
import org.junit.Before
import java.time.Instant

/**
 *
 */
abstract class AbstractTest {

    class TestContext {

        val core: NlpCore = mock()
        val config: ApplicationConfiguration = mock()
        val executor: Executor = mock()
        val logDAO: ParseRequestLogDAO = mock()

        val frontTestModule = Kodein.Module {
            bind<ApplicationConfiguration>() with provider { config }
            bind<NlpCore>() with provider { core }
            bind<Executor>() with provider { executor }
            bind<ParseRequestLogDAO>() with provider { logDAO }
        }

        fun init() {
            tockInternalInjector = KodeinInjector()
            injector.inject(Kodein {
                import(frontTestModule)
            })
        }
    }

    val context = TestContext()

    val defaultClassification = Classification("test", emptyList())
    val defaultClassifiedSentence = ClassifiedSentence("a", defaultLocale, "id", Instant.now(), Instant.now(), ClassifiedSentenceStatus.inbox, defaultClassification, 1.0, 1.0)

    @Before
    fun initContext() {
        context.init()
    }

    @After
    fun cleanupContext() {
        tockInternalInjector = KodeinInjector()
    }
}