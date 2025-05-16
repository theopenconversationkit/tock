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

import ai.tock.bot.mongo.I18nAlternativeIndex_.Companion.ConnectorId
import ai.tock.bot.mongo.I18nAlternativeIndex_.Companion.ContextId
import ai.tock.bot.mongo.I18nAlternativeIndex_.Companion.Date
import ai.tock.bot.mongo.I18nAlternativeIndex_.Companion.Index
import ai.tock.bot.mongo.I18nAlternativeIndex_.Companion.InterfaceType
import ai.tock.bot.mongo.I18nAlternativeIndex_.Companion.LabelId
import ai.tock.bot.mongo.I18nAlternativeIndex_.Companion.Locale
import ai.tock.bot.mongo.MongoBotConfiguration.asyncDatabase
import ai.tock.bot.mongo.MongoBotConfiguration.database
import ai.tock.shared.defaultLocale
import ai.tock.shared.ensureIndex
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.error
import ai.tock.shared.longProperty
import ai.tock.shared.watch
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelContract
import ai.tock.translator.I18nLabelFilter
import ai.tock.translator.I18nLabelStat
import ai.tock.translator.I18nLabelStat_
import ai.tock.translator.I18nLabelStateFilter.ALL
import ai.tock.translator.I18nLabelStateFilter.VALIDATED
import ai.tock.translator.I18nLabel_.Companion.Category
import ai.tock.translator.I18nLabel_.Companion.DefaultLabel
import ai.tock.translator.I18nLabel_.Companion.I18n
import ai.tock.translator.I18nLabel_.Companion.Namespace
import ai.tock.translator.I18nLabel_.Companion._id
import ai.tock.translator.I18nLocalizedLabel
import com.mongodb.client.model.Filters.regex
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.UpdateOptions
import java.time.Instant
import java.util.concurrent.TimeUnit
import mu.KotlinLogging
import org.bson.BsonString
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.currentDate
import org.litote.kmongo.deleteOne
import org.litote.kmongo.elemMatch
import org.litote.kmongo.eq
import org.litote.kmongo.excludeId
import org.litote.kmongo.fields
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.gte
import org.litote.kmongo.inc
import org.litote.kmongo.include
import org.litote.kmongo.nin
import org.litote.kmongo.not
import org.litote.kmongo.or
import org.litote.kmongo.projection
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.regex
import org.litote.kmongo.setValue
import org.litote.kmongo.toId
import org.litote.kmongo.updateOneById
import org.litote.kmongo.upsert
import org.litote.kmongo.withDocumentClass

/**
 *
 */
internal object I18nMongoDAO : I18nDAO {

    private val logger = KotlinLogging.logger {}

    private val col = database.getCollection<I18nLabel>().apply {
        ensureIndex(Namespace, Category)
    }
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
            ensureIndex(Namespace, LastUpdate)
        }
    }

    private fun sortLabels(list: List<I18nLabel>): List<I18nLabel> =
        list.sortedWith(compareBy({ it.category }, { it.findLabel(defaultLocale, null)?.label ?: "" }))

    private fun sortLocalizedLabels(list: MutableSet<I18nLocalizedLabel>): LinkedHashSet<I18nLocalizedLabel> =
        LinkedHashSet(list.sortedWith(compareBy({ it.locale.language }, { it.interfaceType }, { it.connectorId })))

    private fun sortLocalizedLabels(label: I18nLabelContract): I18nLabelContract =
        label.withUpdatedI18n(sortLocalizedLabels(label.i18n), version = label.version?.apply(Int::inc))

    override fun listenI18n(listener: (Id<I18nLabel>) -> Unit) {
        asyncCol.watch {
            it.documentKey?.get("_id")?.let { id -> listener((id as BsonString).value.toId()) }
        }
    }

    override fun getLabels(namespace: String, filter: I18nLabelFilter?): List<I18nLabel> {
        val labelIds: Set<Id<I18nLabel>>? = filter?.notUsedSince?.let { notUsedSinceDays ->
            getLabelIdsFromStats(namespace, notUsedSinceDays)
        }
        val filters = listOf(Namespace eq namespace) + (filter?.toFilterList(labelIds) ?: emptyList())
        return sortLabels(col.find(and(filters)).toList())
    }

    private fun I18nLabelFilter.toFilterList(labelIds: Set<Id<I18nLabel>>?): Iterable<Bson?> = listOfNotNull(
        label?.let {
            or(
                DefaultLabel.regex(it.trim(), "i"),
                I18n elemMatch (I18nLocalizedLabel::label.regex(it.trim(), "i")),
                I18n elemMatch (I18nLocalizedLabel::alternatives elemMatch regex(it.trim(), "i"))
            )
        },
        category?.let { Category eq category },
        state.takeIf { it != ALL }?.let {
            val atLeastOneValidated = it == VALIDATED
            if (atLeastOneValidated) {
                // Filters i18n labels that have at least one validated localised label
                I18n elemMatch (I18nLocalizedLabel::validated eq true)
            } else {
                // Filters i18n labels that do not have even one validated localised label
                not(I18n elemMatch (I18nLocalizedLabel::validated eq true))
            }
        },
        labelIds?.takeUnless { it.isEmpty() }?.let { filteredIds -> _id nin filteredIds }
    )

    override fun getLabelById(id: Id<I18nLabel>): I18nLabel? {
        return col.findOneById(id)
    }

    override fun save(label: I18nLabelContract) {
        val sortedLabel = sortLocalizedLabels(label)
        //update default label
        val defaultLabel = sortedLabel.run {
            withDefaultLabel(i18n.firstOrNull { it.locale == defaultLocale }?.label ?: defaultLabel)
        }
        col.updateOneById(defaultLabel._id, defaultLabel, options = UpdateOptions().upsert(true), updateOnlyNotNullProperties = true)
    }

    override fun save(i18n: List<I18nLabelContract>) {
        i18n.forEach { save(it) }
    }

    override fun saveIfNotExist(i18n: List<I18nLabelContract>) {
        val existingIds = sortLabels(col.find().toList()).map { it._id }.toSet()
        save(i18n.filterNot { existingIds.contains(it._id) })
    }

    override fun deleteByNamespaceAndId(namespace: String, id: Id<I18nLabel>) {
        col.deleteOne(Namespace eq namespace, _id eq id)
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

    override fun getLabelIdsFromStats(namespace: String, timeMarker: Instant): Set<Id<I18nLabel>> {
        return statCol
            .projection(
                I18nLabelStat::labelId,
                and(
                    I18nLabelStat_.Namespace eq namespace,
                    I18nLabelStat_.LastUpdate gte timeMarker
                )
            ).distinct().toSet()
    }
}
