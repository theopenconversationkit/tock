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

package fr.vsct.tock.translator

import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.defaultNamespace
import fr.vsct.tock.translator.UserInterfaceType.textChat
import io.mockk.every
import io.mockk.verify
import org.junit.Test
import org.litote.kmongo.toId
import kotlin.test.assertEquals

/**
 *
 */
class TranslatorTest : AbstractTest() {

    @Test
    fun formatMessage_shouldHandleWell_SpecialCharInChoiceFormat() {
        val result = Translator.formatMessage(
            "Hey'{0,choice,0#|1# and %%<b>when</b>%%} ?",
            defaultLocale,
            textChat,
            null,
            listOf(true)
        )
        assertEquals("Hey' and <b>when</b> ?", result)
    }

    @Test
    fun formatMessage_shouldHandleWell_NullArgValue() {
        val result = Translator.formatMessage("a {0}", defaultLocale, textChat, null, listOf(null))
        assertEquals("a ", result)
    }

    @Test
    fun translate_shouldReturnsRightValue_whenAlreadyTranslatedStringIsPassed() {
        val category = "category"
        val toTranslate = "aaa"
        val target = "bbb"
        val id = "a"
        every { i18nDAO.getLabelById(id.toId()) } returns
                I18nLabel(
                    id.toId(),
                    defaultNamespace,
                    category,
                    LinkedHashSet(
                        listOf(
                            I18nLocalizedLabel(
                                defaultLocale,
                                textChat,
                                target
                            )
                        )
                    )
                )

        val key = I18nLabelKey(
            id,
            defaultNamespace,
            category,
            toTranslate
        )

        val translated = Translator.translate(key, defaultLocale, textChat)
        assertEquals(target, translated.toString())

        val key2 = I18nLabelKey(
            Translator.getKeyFromDefaultLabel(translated),
            defaultNamespace,
            category,
            translated
        )

        val translated2 = Translator.translate(key2, defaultLocale, textChat)

        assertEquals(translated, translated2)
    }

    @Test
    fun translate_shouldUseCacheAndLoadLabelOnlyOneTime_whenTheSameKeyIsAskedTwiceSequentially() {
        val category = "category"
        val toTranslate = "aaa"
        val target = "bbb"
        val id = "not_yet_cached_id"
        every { i18nDAO.getLabelById(id.toId()) } returns
                I18nLabel(
                    id.toId(),
                    defaultNamespace,
                    category,
                    LinkedHashSet(
                        listOf(
                            I18nLocalizedLabel(
                                defaultLocale,
                                textChat,
                                target
                            )
                        )
                    )
                )


        val key = I18nLabelKey(
            id,
            defaultNamespace,
            category,
            toTranslate
        )


        assertEquals(target, Translator.translate(key, defaultLocale, textChat).toString())

        assertEquals(target, Translator.translate(key, defaultLocale, textChat).toString())

        verify { i18nDAO.getLabelById(id.toId()) }
    }

    @Test
    fun translate_shouldGenerateDefault_whenNotExistingIdIsPassed() {
        val category = "category"
        val toTranslate = "aaa"
        val id = "not_existing_id"

        val key = I18nLabelKey(
            id,
            defaultNamespace,
            category,
            toTranslate
        )

        every { i18nDAO.getLabelById(id.toId()) }.returns(null)

        assertEquals(toTranslate, Translator.translate(key, defaultLocale, textChat).toString())

        verify { i18nDAO.getLabelById(id.toId()) }
    }
}