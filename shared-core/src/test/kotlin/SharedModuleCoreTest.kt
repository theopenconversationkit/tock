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

package ai.tock.shared

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instanceOrNull
import kotlin.test.Test
import kotlin.test.assertNull

class SharedModuleCoreTest {
    @Test
    fun `without tock-shared, sharedModule binds neither the Vert_x executor nor MongoDB`() {
        val kodein = Kodein { import(sharedModule) }

        assertNull(kodein.instanceOrNull<Executor>())
    }
}
