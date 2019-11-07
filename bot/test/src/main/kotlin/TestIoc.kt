/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

import com.github.salomonbrys.kodein.Kodein
import ai.tock.bot.test.TestContext
import ai.tock.translator.TranslatorEngine


/**
 * Module containing [TranslatorEngine].
 */
@Deprecated(
    "Use same var with ai.tock.bot.test package",
    replaceWith = ReplaceWith("testTranslatorModule", "ai.tock.bot.test.testTranslatorModule")
)
var testTranslatorModule: Kodein.Module
    get() = ai.tock.bot.test.testTranslatorModule
    set(v) {
        ai.tock.bot.test.testTranslatorModule = v
    }

/**
 * Test modules injected in [testInjector].
 */
@Deprecated(
    "Use same val with ai.tock.bot.test package",
    replaceWith = ReplaceWith("testModules", "ai.tock.bot.test.testModules")
)
val testModules: MutableList<Kodein.Module>
    get() = ai.tock.bot.test.testModules

/**
 * The current [TestContext] used when test parallelism is not a requirement.
 */
@Deprecated(
    "Use same var with ai.tock.bot.test package",
    replaceWith = ReplaceWith("currentTestContext", "ai.tock.bot.test.currentTestContext")
)
var currentTestContext: TestContext
    get() = ai.tock.bot.test.currentTestContext
    set(v) {
        ai.tock.bot.test.currentTestContext = v
    }



