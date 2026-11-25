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

import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import org.litote.kmongo.Id
import java.time.Instant

/**
 * The part of [I18nDAO] that the bots use at runtime.
 *
 * A storage that only serves the bots (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [I18nDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface I18nRuntimeDAO {
    fun listenI18n(listener: (Id<I18nLabel>) -> Unit)

    fun getLabels(
        namespace: String,
        filter: I18nLabelFilter? = null,
    ): List<I18nLabel>

    fun getLabelById(id: Id<I18nLabel>): I18nLabel?

    fun save(label: I18nLabel)

    fun save(label: I18nLabelContract)

    fun save(i18n: List<I18nLabelContract>)

    fun saveIfNotExist(i18n: List<I18nLabelContract>)

    fun addAlternativeIndex(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        alternativeIndex: Int,
        contextId: String,
    )

    fun deleteAlternativeIndexes(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        contextId: String,
    )

    fun getAlternativeIndexes(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        contextId: String,
    ): Set<Int>

    fun incrementLabelStat(stat: I18nLabelStat)

    companion object {
        /**
         * The bound [I18nRuntimeDAO], or the bound [I18nDAO] adapted to it.
         */
        fun fromInjector(): I18nRuntimeDAO = injector.provideOrDefault<I18nRuntimeDAO> { injector.provide<I18nDAO>().asRuntimeDAO() }
    }
}

/**
 * Adapts this DAO to [I18nRuntimeDAO].
 */
fun I18nDAO.asRuntimeDAO(): I18nRuntimeDAO = I18nRuntimeDAOAdapter(this)

private class I18nRuntimeDAOAdapter(
    private val dao: I18nDAO,
) : I18nRuntimeDAO {
    override fun listenI18n(listener: (Id<I18nLabel>) -> Unit) = dao.listenI18n(listener)

    override fun getLabels(
        namespace: String,
        filter: I18nLabelFilter?,
    ): List<I18nLabel> = dao.getLabels(namespace, filter)

    override fun getLabelById(id: Id<I18nLabel>): I18nLabel? = dao.getLabelById(id)

    override fun save(label: I18nLabel) = dao.save(label)

    override fun save(label: I18nLabelContract) = dao.save(label)

    override fun save(i18n: List<I18nLabelContract>) = dao.save(i18n)

    override fun saveIfNotExist(i18n: List<I18nLabelContract>) = dao.saveIfNotExist(i18n)

    override fun addAlternativeIndex(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        alternativeIndex: Int,
        contextId: String,
    ) = dao.addAlternativeIndex(label, localized, alternativeIndex, contextId)

    override fun deleteAlternativeIndexes(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        contextId: String,
    ) = dao.deleteAlternativeIndexes(label, localized, contextId)

    override fun getAlternativeIndexes(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        contextId: String,
    ): Set<Int> = dao.getAlternativeIndexes(label, localized, contextId)

    override fun incrementLabelStat(stat: I18nLabelStat) = dao.incrementLabelStat(stat)
}
