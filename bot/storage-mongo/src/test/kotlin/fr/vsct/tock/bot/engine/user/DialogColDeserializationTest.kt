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

package fr.vsct.tock.bot.engine.user

import fr.vsct.tock.bot.mongo.DialogCol.AnyValueMongoWrapper
import fr.vsct.tock.shared.mapper
import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class DialogColDeserializationTest {

    @Test
    fun testAnyValueMongoWrapperSerialization() {
        val value = AnyValueMongoWrapper(
                UserLocation::class.qualifiedName!!,
                UserLocation(1.0, 2.0))
        val s = mapper.writeValueAsString(value)
        val newValue = mapper.readValue(s, AnyValueMongoWrapper::class.java)
        assertEquals(value, newValue)
    }

}