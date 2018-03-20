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
import fr.vsct.tock.nlp.core.NlpCore
import fr.vsct.tock.nlp.front.service.storage.ParseRequestLogDAO
import fr.vsct.tock.nlp.front.shared.ApplicationConfiguration
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.Classification
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.QueryContext
import fr.vsct.tock.shared.Dice
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.name
import fr.vsct.tock.shared.tockInternalInjector
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant

/**
 *
 */
abstract class AbstractTest {

    class TestContext {

        val core: NlpCore = mockk(relaxed = true)
        val config: ApplicationConfiguration = mockk(relaxed = true)
        val executor: Executor = mockk(relaxed = true)
        val logDAO: ParseRequestLogDAO = mockk(relaxed = true)

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

    val namespace = "namespace"
    val appName = "test"
    val app = ApplicationDefinition(appName, namespace, _id = "id".toId())

    val defaultIntentName = "$namespace:intent"
    val defaultIntentDefinition = IntentDefinition(defaultIntentName.name(), namespace, setOf(app._id), emptySet(), _id = newId())
    val defaultClassification = Classification(defaultIntentDefinition._id, emptyList())
    val defaultClassifiedSentence = ClassifiedSentence("a", defaultLocale, "id".toId(), Instant.now(), Instant.now(), ClassifiedSentenceStatus.inbox, defaultClassification, 1.0, 1.0)

    val parseQuery = ParseQuery(emptyList(), namespace, appName, QueryContext(defaultLocale, Dice.newId()))

    @Before
    fun initContext() {
        context.init()
    }

    @After
    fun cleanupContext() {
        tockInternalInjector = KodeinInjector()
    }
}