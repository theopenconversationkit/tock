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

package mongo.mock

import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelFilter
import ai.tock.translator.I18nLabelStat
import ai.tock.translator.I18nLocalizedLabel
import io.mockk.mockk
import org.litote.kmongo.Id
import java.time.Instant

/**
 * Mocked implementation to use I18nDAO since I18nMongoDAO is internal in tock-bot-storage-mongo
 */
class I18nMongoDAOMock : I18nDAO {
    override fun listenI18n(listener: (Id<I18nLabel>) -> Unit) = mockk<Unit>()

    override fun getLabels(namespace: String, filter: I18nLabelFilter?): List<I18nLabel> = mockk()

    override fun getLabelById(id: Id<I18nLabel>): I18nLabel? = mockk()

    override fun getLabelsByIds(ids: Set<Id<I18nLabel>>): List<I18nLabel> = mockk()

    override fun save(label: I18nLabel) = mockk<Unit>()

    override fun save(i18n: List<I18nLabel>) = mockk<Unit>()

    override fun saveIfNotExist(i18n: List<I18nLabel>) = mockk<Unit>()

    override fun deleteByNamespaceAndId(namespace: String, id: Id<I18nLabel>) = mockk<Unit>()

    override fun addAlternativeIndex(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        alternativeIndex: Int,
        contextId: String
    ) = mockk<Unit>()

    override fun deleteAlternativeIndexes(label: I18nLabel, localized: I18nLocalizedLabel, contextId: String) =
        mockk<Unit>()

    override fun getAlternativeIndexes(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        contextId: String
    ): Set<Int> = mockk()

    override fun incrementLabelStat(stat: I18nLabelStat) = mockk<Unit>()

    override fun getLabelStats(namespace: String): List<I18nLabelStat> = mockk()

    override fun getLabelIdsFromStats(namespace: String, timeMarker: Instant): Set<Id<I18nLabel>> = mockk()

}