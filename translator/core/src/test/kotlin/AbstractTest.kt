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

package ai.tock.translator

import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

/**
 *
 */
abstract class AbstractTest {

    val i18nDAO: I18nDAO = mockk(relaxed = true)
    val translatorEngine: TranslatorEngine = mockk(relaxed = true)

    open fun baseModule(): Kodein.Module {
        return Kodein.Module {
            bind<I18nDAO>() with provider { i18nDAO }
            bind<TranslatorEngine>() with provider { translatorEngine }
        }
    }

    @BeforeEach
    fun initContext() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein {
                import(sharedTestModule)
                import(baseModule())
            }
        )
        Translator.enabled = true
    }

    @AfterEach
    fun cleanupContext() {
        tockInternalInjector = KodeinInjector()
        Translator.enabled = false
    }
}
