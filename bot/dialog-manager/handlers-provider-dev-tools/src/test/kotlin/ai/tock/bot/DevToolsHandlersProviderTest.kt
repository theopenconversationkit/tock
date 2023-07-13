/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot

import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test

class DevToolsHandlersProviderTest {

    @Test
    fun `invoke all handlers`(){
        DevToolsHandlersProvider().getActionHandlers()
            .forEach {
                assertDoesNotThrow("Requirements failed") {
                    it.invokeHandler(it.inputContexts.associateWith { null })
                }
            }
    }



}