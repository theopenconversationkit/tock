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
import fr.vsct.tock.shared.injector
import fr.vsct.tock.translator.I18nDAO
import fr.vsct.tock.translator.noop.noOpTranslatorModule


/**
 * Test modules injected in [testInjector].
 */
val testModules = mutableListOf(noOpTranslatorModule)

/**
 * The test [Kodein] injected.
 */
var testKodein: Kodein = Kodein {
    import(
            Kodein.Module {
                bind<I18nDAO>() with provider { mock<I18nDAO>() }
            })
    testModules.forEach { import(it) }
}
/**
 * [KodeinInjector] used in tests.
 */
val testInjector: KodeinInjector
        by lazy {
            injector.inject(testKodein)
            KodeinInjector().apply {
                inject(testKodein)
            }
        }


