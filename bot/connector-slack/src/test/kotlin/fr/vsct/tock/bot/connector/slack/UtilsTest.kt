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

package fr.vsct.tock.bot.connector.slack


import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.junit.Before
import org.junit.Test

import kotlin.test.assertEquals


class UtilsTest {

    private val context: RoutingContext = mock()
    private val urlEncodedString = "arg1=val1&arg2=val2&arg3=val3"

    @Before
    fun before() {
        whenever(context.bodyAsString).thenReturn(urlEncodedString)
    }

    @Test
    fun testConvertUrlEncodedStringToJson() {
        val expectedJson = JsonObject().put("arg1", "val1").put("arg2", "val2").put("arg3", "val3").toString()
        assertEquals(expectedJson, context.convertUrlEncodedStringToJson())
    }
}