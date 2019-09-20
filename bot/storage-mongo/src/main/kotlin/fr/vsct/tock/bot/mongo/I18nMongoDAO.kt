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

import com.mongodb.client.model.IndexOptions
import fr.vsct.tock.bot.mongo.I18nAlternativeIndex_.Companion.ConnectorId
import fr.vsct.tock.bot.mongo.I18nAlternativeIndex_.Companion.ContextId
import fr.vsct.tock.bot.mongo.I18nAlternativeIndex_.Companion.Date
import fr.vsct.tock.bot.mongo.I18nAlternativeIndex_.Companion.Index
import fr.vsct.tock.bot.mongo.I18nAlternativeIndex_.Companion.InterfaceType
import fr.vsct.tock.bot.mongo.I18nAlternativeIndex_.Companion.LabelId
import fr.vsct.tock.bot.mongo.I18nAlternativeIndex_.Companion.Locale
import fr.vsct.tock.bot.mongo.MongoBotConfiguration.asyncDatabase
import fr.vsct.tock.bot.mongo.MongoBotConfiguration.database
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.watch
import fr.vsct.tock.translator.I18nDAO
import fr.vsct.tock.translator.I18nLabel
import fr.vsct.tock.translator.I18nLabelStat
import fr.vsct.tock.translator.I18nLabelStat_
import fr.vsct.tock.translator.I18nLabel_
import fr.vsct.tock.translator.I18nLabel_.Companion._id
import fr.vsct.tock.translator.I18nLocalizedLabel
import mu.KotlinLogging
import org.bson.BsonString
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.currentDate
import org.litote.kmongo.deleteOne
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.excludeId
import org.litote.kmongo.fields
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.inc
import org.litote.kmongo.include
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.save
import org.litote.kmongo.setValue
import org.litote.kmongo.toId
import org.litote.kmongo.upsert
import org.litote.kmongo.withDocumentClass
import java.util.concurrent.TimeUnit

/**
 *
 */
internal object I18nMongoDAO : I18nDAO {

    private val logger = KotlinLogging.logger {}

    private val col = database.getCollection<I18nLabel>()
    private val asyncCol = asyncDatabase.getCollection<I18nLabel>()
    private val alternativeIndexCol = database.getCollection<I18nAlternativeIndex>().apply {
        ensureIndex(ContextId, LabelId, I18nAlternativeIndex_.Namespace, Locale, InterfaceType, ConnectorId)
        ensureIndex(
            Date,
            indexOptions = IndexOptions().expireAfter(
                longProperty("tock_bot_alternative_index_ttl_hours", 1),
                TimeUnit.HOURS
            )
        )
    }
    private val statCol = database.getCollection<I18nLabelStat>().apply {
        I18nLabelStat_.apply {
            ensureUniqueIndex(LabelId, Locale, InterfaceType, ConnectorId)
            ensureIndex(Namespace)
        }
    }

    private fun sortLabels(list: List<I18nLabel>): List<I18nLabel> =
        list.sortedWith(compareBy({ it.category }, { it.findLabel(defaultLocale, null)?.label ?: "" }))

    private fun sortLocalizedLabels(list: MutableSet<I18nLocalizedLabel>): LinkedHashSet<I18nLocalizedLabel> =
        LinkedHashSet(list.sortedWith(compareBy({ it.locale.language }, { it.interfaceType }, { it.connectorId })))

    private fun sortLocalizedLabels(label: I18nLabel): I18nLabel =
        label.copy(i18n = sortLocalizedLabels(label.i18n), version = label.version + 1)

    override fun listenI18n(listener: (Id<I18nLabel>) -> Unit) {
        asyncCol.watch {
            it.documentKey?.get("_id")?.let { id -> listener((id as BsonString).value.toId()) }
        }
    }

    override fun getLabels(namespace: String): List<I18nLabel> {
        return sortLabels(col.find(I18nLabel_.Namespace eq namespace).toList())
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
        val existingIds = sortLabels(col.find().toList()).map { it._id }.toSet()
        save(i18n.filterNot { existingIds.contains(it._id) })
    }

    override fun deleteByNamespaceAndId(namespace: String, id: Id<I18nLabel>) {
        col.deleteOne(I18nLabel_.Namespace eq namespace, _id eq id)
    }

    override fun addAlternativeIndex(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        alternativeIndex: Int,
        contextId: String
    ) {
        try {
            alternativeIndexCol.insertOne(I18nAlternativeIndex(label, localized, alternativeIndex, contextId))
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    private fun alternativeIndexesFilter(label: I18nLabel, localized: I18nLocalizedLabel, contextId: String): Bson {
        return and(
            ContextId eq contextId,
            LabelId eq label._id,
            label::namespace eq label.namespace,
            Locale eq localized.locale,
            InterfaceType eq localized.interfaceType,
            ConnectorId eq localized.connectorId
        )
    }

    override fun deleteAlternativeIndexes(label: I18nLabel, localized: I18nLocalizedLabel, contextId: String) {
        try {
            alternativeIndexCol.deleteMany(
                alternativeIndexesFilter(label, localized, contextId)
            )
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    override fun getAlternativeIndexes(label: I18nLabel, localized: I18nLocalizedLabel, contextId: String): Set<Int> =
        try {
            alternativeIndexCol
                .withDocumentClass<Document>()
                .find(alternativeIndexesFilter(label, localized, contextId))
                .projection(fields(include(Index), excludeId()))
                .map { it.getInteger(Index.name) }
                .toSet()
        } catch (e: Exception) {
            logger.error(e)
            emptySet()
        }

    override fun incrementLabelStat(stat: I18nLabelStat) {
        I18nLabelStat_.apply {
            statCol.updateOne(
                and(
                    LabelId eq stat.labelId,
                    Locale eq stat.locale,
                    InterfaceType eq stat.interfaceType,
                    ConnectorId eq stat.connectorId
                ),
                combine(
                    inc(Count, stat.count),
                    currentDate(LastUpdate),
                    setValue(Namespace, stat.namespace)
                ),
                upsert()
            )
        }
    }

    override fun getLabelStats(namespace: String): List<I18nLabelStat> {
        return statCol.find(I18nLabelStat_.Namespace eq namespace).toList()
    }
}