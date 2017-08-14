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

package fr.vsct.tock.bot.connector.ga.model.request

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.resource
import org.junit.Test
import kotlin.test.assertNotNull

/**
 *
 */
class GARequestDeserializerTest {

    @Test
    fun testGARequestDeserializer() {
        val json = resource("/request.json")
        val request: GARequest = mapper.readValue(json)
        assertNotNull(request)
    }

    @Test
    fun testGARequest2Deserializer() {
        val json = resource("/request2.json")
        val request: GARequest = mapper.readValue(json)
        assertNotNull(request)
    }

    @Test
    fun testGARequest3Deserializer() {
        val json = resource("/request3.json")
        val request: GARequest = mapper.readValue(json)
        assertNotNull(request)
    }

}