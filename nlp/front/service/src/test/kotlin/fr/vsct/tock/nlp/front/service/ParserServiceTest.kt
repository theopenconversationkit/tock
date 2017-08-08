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

import fr.vsct.tock.nlp.front.service.ParserService.formatQuery
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.shared.defaultLocale
import org.junit.Test
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
}