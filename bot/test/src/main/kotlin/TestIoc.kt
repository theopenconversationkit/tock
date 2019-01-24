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
import fr.vsct.tock.bot.test.TestContext
import fr.vsct.tock.translator.TranslatorEngine


/**
 * Module containing [TranslatorEngine].
 */
@Deprecated(
    "Use same var with fr.vsct.tock.bot.test package",
    replaceWith = ReplaceWith("testTranslatorModule", "fr.vsct.tock.bot.test.testTranslatorModule")
)
var testTranslatorModule: Kodein.Module
    get() = fr.vsct.tock.bot.test.testTranslatorModule
    set(v) {
        fr.vsct.tock.bot.test.testTranslatorModule = v
    }

/**
 * Test modules injected in [testInjector].
 */
@Deprecated(
    "Use same val with fr.vsct.tock.bot.test package",
    replaceWith = ReplaceWith("testModules", "fr.vsct.tock.bot.test.testModules")
)
val testModules: MutableList<Kodein.Module>
    get() = fr.vsct.tock.bot.test.testModules

/**
 * The current [TestContext] used when test parallelism is not a requirement.
 */
@Deprecated(
    "Use same var with fr.vsct.tock.bot.test package",
    replaceWith = ReplaceWith("currentTestContext", "fr.vsct.tock.bot.test.currentTestContext")
)
var currentTestContext: TestContext
    get() = fr.vsct.tock.bot.test.currentTestContext
    set(v) {
        fr.vsct.tock.bot.test.currentTestContext = v
    }



