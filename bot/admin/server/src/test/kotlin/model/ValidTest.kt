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

package model

import ai.tock.bot.admin.model.ToValidate
import ai.tock.bot.admin.model.Valid
import ai.tock.bot.admin.model.ValidationError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private const val NAME_CANNOT_BE_EMPTY = "Name cannot be empty"
private const val AGE_CANNOT_BE_NULL = "Age cannot be null"

/**
 * Person example implement of [ToValidate]
 */
data class Person(val name: String = "", val age: Int? = null) : ToValidate {
    override fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (name.isEmpty()) errors.add(NAME_CANNOT_BE_EMPTY)
        if (age == null) errors.add(AGE_CANNOT_BE_NULL)
        return errors
    }
}

class ValidTest {

    @Test
    fun `validation should succeed`() {

        val p = Valid(Person(name = "john", age = 18)).data

        assertNotNull(p)
        assertEquals("john", p.name)
        assertEquals(18, p.age)

    }

    @Test
    fun `validation should fail because name is empty`() {
        val error = assertThrows<ValidationError> {
            Valid(Person(age = 10))
        }

        assertEquals(NAME_CANNOT_BE_EMPTY, error.message)
    }

    @Test
    fun `validation should fail because age is null`() {
        val error = assertThrows<ValidationError> {
            Valid(Person(name = "john"))
        }

        assertEquals(AGE_CANNOT_BE_NULL, error.message)
    }

    @Test
    fun `validation should fail because name is empty and age is null`() {
        val error = assertThrows<ValidationError> {
            Valid(Person())
        }

        assertEquals(listOf(NAME_CANNOT_BE_EMPTY, AGE_CANNOT_BE_NULL).joinToString("\n"), error.message)
    }

}
