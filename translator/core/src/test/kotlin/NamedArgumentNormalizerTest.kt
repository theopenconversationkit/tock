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

package ai.tock.translator

import ai.tock.translator.NamedArgumentNormalizer.normalize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.text.MessageFormat
import java.time.LocalDate

internal class NamedArgumentNormalizerTest {

    @Test
    fun normalize_shouldHandle_onlyNamedArgs() {
        val result = normalize(
            "nom: {:name}, age {:age}",
            listOf(
                "name" to "smith",
                "age" to 10
            )
        )

        assertEquals("nom: {0}, age {1}", result.label)
        assertEquals(listOf("smith", 10), result.args)
    }

    @Test
    fun normalize_shouldHandle_namedArgsWithUppercase() {
        val result = normalize(
            "nom: {:NaMe}, age {:age}",
            listOf(
                "NaMe" to "smith",
                "age" to 10
            )
        )

        assertEquals("nom: {0}, age {1}", result.label)
        assertEquals(listOf("smith", 10), result.args)
    }

    @Test
    fun normalize_shouldHandle_namedArgsWithUnderscore() {
        val result = normalize(
            "nom: {:first_name}, age {:age}",
            listOf(
                "first_name" to "smith",
                "age" to 10
            )
        )

        assertEquals("nom: {0}, age {1}", result.label)
        assertEquals(listOf("smith", 10), result.args)
    }

    @Test
    fun normalize_shouldHandle_onlyVanillaArgs() {
        val result = normalize(
            "firstname: {0}, lastname {1}",
            listOf(
                "john",
                "doe"
            )
        )

        assertEquals("firstname: {0}, lastname {1}", result.label)
        assertEquals(listOf("john", "doe"), result.args)
    }

    @Test
    fun normalize_shouldHandle_mixArgTypes() {
        val result = normalize(
            "nom: {:name}, {0} age {:age_noe} {1}",
            listOf(
                "age_noe" to 123,
                "first parameter",
                "name" to "smith",
                "last parameter"
            )
        )

        assertEquals("nom: {2}, {0} age {3} {1}", result.label)
        assertEquals(listOf("first parameter", "last parameter", "smith", 123), result.args)
    }

    @Test
    fun normalize_shouldNotCheck_VanillaArgConsistency() {
        val result = normalize(
            "nom: {:name}, {0} age {:age_noe} {1}",
            listOf(
                "age_noe" to 123,
                "first parameter",
                "name" to "smith"
            )
        )

        assertEquals("nom: {2}, {0} age {3} {1}", result.label)
        assertEquals(listOf("first parameter", "smith", 123), result.args)
        assertEquals("nom: 123, first parameter age {3} smith", MessageFormat.format(result.label, *result.args.toTypedArray()))
    }

    @Test
    fun normalize_shouldHandle_complexArgsValue() {
        val result = normalize(
            "nom: {:name}, date of birth {:date_of_birth}",
            listOf(
                "name" to "smith",
                "date_of_birth" to LocalDate.of(2000, 12, 25)
            )
        )

        assertEquals("nom: {0}, date of birth {1}", result.label)
        assertEquals(listOf("smith", LocalDate.of(2000, 12, 25)), result.args)
    }

    @Test
    fun normalize_shouldFillWithMissingArgWithArgName() {
        val result = normalize(
            "nom: {:name}, age {:age} {:address}",
            listOf("age" to 10)
        )
        assertEquals("nom: {0}, age {1} {2}", result.label)
        assertEquals(listOf(":name", 10, ":address"), result.args)
        assertEquals("nom: :name, age 10 :address", MessageFormat.format(result.label, *result.args.toTypedArray()))
    }

    @Test
    fun normalize_shouldNotRaiseException_IfArgsAreEmpty() {
        val result = normalize("nom: {:name}, date of birth {:date_of_birth}")

        assertEquals("nom: {0}, date of birth {1}", result.label)
        assertEquals("nom: {0}, date of birth {1}", MessageFormat.format(result.label))
    }

    @Test
    fun normalize_shouldHandle_wrongTags() {
        val result = normalize(
            "nom: {:name}, {0} {:} {other} :age: {:age_noe}  {:",
            listOf(
                "age_noe" to 10,
                "first parameter",
                "name" to "smith"
            )
        )

        assertEquals("nom: {1}, {0} {:} {other} :age: {2}  {:", result.label)
        assertEquals(listOf("first parameter", "smith", 10), result.args)
    }
}
