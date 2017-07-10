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

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.nhaarman.mockito_kotlin.mock
import fr.vsct.tock.nlp.front.service.ParserService.formatQuery
import fr.vsct.tock.nlp.front.service.storage.ParseRequestLogDAO
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.tockInternalInjector
import org.junit.Before
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
class ParserServiceTest {

    @Before
    fun before() {
        val executor: Executor = mock()
        val logDAO: ParseRequestLogDAO = mock()
        tockInternalInjector = KodeinInjector()
        injector.inject(Kodein {
            import(Kodein.Module {
                bind<Executor>() with provider { executor }
                bind<ParseRequestLogDAO>() with provider { logDAO }
            })
        })
    }

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