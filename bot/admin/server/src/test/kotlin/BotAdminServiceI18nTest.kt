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

package ai.tock.bot.admin

import ai.tock.bot.admin.model.CreateI18nLabelRequest
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.shared.tockInternalInjector
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.Translator
import ai.tock.translator.UserInterfaceType.textChat
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import mongo.mock.I18nMongoDAOMock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BotAdminServiceI18nTest : AbstractTest() {

    companion object {
        val i18nMongoDAOMocked: I18nMongoDAOMock = mockk(relaxed = true)
        init {
            // IOC
            tockInternalInjector = KodeinInjector()

            val specificModule = Kodein.Module {
                bind<StoryDefinitionConfigurationDAO>() with provider { storyDefinitionDAO }
                bind<I18nDAO>() with provider { i18nMongoDAOMocked }
            }

            tockInternalInjector.inject(
                Kodein {
                    import(defaultModulesBinding())
                    import(specificModule)
                }
            )
        }
    }

    @BeforeEach
    fun before(){
        mockkObject(Translator)
    }

    @AfterEach
    fun after(){
        unmockkAll()
    }

    val namespace = "namespace"

    @Test
    fun `GIVEN createI18nRequest WHEN without i18n parameter THEN should create I18nLabel`() {
        // GIVEN
        val locale = Locale.FRENCH
        val request = CreateI18nLabelRequest("label", locale, "category")
        every { Translator.create(any(), any()) } answers {
            I18nLabel(
                "i18nId".toId(),
                namespace,
                request.category,
                LinkedHashSet(),
                request.label,
                locale
            )
        }
        justRun {
            i18nMongoDAOMocked.save(any<I18nLabel>())
        }
        // WHEN
        val answer = BotAdminService.createI18nRequest(namespace, request)

        //THEN
        assertEquals(answer.defaultLabel, request.label)
        assertEquals(answer.defaultLocale, locale)
        assertTrue(answer.i18n.isEmpty())

        verify(exactly = 1) {
            Translator.create(any(),any())
        }
        verify(exactly = 0) {
            Translator.completeAllLabels(any())
        }
    }

    @Test
    fun `GIVEN createI18nRequest WHEN with i18n parameter THEN should create I18nLabel`() {
        // GIVEN
        val locale = Locale.FRENCH

        val englishLabel = I18nLocalizedLabel(Locale.ENGLISH, textChat, "englishLabel")
        val germanLabel = I18nLocalizedLabel(Locale.GERMAN, textChat, "germanLabel")
        val i18nLocalizedLabel = linkedSetOf(englishLabel, germanLabel)
        val request = CreateI18nLabelRequest("label", locale, "category", i18nLocalizedLabel)
        every { Translator.create(any(), any()) } answers {
            I18nLabel(
                "i18nId".toId(),
                namespace,
                request.category,
                //the i18n localized label is not filled here per default
                LinkedHashSet(),
                request.label,
                locale
            )
        }
        justRun {
            i18nMongoDAOMocked.save(any<I18nLabel>())
        }
        // WHEN
        val answer = BotAdminService.createI18nRequest(namespace, request)

        //THEN
        assertEquals(answer.defaultLabel, request.label)
        assertEquals(answer.defaultLocale, locale)
        assertTrue(answer.i18n.isNotEmpty())
        assertEquals(answer.i18n,request.i18n)

        verify(exactly = 1) {
            Translator.create(any(),any())
            Translator.completeAllLabels(any())
        }
    }
}