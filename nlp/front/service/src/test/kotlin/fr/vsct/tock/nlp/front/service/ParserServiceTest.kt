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

package fr.vsct.tock.nlp.front.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import fr.vsct.tock.nlp.front.service.ParserService.formatQuery
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import fr.vsct.tock.shared.defaultLocale
import org.junit.Test
import org.litote.kmongo.toId
import org.mockito.Mockito.verify
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
class ParserServiceTest : AbstractTest() {

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
        val locale = ParserService.findLanguage(
                ApplicationDefinition("test", "test", supportedLocales = setOf(defaultLocale)),
                Locale.JAPANESE
        )

        assertEquals(defaultLocale, locale)
    }

    @Test
    fun findLanguage_shouldReturnFirstFound_WhenLocaleParameterIsNotSupportedAndDefaultIsNotSupported() {
        val locale = ParserService.findLanguage(
                ApplicationDefinition("test", "test", supportedLocales = setOf(Locale.ITALIAN)),
                Locale.JAPANESE
        )

        assertEquals(Locale.ITALIAN, locale)
    }

    private val validatedSentence = defaultClassifiedSentence.copy(classification = defaultClassification.copy("new_intent".toId()))


    @Test
    fun saveSentence_shouldSaveTheSentence_ifTheAlreadyExistingSentenceHasInboxStatusAndNotSameContent() {
        ParserService.saveSentence(defaultClassifiedSentence, validatedSentence)
        verify(context.config).save(any<ClassifiedSentence>())
    }

    @Test
    fun saveSentence_shouldNotSaveTheSentence_ifTheAlreadyExistingSentenceHasValidatedStatus() {
        ParserService.saveSentence(defaultClassifiedSentence, validatedSentence.copy(status = validated))
        verify(context.config, never()).save(any<ClassifiedSentence>())
    }

    @Test
    fun saveSentence_shouldNotSaveTheSentence_ifTheAlreadyExistingSentenceHasModelStatus() {
        ParserService.saveSentence(defaultClassifiedSentence, validatedSentence.copy(status = model))
        verify(context.config, never()).save(any<ClassifiedSentence>())
    }

}