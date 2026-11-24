/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.storage.dynamodb

import ai.tock.shared.defaultLocale
import ai.tock.shared.defaultNamespace
import ai.tock.shared.longProperty
import ai.tock.storage.dynamodb.DynamoStore.configTable
import ai.tock.storage.dynamodb.DynamoStore.runtimeTable
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelContract
import ai.tock.translator.I18nLabelFilter
import ai.tock.translator.I18nLabelStat
import ai.tock.translator.I18nLabelStateFilter.ALL
import ai.tock.translator.I18nLabelStateFilter.VALIDATED
import ai.tock.translator.I18nLocalizedLabel
import mu.KotlinLogging
import org.litote.kmongo.Id
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * The labels, their statistics and the alternatives already used.
 *
 * - a label is stored twice in the configuration table, in one transaction: `I18N#<id>` / `L` for [getLabelById],
 *   the label of each answer, and `I18NS#<namespace>` / `<id>` for [getLabels], which loads a namespace at startup;
 * - a statistic is a counter of the runtime table, `I18NSTAT#<namespace>` / `<labelId>|<locale>|<interface>|<connector>`,
 *   incremented by an atomic `ADD`;
 * - the alternatives used in a context are items of the runtime table, `I18NALT#<context>|<label>|...` / `<index>`,
 *   expired after `tock_bot_alternative_index_ttl_hours` (1), as the Mongo TTL index does.
 *
 * No change stream: [listenI18n] listens to the publications of the admin ([DynamoChanges]). A publication does
 * not say which labels changed: the listener is called for every label returned by [getLabels], the only ones the
 * `Translator` caches.
 */
object I18nDynamoDAO : I18nDAO {
    private val logger = KotlinLogging.logger {}

    private val alternativeIndexTtlSeconds = longProperty("tock_bot_alternative_index_ttl_hours", 1) * 3600

    private const val LABEL_SK = "L"
    private const val COUNT = "count"
    private const val LAST_UPDATE = "lastUpdate"

    private fun labelPk(id: Id<I18nLabel>) = "I18N#$id"

    private fun namespacePk(namespace: String) = "I18NS#$namespace"

    /** The labels returned by [getLabels]: the ones that a listener may have cached. */
    private val returnedLabelIds: MutableSet<Id<I18nLabel>> = ConcurrentHashMap.newKeySet()

    override fun listenI18n(listener: (Id<I18nLabel>) -> Unit) =
        DynamoChanges.listen(DynamoChanges.LABEL) {
            returnedLabelIds.toList().forEach(listener)
        }

    private fun sortLabels(list: List<I18nLabel>): List<I18nLabel> = list.sortedWith(compareBy({ it.category }, { it.findLabel(defaultLocale, null)?.label ?: "" }))

    private fun sortLocalizedLabels(list: MutableSet<I18nLocalizedLabel>): LinkedHashSet<I18nLocalizedLabel> =
        LinkedHashSet(list.sortedWith(compareBy({ it.locale.language }, { it.interfaceType }, { it.connectorId })))

    private fun sortLocalizedLabels(label: I18nLabelContract): I18nLabelContract = label.withUpdatedI18n(sortLocalizedLabels(label.i18n), version = label.version)

    override fun getLabels(
        namespace: String,
        filter: I18nLabelFilter?,
    ): List<I18nLabel> {
        val labels =
            DynamoStore
                .queryPartition(configTable, namespacePk(namespace), consistentRead = true)
                .map { DocumentCodec.decode<I18nLabel>(it.bytes(DOC)) }
        labels.forEach { returnedLabelIds += it._id }
        val recentlyUsed = filter?.notUsedSince?.let { getLabelIdsFromStats(namespace, it) }
        return sortLabels(if (filter == null) labels else labels.filter { filter.matches(it, recentlyUsed) })
    }

    /** The filters of the Mongo DAO, in memory: a namespace has a few hundred labels. */
    private fun I18nLabelFilter.matches(
        label: I18nLabel,
        recentlyUsed: Set<Id<I18nLabel>>?,
    ): Boolean {
        val text = this.label?.trim()?.let { Regex(it, RegexOption.IGNORE_CASE) }
        return (
            text == null ||
                label.defaultLabel?.let { text.containsMatchIn(it) } == true ||
                label.i18n.any { localized -> text.containsMatchIn(localized.label) || localized.alternatives.any { text.containsMatchIn(it) } }
        ) &&
            (category == null || label.category == category) &&
            (state == ALL || (state == VALIDATED) == label.i18n.any { it.validated }) &&
            (recentlyUsed.isNullOrEmpty() || label._id !in recentlyUsed)
    }

    override fun getLabelById(id: Id<I18nLabel>): I18nLabel? = DynamoStore.getItem(configTable, labelPk(id), LABEL_SK)?.let { DocumentCodec.decode<I18nLabel>(it.bytes(DOC)) }

    /**
     * Stores [label] as the Mongo DAO does: localized labels sorted, default label updated, and the null
     * properties of [label] do not replace the stored ones (`updateOnlyNotNullProperties`).
     */
    override fun save(label: I18nLabelContract) {
        val sortedLabel = sortLocalizedLabels(label)
        // the same expression as the Mongo DAO: defaultLocale is the one of the label
        val withDefault =
            sortedLabel.run {
                withDefaultLabel(i18n.firstOrNull { it.locale == defaultLocale }?.label ?: defaultLabel)
            }
        val existing = getLabelById(withDefault._id)
        val defaultLabel = withDefault.defaultLabel ?: existing?.defaultLabel
        val stored =
            I18nLabel(
                _id = withDefault._id,
                namespace = withDefault.namespace ?: existing?.namespace ?: defaultNamespace,
                category = withDefault.category,
                i18n = withDefault.i18n,
                defaultLabel = defaultLabel,
                defaultLocale = withDefault.defaultLocale ?: existing?.defaultLocale ?: I18nLabel.findDefaultLabelLocale(defaultLabel, withDefault.i18n),
                defaultI18n = withDefault.defaultI18n ?: existing?.defaultI18n ?: emptySet(),
                version = withDefault.version ?: existing?.version ?: 0,
            )
        write(stored, existing)
    }

