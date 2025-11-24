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

package ai.tock.shared.jackson

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class AnyValueWrapperTest {
    data class Custom(val name: String)

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    sealed class Either {
        data class ThisOne(val anyProperty: String) : Either()

        data class AnotherOne(val anyProperty: String, val otherProperty: Int) : Either()
    }

    data class Somes(val somes: List<Either>)

    @Test
    fun serializeAndDeserializeAnyValueWrapper_shouldLeftDataInchanged() {
        val value = AnyValueWrapper(Custom("ok"))
        val s = mapper.writeValueAsString(value)
        val newValue = mapper.readValue<AnyValueWrapper>(s)
        assertEquals(value, newValue)
    }

    @Test
    fun serializeAndDeserializeNullValueWrapper_shouldLeftDataInchanged() {
        val value = AnyValueWrapper(Custom::class, null)
        val s = mapper.writeValueAsString(value)
        val newValue = mapper.readValue<AnyValueWrapper>(s)
        assertEquals(value, newValue)
    }

    @Test
    fun serializeAndDeserializeTypedArrayValueWrapper_shouldLeftDataInchanged() {
        val value = AnyValueWrapper(arrayOf(Custom("ok")))
        val s = mapper.writeValueAsString(value)
        val newValue = mapper.readValue<AnyValueWrapper>(s)
        Assertions.assertArrayEquals(value.value as Array<*>, newValue.value as Array<*>)
    }

    @Test
    fun deserializeUnknownClass_shouldNotFailAndReturnsNull() {
        val value = AnyValueWrapper("unknown", null)
        val s = mapper.writeValueAsString(value)
        val newValue = mapper.readValue<AnyValueWrapper?>(s, object : TypeReference<AnyValueWrapper?>() {})
        assertNull(newValue)
    }

    @Test
    fun serializeAndDeserializeSealedClass_shouldLeftDataUnchanged() {
        val value = Either.ThisOne("toto")
        val s = mapper.writeValueAsString(value)
        val newValue = mapper.readValue<Either.ThisOne>(s)
        assertEquals(value, newValue)
    }

    /**
     * Be careful : doesn't work if you directly writeValueAsString on a list object because of generic collection type erasure.
     *
     * So you will have to use a first class collection like Somes
     */
    @Test
    fun serializeAndDeserializeSealedClassInCollection_shouldCreateCorrectSubtype() {
        val thisOne = Either.ThisOne("toto")
        val anotherOne = Either.AnotherOne("titi", 42)
        val s = mapper.writeValueAsString(Somes(listOf(thisOne, anotherOne)))
        val newValue = mapper.readValue<Somes>(s)
        assertEquals(thisOne, newValue.somes[0])
        assertEquals(anotherOne, newValue.somes[1])
    }
}
