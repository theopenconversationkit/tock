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

package ai.tock.bot.test

import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.KodeinInjector
import io.mockk.coEvery

/**
 *
 */
open class TestLifecycle<out T : TestContext>(val testContext: T) {
    open fun resetInjectors() {
        tockInternalInjector = KodeinInjector()
    }

    open fun start() {
        resetInjectors()
        // force injection loading
        testContext.testInjector
    }

    open fun end() {
        resetInjectors()
    }

    open fun configureTestIoc() {
        with(testContext) {
            coEvery { mockedUserTimelineDAO.getSnapshots(any()) } returns botBusMockContext.snapshots
        }
    }
}
