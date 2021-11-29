/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import ai.tock.shared.ModelOptions
import ai.tock.shared.formatTockText
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals

internal class ModelCoreServiceTest {
    val locale: Locale = Locale.FRENCH

    @Test
    fun `formatTockText Should Keep the text unchanged IF caseInsensitive is false and ignoreTrailingPunctuation is false`() {
        val modelOptions = ModelOptions(caseInsensitive = false, ignoreTrailingPunctuation = false)
        val input = "This is a sentence"
        assertEquals(input, input.formatTockText(modelOptions, locale))
    }

    @Test
    fun `formatTockText Should only lowercase the text IF caseInsensitive is true and ignoreTrailingPunctuation is false`() {
        val modelOptions = ModelOptions( caseInsensitive = true, ignoreTrailingPunctuation = false)
        val input = "THIS is a sentence"
        val output = "this is a sentence"
        assertEquals(output, input.formatTockText(modelOptions, locale))
    }

    @Test
    fun `formatTockText Should only remove trailing punctuation IF caseInsensitive is false and ignoreTrailingPunctuation is true`() {
        val modelOptions = ModelOptions( caseInsensitive = false, ignoreTrailingPunctuation = true)
        val input = "This is a sentence??"
        val output = "This is a sentence"
        assertEquals(output, input.formatTockText(modelOptions, locale))
    }

    @Test
    fun `formatTockText Should lowercase and remove the trailing punctuation IF caseInsensitive is true and ignoreTrailingPunctuation is true`() {
        val modelOptions = ModelOptions( caseInsensitive = true, ignoreTrailingPunctuation = true)
        val input = "THIS IS A sentence??"
        val output = "this is a sentence"
        assertEquals(output, input.formatTockText(modelOptions, locale))
    }
}
