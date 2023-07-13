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

package ai.tock.bot.mongo

import ai.tock.bot.mongo.I18nMongoDAO.getAlternativeIndexes
import ai.tock.shared.defaultLocale
import ai.tock.shared.defaultNamespace
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType
import ai.tock.translator.defaultUserInterface
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 */
class I18nMongoDAOTest : AbstractTest() {

    @Test
    fun `getAlternativeIndexes returns the indexes already used`() {
        val label = I18nLabel(newId(), defaultNamespace, "category", LinkedHashSet())
        val localized = I18nLocalizedLabel(defaultLocale, defaultUserInterface, "a")
        I18nMongoDAO.addAlternativeIndex(label, localized, 2, "id")
        I18nMongoDAO.addAlternativeIndex(label, localized, 3, "id")

        assertEquals(
            setOf(2, 3),
            getAlternativeIndexes(label, localized, "id")

        )
    }

    private val i18nId = "idI18n".toId<I18nLabel>()
    private val namespace = "test"
    private val label = "the answer"
    private val mockedDefaultLocalizedLabel: I18nLocalizedLabel = I18nLocalizedLabel(
        locale = defaultLocale,
        interfaceType = UserInterfaceType.textChat,
        label = label,
        validated = true,
        alternatives = emptyList()
    )
    val mockedI18n = I18nLabel(
        _id = i18nId, namespace = namespace, category = "category", linkedSetOf(
            mockedDefaultLocalizedLabel
        ), defaultLabel = label, version=1
    )

    val unknownRandomId = "unknownI18n".toId<I18nLabel>()

    @Test
    fun `GIVEN answer WHEN saveI18 THEN getLabelById should be same as created and version +1`() {
        I18nMongoDAO.save(mockedI18n)
        val label = I18nMongoDAO.getLabelById(i18nId)
        assertEquals(label?._id,i18nId)
        assertEquals(label?.defaultLabel,mockedI18n.defaultLabel)
        assertEquals(label?.category,mockedI18n.category)
        assertEquals(label?.i18n,mockedI18n.i18n)
        assertEquals(label?.version,mockedI18n.version+1)
    }

    @Test
    fun `GIVEN answer WHEN not created THEN getLabelById should be null`() {
        assertNull(I18nMongoDAO.getLabelById(unknownRandomId))
    }

    @Test
    fun `GIVEN answer WHEN saveI18 THEN getLabelsByIds should be same as created and version +1`() {
        I18nMongoDAO.save(mockedI18n)
        val reallyLongId400chars = "word".repeat(10).toId<I18nLabel>()
        I18nMongoDAO.save(mockedI18n.copy(reallyLongId400chars))

        val labels = I18nMongoDAO.getLabelsByIds(setOf(i18nId,reallyLongId400chars))
        assertEquals(labels.size,2)
    }

    @Test
    fun `GIVEN answer WHEN not created THEN getLabelsByIds should be empty`() {
        assertEquals(emptyList(), I18nMongoDAO.getLabelsByIds(setOf(unknownRandomId)))
    }
}
