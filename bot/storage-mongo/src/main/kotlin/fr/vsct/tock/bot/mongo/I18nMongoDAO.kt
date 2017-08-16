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
import fr.vsct.tock.translator.I18nDAO
import fr.vsct.tock.translator.I18nLabel
import org.litote.kmongo.deleteOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.save

/**
 *
 */
internal object I18nMongoDAO : I18nDAO {

    private val col = database.getCollection<I18nLabel>()

    override fun getLabels(): List<I18nLabel> {
        return col.find().toList()
    }

    override fun getLabelById(id: String): I18nLabel? {
        return col.findOneById(id)
    }

    override fun save(i18n: I18nLabel) {
        col.save(i18n)
    }

    override fun save(i18n: List<I18nLabel>) {
        i18n.forEach { save(it) }
    }

    override fun deleteByNamespaceAndId(namespace: String, id: String) {
        col.deleteOne("{namespace:${namespace.json}, _id:${id.json}}")
    }
}