    /** Stores [label] as it is, as published by the admin. */
    fun publish(label: I18nLabel) {
        write(label, getLabelById(label._id))
    }

    private fun write(
        stored: I18nLabel,
        existing: I18nLabel?,
    ) {
        val document = b(DocumentCodec.encode(stored))
        DynamoStore.transactWrite(
            listOfNotNull(
                DynamoStore.transactPut(configTable, DynamoStore.key(labelPk(stored._id), LABEL_SK) + (DOC to document)),
                DynamoStore.transactPut(configTable, DynamoStore.key(namespacePk(stored.namespace), stored._id.toString()) + (DOC to document)),
                existing
                    ?.takeIf { it.namespace != stored.namespace }
                    ?.let { DynamoStore.transactDelete(configTable, DynamoStore.key(namespacePk(it.namespace), it._id.toString())) },
            ),
        )
    }

    override fun save(i18n: List<I18nLabelContract>) {
        i18n.forEach { save(it) }
    }

    override fun saveIfNotExist(i18n: List<I18nLabelContract>) {
        i18n.filter { getLabelById(it._id) == null }.forEach { save(it) }
    }

    override fun deleteByNamespaceAndId(
        namespace: String,
        id: Id<I18nLabel>,
    ) {
        val label = getLabelById(id)?.takeIf { it.namespace == namespace } ?: return
        DynamoStore.batchWrite(
            configTable,
            deleteKeys = listOf(DynamoStore.key(labelPk(id), LABEL_SK), DynamoStore.key(namespacePk(label.namespace), id.toString())),
        )
    }

    // ---------------------------------------------------------------- alternatives

    private fun alternativePk(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        contextId: String,
    ) = "I18NALT#$contextId|${label._id}|${label.namespace}|${localized.locale}|${localized.interfaceType}|${localized.connectorId ?: ""}"

    override fun addAlternativeIndex(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        alternativeIndex: Int,
        contextId: String,
    ) {
        try {
            DynamoStore.putItem(
                runtimeTable,
                DynamoStore.key(alternativePk(label, localized, contextId), "%06d".format(alternativeIndex)) +
                    (TTL to n(Instant.now().epochSecond + alternativeIndexTtlSeconds)),
            )
        } catch (e: Exception) {
            logger.error(e) { "unable to store the alternative $alternativeIndex of ${label._id}" }
        }
    }

    override fun deleteAlternativeIndexes(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        contextId: String,
    ) {
        try {
            DynamoStore.deletePartition(runtimeTable, alternativePk(label, localized, contextId))
        } catch (e: Exception) {
            logger.error(e) { "unable to delete the alternatives of ${label._id}" }
        }
    }

    override fun getAlternativeIndexes(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        contextId: String,
    ): Set<Int> =
        try {
            val now = Instant.now().epochSecond
            DynamoStore
                .queryPartition(runtimeTable, alternativePk(label, localized, contextId), consistentRead = true)
                // the DynamoDB TTL removes the items late
                .filter { (it.long(TTL) ?: Long.MAX_VALUE) > now }
                .mapNotNull { it.str(SK)?.toIntOrNull() }
                .toSet()
        } catch (e: Exception) {
            logger.error(e) { "unable to read the alternatives of ${label._id}" }
            emptySet()
        }

    // ---------------------------------------------------------------- statistics

    private fun statPk(namespace: String) = "I18NSTAT#$namespace"

    private fun statSk(stat: I18nLabelStat) = "${stat.labelId}|${stat.locale}|${stat.interfaceType}|${stat.connectorId ?: ""}"

    override fun incrementLabelStat(stat: I18nLabelStat) {
        val update = "ADD #count :count SET #lastUpdate = :now, #doc = if_not_exists(#doc, :doc)"
        DynamoStore.client
            .updateItem {
                it
                    .tableName(runtimeTable)
                    .key(DynamoStore.key(statPk(stat.namespace), statSk(stat)))
                    .updateExpression(update)
                    .expressionAttributeNames(mapOf("#count" to COUNT, "#lastUpdate" to LAST_UPDATE, "#doc" to DOC))
                    .expressionAttributeValues(
                        mapOf(
                            ":count" to n(stat.count),
                            ":now" to n(Instant.now().toEpochMilli()),
                            // the key of the statistic: the counter and the date are attributes
                            ":doc" to b(DocumentCodec.encode(stat.copy(count = 0))),
                        ),
                    ).returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
            }.also { DynamoStore.recordWrite(it.consumedCapacity()) }
    }

    override fun getLabelStats(namespace: String): List<I18nLabelStat> =
        DynamoStore.queryPartition(runtimeTable, statPk(namespace), consistentRead = true).map {
            DocumentCodec
                .decode<I18nLabelStat>(it.bytes(DOC))
                .copy(count = it.long(COUNT)?.toInt() ?: 0, lastUpdate = Instant.ofEpochMilli(it.long(LAST_UPDATE) ?: 0))
        }

    override fun getLabelIdsFromStats(
        namespace: String,
        timeMarker: Instant,
    ): Set<Id<I18nLabel>> = getLabelStats(namespace).filter { !it.lastUpdate.isBefore(timeMarker) }.map { it.labelId }.toSet()
}
