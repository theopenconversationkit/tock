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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class HandlerNamespaceTest {

    @Test
    fun `all namespaces are not blank`(){
        HandlerNamespace.values()
            .forEach {
                assertFalse { it.key.isNullOrBlank() }
            }
    }

    @Test
    fun `UNKOWN namespace`(){
        assertFalse { HandlerNamespace.UNKNOWN.shared }
        assertEquals(HandlerNamespace.UNKNOWN, HandlerNamespace.find("TEST-123"))
    }

}