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

package ai.tock.nlp.front.service

import ai.tock.nlp.core.ParsingResult
import ai.tock.nlp.front.service.ParserService.formatQuery
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import ai.tock.nlp.front.shared.config.SentencesQueryResult
import ai.tock.nlp.front.shared.parser.IntentQualifier
import ai.tock.shared.defaultLocale
import ai.tock.shared.name
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
class ParserServiceTest : AbstractTest() {
    private val validatedSentence =
        defaultClassifiedSentence.copy(classification = defaultClassification.copy("new_intent".toId()))

    @Test
    fun formatQuery_shouldRemoveAllTabsAndCarriage() {
        val parsed = formatQuery("a \r d\n \td")
        assertEquals("a  d d", parsed)
        println("a  d d")
    }

    @Test
    fun formatQuery_shouldTrim() {
        val parsed = formatQuery(" a \r d\n \t ")
        assertEquals("a  d", parsed)
    }

    @Test
    fun findLanguage_shouldReturnDefault_WhenLocaleParameterIsNotSupportedAndDefaultIsSupported() {
        val locale =
            ParserService.findLanguage(
                ApplicationDefinition("test", namespace = "test", supportedLocales = setOf(defaultLocale)),
                Locale.JAPANESE,
            )

        assertEquals(defaultLocale, locale)
    }

    @Test
    fun findLanguage_shouldReturnFirstFound_WhenLocaleParameterIsNotSupportedAndDefaultIsNotSupported() {
        val locale =
            ParserService.findLanguage(
                ApplicationDefinition("test", namespace = "test", supportedLocales = setOf(Locale.ITALIAN)),
                Locale.JAPANESE,
            )

        assertEquals(Locale.ITALIAN, locale)
    }

    @Test
    fun saveSentence_shouldSaveTheSentence_ifTheAlreadyExistingSentenceHasInboxStatusAndNotSameContent() {
        ParserService.saveSentence(app, defaultClassifiedSentence, validatedSentence)
        verify { context.config.save(any<ClassifiedSentence>()) }
    }

    @Test
    fun saveSentence_shouldNotSaveTheSentence_ifTheAlreadyExistingSentenceHasValidatedStatus() {
        ParserService.saveSentence(app, defaultClassifiedSentence, validatedSentence.copy(status = validated))
        verify(exactly = 0) { context.config.save(any<ClassifiedSentence>()) }
    }

    @Test
    fun saveSentence_shouldNotSaveTheSentence_ifTheAlreadyExistingSentenceHasModelStatus() {
        ParserService.saveSentence(app, defaultClassifiedSentence, validatedSentence.copy(status = model))
        verify(exactly = 0) { context.config.save(any<ClassifiedSentence>()) }
    }

    @Test
    fun `GIVEN a parse request WHEN the sentence is validated and in intentsSubset THEN this sentence is used`() {
        every { context.config.search(any()) } returns SentencesQueryResult(1, listOf(intent2ClassifiedSentence))

        val result = ParserService.parse(intentSubsetParseQuery)

        assertEquals(intent2Name.name(), result.intent)
    }

    @Test
    fun `GIVEN a parse request WHEN the sentence is validated but not in intentsSubset THEN the nlp model is used`() {
        every { context.config.search(any()) } returns SentencesQueryResult(1, listOf(intent2ClassifiedSentence))
        every { context.core.parse(any(), any(), any()) } returns
            ParsingResult(
                defaultIntentName,
                emptyList(),
                emptyList(),
                1.0,
                1.0,
            )

        val query = intentSubsetParseQuery.copy(intentsSubset = setOf(IntentQualifier(defaultIntentName, 0.0)))
        val result = ParserService.parse(query)

        assertEquals(defaultIntentName.name(), result.intent)
    }
}
