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

import ai.tock.shared.defaultLocale
import ai.tock.shared.defaultNamespace
import ai.tock.translator.UserInterfaceType.textChat
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId

/**
 *
 */
class TranslatorTest : AbstractTest() {

    @AfterEach
    fun teardown() {
        Translator.resetValueWhenDefaultChanges = false
    }

    @Test
    fun formatMessage_shouldHandleWell_SpecialCharInChoiceFormat() {
        val result = Translator.formatMessage(
            "Hey'{0,choice,0#|1# and %%<b>when</b>%%} ?",
            I18nContext(
                defaultLocale,
                textChat,
                null
            ),
            listOf(true)
        )
        assertEquals("Hey' and <b>when</b> ?", result)
    }

    @Test
    fun formatMessage_shouldHandleWell_NullArgValue() {
        val result = Translator.formatMessage(
            "a {0}",
            I18nContext(defaultLocale, textChat, null),
            listOf(null)
        )
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

        val key = I18nLabelValue(
            id,
            defaultNamespace,
            category,
            toTranslate
        )

        val translated = Translator.translate(
            key,
            I18nContext(defaultLocale, textChat)
        )
        assertEquals(target, translated.toString())

        val key2 = I18nLabelValue(
            Translator.getKeyFromDefaultLabel(translated),
            defaultNamespace,
            category,
            translated
        )

        val translated2 = Translator.translate(
            key2,
            I18nContext(defaultLocale, textChat)
        )

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

        val key = I18nLabelValue(
            id,
            defaultNamespace,
            category,
            toTranslate
        )

        assertEquals(
            target,
            Translator.translate(
                key,
                I18nContext(defaultLocale, textChat)
            ).toString()
        )

        assertEquals(
            target,
            Translator.translate(
                key,
                I18nContext(defaultLocale, textChat)
            ).toString()
        )

        verify { i18nDAO.getLabelById(id.toId()) }
    }

    @Test
    fun translate_shouldResetLabel_whenEnabledAndTheSameKeyIsAskedTwiceWithDifferentDefaults() {
        val category = "category"
        val toTranslate = "aaa"
        val target = "bbb"
        val updatedTarget = "bbbc"
        val id = "not_yet_cached_id"
        val initialDefaults = linkedSetOf(
            I18nLocalizedLabel(
                defaultLocale,
                textChat,
                target
            )
        )
        every { i18nDAO.getLabelById(id.toId()) } returns
            I18nLabel(
                id.toId(),
                defaultNamespace,
                category,
                initialDefaults,
                defaultI18n = initialDefaults
            )
        Translator.resetValueWhenDefaultChanges = true

        val key = I18nLabelValue(
            id,
            defaultNamespace,
            category,
            toTranslate,
            defaultI18n = initialDefaults
        )

        assertEquals(
            target,
            Translator.translate(
                key,
                I18nContext(defaultLocale, textChat)
            ).toString()
        )

        assertEquals(
            updatedTarget,
            Translator.translate(
                I18nLabelValue(
                    id,
                    defaultNamespace,
                    category,
                    toTranslate,
                    defaultI18n = linkedSetOf(
                        I18nLocalizedLabel(
                            defaultLocale,
                            textChat,
                            updatedTarget
                        )
                    )
                ),
                I18nContext(defaultLocale, textChat)
            ).toString()
        )

        verify { i18nDAO.getLabelById(id.toId()) }
    }

    @Test
    fun translate_shouldGenerateDefault_whenNotExistingIdIsPassed() {
        val category = "category"
        val toTranslate = "aaa"
        val id = "not_existing_id"

        val key = I18nLabelValue(
            id,
            defaultNamespace,
            category,
            toTranslate
        )

        every { i18nDAO.getLabelById(id.toId()) }.returns(null)

        assertEquals(
            toTranslate,
            Translator.translate(
                key,
                I18nContext(defaultLocale, textChat)
            ).toString()
        )

        verify { i18nDAO.getLabelById(id.toId()) }
    }

    @Test
    fun `randomText returns the same label when index specified`() {
        val label = I18nLocalizedLabel(
            defaultLocale,
            defaultUserInterface,
            "a",
            listOf("b")
        )
        val l = I18nLabel(
            "".toId(),
            defaultNamespace,
            "",
            LinkedHashSet(listOf(label))
        )

        val index = 1
        assertEquals("b", Translator.randomText(l, label, "contextId", index))
    }

    @Test
    fun `saveIfNotExist should use default locale when none provided`() {

        val locale = Locale("fr")
        assertNotEquals(locale, defaultLocale)

        val localizedLabel = I18nLocalizedLabel(
            locale,
            defaultUserInterface,
            "a",
            listOf("b")
        )
        val label = I18nLabel(
            "".toId(),
            defaultNamespace,
            "",
            LinkedHashSet(listOf(localizedLabel))
        )

        every { i18nDAO.getLabelById(any()) }.returns(null)

        Translator.saveIfNotExist(I18nLabelValue(label))

        val slot = slot<I18nLabel>()
        verify { i18nDAO.save(capture(slot)) }
        val savedLabel = slot.captured

        assertEquals(defaultLocale, savedLabel.defaultLocale)
        assert(savedLabel.i18n.all { it.locale == defaultLocale })
    }

    @Test
    fun `saveIfNotExist should use locale when provided`() {

        val locale = Locale("fr")
        assertNotEquals(locale, defaultLocale)

        val localizedLabel = I18nLocalizedLabel(
            locale,
            defaultUserInterface,
            "a",
            listOf("b")
        )
        val label = I18nLabel(
            "".toId(),
            defaultNamespace,
            "",
            LinkedHashSet(listOf(localizedLabel))
        )

        every { i18nDAO.getLabelById(any()) }.returns(null)

        Translator.saveIfNotExist(I18nLabelValue(label), locale)

        val slot = slot<I18nLabel>()
        verify { i18nDAO.save(capture(slot)) }
        val savedLabel = slot.captured

        assertEquals(locale, savedLabel.defaultLocale)
        assert(savedLabel.i18n.all { it.locale == locale })
    }
}
