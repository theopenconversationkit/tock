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

package ai.tock.bot.mongo

import ai.tock.bot.mongo.I18nMongoDAO.getAlternativeIndexes
import ai.tock.shared.defaultLocale
import ai.tock.shared.defaultNamespace
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.defaultUserInterface
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import kotlin.test.assertEquals

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
            getAlternativeIndexes(label, localized, "id"),
        )
    }
}
