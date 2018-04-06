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

package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.mongo.MongoBotConfiguration.database
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.translator.I18nDAO
import fr.vsct.tock.translator.I18nLabel
import fr.vsct.tock.translator.I18nLabel_.Companion.Namespace
import fr.vsct.tock.translator.I18nLabel_.Companion._id
import fr.vsct.tock.translator.I18nLocalizedLabel
import org.litote.kmongo.Id
import org.litote.kmongo.deleteOne
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.save

/**
 *
 */
internal object I18nMongoDAO : I18nDAO {

    private val col = database.getCollection<I18nLabel>()

    private fun sortLabels(list: List<I18nLabel>): List<I18nLabel> =
        list.sortedWith(compareBy({ it.category }, { it.findLabel(defaultLocale, null)?.label ?: "" }))

    private fun sortLocalizedLabels(list: LinkedHashSet<I18nLocalizedLabel>): LinkedHashSet<I18nLocalizedLabel> =
        LinkedHashSet(list.sortedWith(compareBy({ it.locale.language }, { it.interfaceType }, { it.connectorId })))

    private fun sortLocalizedLabels(label: I18nLabel): I18nLabel =
        label.copy(i18n = sortLocalizedLabels(label.i18n))

    override fun getLabels(): List<I18nLabel> {
        return sortLabels(col.find().toList())
    }

    override fun getLabelById(id: Id<I18nLabel>): I18nLabel? {
        return col.findOneById(id)
    }

    override fun save(i18n: I18nLabel) {
        col.save(sortLocalizedLabels(i18n))
    }

    override fun save(i18n: List<I18nLabel>) {
        i18n.forEach { save(it) }
    }

    override fun saveIfNotExist(i18n: List<I18nLabel>) {
        val existingIds = getLabels().map { it._id }.toSet()
        save(i18n.filterNot { existingIds.contains(it._id) })
    }

    override fun deleteByNamespaceAndId(namespace: String, id: Id<I18nLabel>) {
        col.deleteOne(Namespace eq namespace, _id eq id)
    }
}