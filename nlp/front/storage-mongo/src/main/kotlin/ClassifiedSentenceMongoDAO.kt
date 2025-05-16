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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.core.Intent
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedEntity_.Companion.Role
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.inbox
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.config.SentencesQueryResult
import ai.tock.nlp.front.storage.mongo.ApplicationDefinitionMongoDAO.getApplicationById
import ai.tock.nlp.front.storage.mongo.ApplicationDefinitionMongoDAO.getApplicationsByNamespace
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.ApplicationId
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.Classifier
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.ForReview
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.FullText
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.Language
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.LastEntityProbability
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.LastIntentProbability
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.LastUsage
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.NormalizedText
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.Configuration
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.Status
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.Text
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.UnknownCount
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.UpdateDate
import ai.tock.nlp.front.storage.mongo.ClassifiedSentenceCol_.Companion.UsageCount
import ai.tock.nlp.front.storage.mongo.MongoFrontConfiguration.database
import ai.tock.nlp.front.storage.mongo.ParseRequestLogMongoDAO.ParseRequestLogStatCol
import ai.tock.shared.Executor
import ai.tock.shared.defaultLocale
import ai.tock.shared.ensureIndex
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.error
import ai.tock.shared.normalize
import ai.tock.shared.injector
import ai.tock.shared.listProperty
import ai.tock.shared.longProperty
import ai.tock.shared.namespace
import ai.tock.shared.provide
import ai.tock.shared.safeCollation
import ai.tock.shared.security.UserLogin
import com.mongodb.ReadPreference.secondaryPreferred
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Collation
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.ReplaceOptions
import mu.KotlinLogging
import org.bson.conversions.Bson
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.MongoOperator.elemMatch
import org.litote.kmongo.MongoOperator.pull
import org.litote.kmongo.`in`
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.descendingSort
import org.litote.kmongo.distinct
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.getCollection
import org.litote.kmongo.gt
import org.litote.kmongo.inc
import org.litote.kmongo.json
import org.litote.kmongo.lt
import org.litote.kmongo.lte
import org.litote.kmongo.ne
import org.litote.kmongo.nin
import org.litote.kmongo.or
import org.litote.kmongo.orderBy
import org.litote.kmongo.pullByFilter
import org.litote.kmongo.regex
import org.litote.kmongo.replaceOneWithFilter
import org.litote.kmongo.setTo
import org.litote.kmongo.setValue
import org.litote.kmongo.updateMany
import java.time.Duration
import java.time.Instant
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.TimeUnit.DAYS

/**
 *
 */
internal object ClassifiedSentenceMongoDAO : ClassifiedSentenceDAO {

    private val logger = KotlinLogging.logger {}

    // alias
    private val Classification_ = ClassifiedSentenceCol_.Classification

    @Data(internal = true)
    @JacksonData(internal = true)
    data class ClassifiedSentenceCol(
        val text: String,
        val normalizedText: String = text,
        val fullText: String = text,
        val language: Locale,
        val applicationId: Id<ApplicationDefinition>,
        val creationDate: Instant,
        val updateDate: Instant,
        val status: ClassifiedSentenceStatus,
        val classification: Classification,
        val lastIntentProbability: Double? = null,
        val lastEntityProbability: Double? = null,
        val lastUsage: Instant? = null,
        val usageCount: Long? = 0,
        val unknownCount: Long? = 0,
        val forReview: Boolean = false,
        val reviewComment: String? = null,
        val classifier: UserLogin? = null,
        val otherIntentsProbabilities: Map<String, Double> = emptyMap(),
        val configuration : String? = null
    ) {

        constructor(sentence: ClassifiedSentence) :
                this(
                    textKey(sentence.text),
                    sentence.text.normalize(sentence.language),
                    sentence.text,
                    sentence.language,
                    sentence.applicationId,
                    sentence.creationDate,
                    now(),
                    sentence.status,
                    sentence.classification,
                    sentence.lastIntentProbability,
                    sentence.lastEntityProbability,
                    sentence.lastUsage,
                    sentence.usageCount,
                    sentence.unknownCount,
                    sentence.forReview,
                    sentence.reviewComment,
                    sentence.qualifier,
                    sentence.otherIntentsProbabilities,
                    sentence.configuration
                )

        fun toSentence(): ClassifiedSentence =
            ClassifiedSentence(
                fullText,
                language,
                applicationId,
                creationDate,
                updateDate,
                status,
                classification,
                lastIntentProbability,
                lastEntityProbability,
                lastUsage,
                usageCount ?: 0,
                unknownCount ?: 0,
                forReview,
                reviewComment,
                classifier,
                otherIntentsProbabilities,
                configuration
            )
    }

    private val col: MongoCollection<ClassifiedSentenceCol> by lazy {
        val c = database.getCollection<ClassifiedSentenceCol>("classified_sentence")
        try {
            c.ensureUniqueIndex(Text, Language, ApplicationId)
            c.ensureIndex(NormalizedText, Language, ApplicationId)
            c.ensureIndex(Language, ApplicationId, Status)
            c.ensureIndex(Status)
            c.ensureIndex(orderBy(mapOf(ApplicationId to true, Language to true, UpdateDate to false)))
            c.ensureIndex(Language, ApplicationId, UsageCount)
            c.ensureIndex(Language, ApplicationId, UnknownCount)
            c.ensureIndex(Language, Status, Classification_.intentId)
            c.ensureIndex(
                ApplicationId, Classification_.intentId, Language, UpdateDate
            )
            c.ensureIndex(ForReview)

            val ttlIntents = listProperty("tock_nlp_classified_sentences_index_ttl_intent_names", emptyList())
            val ttlDays = longProperty("tock_nlp_classified_sentences_index_ttl_days", -1)
            if (ttlIntents.isEmpty() && ttlDays != -1L) {
                logger.info { "add classified sentence ttl index for $ttlDays days" }
                c.ensureIndex(
                    UpdateDate,
                    indexOptions = IndexOptions()
                        .expireAfter(ttlDays, DAYS)
                        .partialFilterExpression(Status eq inbox)
                )
            } else {
                c.ensureIndex(UpdateDate)
            }

            if (ttlIntents.isNotEmpty() && ttlDays != -1L) {
                logger.info { "add classified sentence periodic crawler for $ttlDays days and intents $ttlIntents" }
                injector.provide<Executor>().setPeriodic(Duration.ofMinutes(1), Duration.ofDays(1)) {
                    val intentIds = IntentDefinitionMongoDAO.getIntentsByNames(ttlIntents).map { it._id }
                    val deleted = c.deleteMany(
                        and(
                            Status eq inbox,
                            Classification_.intentId `in` intentIds,
                            UpdateDate lt now().minus(ttlDays, ChronoUnit.DAYS)
                        )
                    )
                    logger.debug { "delete ${deleted.deletedCount} old classified sentences for intents $ttlIntents of ids $intentIds" }
                }
            }
        } catch (e: Exception) {
            logger.error(e)
        }
        c
    }

    override fun updateFormattedSentences(applicationId: Id<ApplicationDefinition>) {
        logger.debug { "start updating formatted sentences" }
        col.find(ApplicationId eq applicationId).forEach {
            save(it.copy(normalizedText = it.text.normalize(it.language)))
        }
        logger.debug { "end updating formatted sentences" }
    }

    override fun getSentences(
        intents: Set<Id<IntentDefinition>>?,
        language: Locale?,
        status: ClassifiedSentenceStatus?
    ): List<ClassifiedSentence> {
        if (intents == null && language == null && status == null) {
            error("at least one parameter should be not null")
        }

        return col
            .find(
                if (intents != null) Classification_.intentId `in` intents else null,
                if (language != null) Language eq language else null,
                if (status != null) Status eq status else null
            )
            .map { it.toSentence() }
            .toList()
    }

    override fun switchSentencesStatus(sentences: List<ClassifiedSentence>, newStatus: ClassifiedSentenceStatus) {
        sentences.forEach {
            save(it.copy(status = newStatus))
        }
    }

    override fun deleteSentencesByStatus(status: ClassifiedSentenceStatus) {
        col.deleteMany(Status eq status)
    }

    override fun deleteSentencesByApplicationId(applicationId: Id<ApplicationDefinition>) {
        col.deleteMany(ApplicationId eq applicationId)
    }

    override fun save(sentence: ClassifiedSentence) {
        save(ClassifiedSentenceCol(sentence))
    }

    private fun save(s: ClassifiedSentenceCol) {
        col.replaceOneWithFilter(
            and(
                Text eq textKey(s.text),
                Language eq s.language,
                ApplicationId eq s.applicationId
            ),
            s,
            ReplaceOptions().upsert(true)
        )
    }

    override fun users(applicationId: Id<ApplicationDefinition>): List<String> {
        return col.distinct(
            Classifier,
            and(ApplicationId eq applicationId, Status `in` listOf(validated, model))
        ).filterNotNull()
    }


    override fun configurations(applicationId: Id<ApplicationDefinition>): List<String> {
        return col.distinct(
            Configuration,
            and(ApplicationId eq applicationId)
        ).filterNotNull()
    }

    override fun search(query: SentencesQuery): SentencesQueryResult {
        with(query) {
            val filterBase =
                and(
                    filterApplication(),
                    filterLanguage(),
                    filterText(),
                    filterIntent(),
                    filterStatus(),
                    filterEntityType(),
                    filterEntityRolesToInclude(),
                    filterEntityRoleToExclude(),
                    filterSearchMark(),
                    filterModifiedAfter(),
                    filterModifiedBefore(),
                    filterReviewOnly(),
                    filterByUser(),
                    filterByAllButUser(),
                    filterMaxIntentProbability(),
                    filterMinIntentProbability(),
                    filterConfiguration()
                )

            logger.debug { filterBase.json }
            // read secondary for long term operations
            val c = if (onlyExactMatch) col else col.withReadPreference(secondaryPreferred())
            val count = c.countDocuments(filterBase)
            logger.debug { "count : $count" }
            if (count > start) {
                val list = c
                    .find(filterBase)
                    .run {
                        if (query.sort.isEmpty()) {
                            descendingSort(UpdateDate)
                        } else {
                            sort(
                                orderBy(
                                    query.sort.map {
                                        when (it.first) {
                                            "text" -> Text
                                            "currentIntent" -> Classification_.intentId
                                            "intentProbability" -> LastIntentProbability
                                            "entitiesProbability" -> LastEntityProbability
                                            "lastUpdate" -> UpdateDate
                                            "lastUsage" -> LastUsage
                                            "usageCount" -> UsageCount
                                            "unknownCount" -> UnknownCount
                                            else -> UpdateDate
                                        } to it.second
                                    }.toMap()
                                )
                            )
                        }
                    }
                    .run {
                        if (query.sort.isNotEmpty()) {
                            safeCollation(
                                Collation
                                    .builder()
                                    .caseLevel(false)
                                    .locale((query.language ?: defaultLocale).toLanguageTag())
                                    .build()
                            )
                        } else {
                            this
                        }
                    }
                    .skip(start.toInt())
                    .run {
                        size?.let { limit(it) } ?: this
                    }


                return SentencesQueryResult(count, list.map { it.toSentence() }.toList())
            } else {
                return SentencesQueryResult(count, emptyList())
            }
        }
    }

    private fun SentencesQuery.filterApplication() =
        if (wholeNamespace) ApplicationId `in`
                (
                        getApplicationById(applicationId)?.namespace?.let { n -> getApplicationsByNamespace(n).map { it._id } }
                            ?: emptyList()
                        )
        else ApplicationId eq applicationId

    private fun SentencesQuery.filterReviewOnly() = if (onlyToReview) ForReview eq true else null

    private fun SentencesQuery.filterByUser() = if (user != null) Classifier eq user else null

    private fun SentencesQuery.filterByAllButUser() = if (allButUser != null) Classifier ne allButUser else null

    private fun SentencesQuery.filterSearchMark(): Bson? =
        if (searchMark == null) null else UpdateDate lte searchMark?.date

    private fun SentencesQuery.filterModifiedAfter(): Bson? =
        if (modifiedAfter == null) null else UpdateDate gt modifiedAfter?.toInstant()

    private fun SentencesQuery.filterModifiedBefore(): Bson? =
        if (modifiedBefore == null) null else UpdateDate lt modifiedBefore?.toInstant()

    private fun SentencesQuery.filterEntityRoleToExclude(): Bson? = when {
        entityRolesToExclude.isEmpty() -> null
        searchSubEntities -> subEntityRoleQueryToExclude(entityRolesToExclude)
        else -> Classification_.entities.role nin entityRolesToExclude
    }

    private fun SentencesQuery.filterEntityRolesToInclude(): Bson? = when {
        entityRolesToInclude.isEmpty() -> null
        searchSubEntities -> subEntityRoleQueryToInclude(entityRolesToInclude)
        else -> Classification_.entities.role `in` entityRolesToInclude
    }

    private fun SentencesQuery.filterEntityType(): Bson? = when {
        entityType == null -> null
        searchSubEntities -> subEntityTypeQuery(entityType!!)
        else -> Classification_.entities.type eq entityType
    }

    private fun SentencesQuery.filterStatus() =
        if (status.isNotEmpty()) Status `in` status else if (notStatus != null) Status ne notStatus else null

    private fun SentencesQuery.filterConfiguration() = if (configuration == null) null else Configuration eq configuration

    private fun SentencesQuery.filterIntent() = if (intentId == null) null else Classification_.intentId eq intentId

    private fun SentencesQuery.filterText(): Bson? = when {
        search.isNullOrBlank() -> null
        onlyExactMatch -> filterOnlyExactMatchText()
        else -> FullText.regex(search!!.trim(), "i")
    }

    private fun SentencesQuery.filterOnlyExactMatchText() =
        when {
            normalizeText -> {
                NormalizedText eq search?.normalize(language ?: defaultLocale)
            }
            else -> Text eq search
        }

    private fun SentencesQuery.filterLanguage() = if (language == null) null else Language eq language

    private fun SentencesQuery.filterMaxIntentProbability(): Bson? =
        if (maxIntentProbability < 1f) LastIntentProbability lt maxIntentProbability.toDouble() else null

    private fun SentencesQuery.filterMinIntentProbability(): Bson? =
        if (minIntentProbability > 0f) LastIntentProbability gt minIntentProbability.toDouble() else null

    // ugly
    private fun subEntityTypeQuery(entityType: String): Bson =
        or(
            Classification_.entities.type eq entityType,
            Classification_.entities.subEntities.type eq entityType,
            Classification_.entities.subEntities.subEntities.type eq entityType,
            Classification_.entities.subEntities.subEntities.subEntities.type eq entityType,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.type eq entityType,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.subEntities.type eq entityType,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.type eq entityType,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.type eq entityType,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.type eq entityType
        )

    private fun subEntityRoleQueryToInclude(roles: List<String>): Bson =
        or(
            Classification_.entities.role `in` roles,
            Classification_.entities.subEntities.role `in` roles,
            Classification_.entities.subEntities.subEntities.role `in` roles,
            Classification_.entities.subEntities.subEntities.subEntities.role `in` roles,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.role `in` roles,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.subEntities.role `in` roles,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.role `in` roles,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.role `in` roles,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.role `in` roles
        )

    private fun subEntityRoleQueryToExclude(roles: List<String>): Bson =
        and(
            Classification_.entities.role nin roles,
            Classification_.entities.subEntities.role nin roles,
            Classification_.entities.subEntities.subEntities.role nin roles,
            Classification_.entities.subEntities.subEntities.subEntities.role nin roles,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.role nin roles,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.subEntities.role nin roles,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.role nin roles,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.role nin roles,
            Classification_.entities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.subEntities.role nin roles
        )

    override fun switchSentencesIntent(
        applicationId: Id<ApplicationDefinition>,
        oldIntentId: Id<IntentDefinition>,
        newIntentId: Id<IntentDefinition>
    ) {
        col.updateMany(
            and(
                ApplicationId eq applicationId,
                Classification_.intentId eq oldIntentId
            ),
            Classification_.intentId setTo newIntentId,
            Classification_.entities setTo emptyList(),
            Status setTo inbox
        )
    }

    override fun switchSentencesIntent(
        sentences: List<ClassifiedSentence>,
        newIntentId: Id<IntentDefinition>
    ) {
        // TODO what if new intent does not contains existing entities?
        sentences.forEach {
            if (newIntentId.toString() == Intent.UNKNOWN_INTENT_NAME
                || newIntentId.toString() == Intent.RAG_EXCLUDED_INTENT_NAME) {
                save(it.copy(classification = it.classification.copy(newIntentId, emptyList())))
            } else {
                save(it.copy(classification = it.classification.copy(newIntentId)))
            }
        }
    }

    override fun switchSentencesEntity(
        allowedNamespace: String,
        sentences: List<ClassifiedSentence>,
        oldEntity: EntityDefinition,
        newEntity: EntityDefinition
    ) {
        // TODO not only first entity level

        val oldEntityType = EntityTypeDefinitionMongoDAO.getEntityTypeByName(oldEntity.entityTypeName)
            ?: error("no entity type found: $oldEntity")
        val newEntityType = EntityTypeDefinitionMongoDAO.getEntityTypeByName(newEntity.entityTypeName)
            ?: error("no entity type found: $newEntity")
        val newSubEntityDefinitions = mutableSetOf<EntityDefinition>()

        sentences.forEach { s ->
            val selectedEntities =
                s.classification.entities.filter { e -> e.role == oldEntity.role && e.type == oldEntity.entityTypeName }
            val newEntities =
                s.classification.entities.filterNot { e -> e.role == oldEntity.role && e.type == oldEntity.entityTypeName } +
                        selectedEntities.map { e ->
                            // select already existing roles and change type
                            val subEntitiesWithExistingRole = e.subEntities
                                .filter { subEntity -> newEntityType.subEntities.any { it.role == subEntity.role } }
                                .map { sub ->
                                    sub.copy(type = newEntityType.subEntities.first { it.role == sub.role }.entityTypeName)
                                }

                            val subEntitiesWithNotExistingRole =
                                if (newEntityType.name.namespace() == allowedNamespace) {
                                    // for non existing roles, add the new sub entity to entity
                                    e.subEntities
                                        .filterNot { subEntity -> newEntityType.subEntities.any { it.role == subEntity.role } }
                                        .apply {
                                            forEach { sub ->
                                                oldEntityType.subEntities.find { sub.role == it.role }
                                                    ?.let { newSubEntity ->
                                                        newSubEntityDefinitions.add(newSubEntity)
                                                    }
                                            }
                                        }
                                } else {
                                    emptyList()
                                }
                            val newSubEntities = subEntitiesWithExistingRole + subEntitiesWithNotExistingRole

                            e.copy(
                                type = newEntity.entityTypeName,
                                role = newEntity.role,
                                subEntities = newSubEntities.sorted()
                            )
                        }
            save(
                s.copy(
                    classification = s.classification.copy(
                        entities = newEntities.sorted()
                    )
                )
            )
        }

        if (newSubEntityDefinitions.isNotEmpty()) {
            EntityTypeDefinitionMongoDAO.save(
                newEntityType.copy(
                    subEntities = newEntityType.subEntities + newSubEntityDefinitions
                )
            )
        }
    }

    override fun removeEntityFromSentences(
        applicationId: Id<ApplicationDefinition>,
        intentId: Id<IntentDefinition>,
        entityType: String,
        role: String
    ) {
        col.updateMany(
            and(
                ApplicationId eq applicationId,
                Classification_.intentId eq intentId
            ),
            pullByFilter(Classification_.entities, Role eq role)
        )
    }

    override fun removeSubEntityFromSentences(
        applicationId: Id<ApplicationDefinition>,
        entityType: String,
        role: String
    ) {
        (1..10).forEach { removeSubEntitiesFromSentence(applicationId, entityType, role, it) }
    }

    private fun removeSubEntitiesFromSentence(
        applicationId: Id<ApplicationDefinition>,
        entityType: String,
        role: String,
        level: Int
    ) {
        val baseFilter = "classification.entities" + (2..level).joinToString("") { ".subEntities" }
        val filter = """{
            'applicationId':${applicationId.json},
            '$baseFilter':{
                $elemMatch :{
                    type:${entityType.json},
                    subEntities:{
                        $elemMatch:{
                            'role':${role.json}
                        }
                    }
                }
            }
        }"""
        val update = """{
                    $pull:{
                        '${baseFilter.replace(".subEntities", ".$[].subEntities")}.$.subEntities':{
                            'role':${role.json}
                            }
                        }
                    }"""
        logger.debug { "$filter $update" }
        col.updateMany(filter, update)
    }

    internal fun updateSentenceState(stat: ParseRequestLogStatCol) {
        col.updateOne(
            and(
                Language eq stat.language,
                ApplicationId eq stat.applicationId,
                Text eq stat.text
            ),
            combine(
                listOfNotNull(
                    stat.intentProbability?.let { setValue(LastIntentProbability, it) },
                    stat.entitiesProbability?.let { setValue(LastEntityProbability, it) },
                    setValue(LastUsage, stat.lastUsage),
                    setValue(UsageCount, stat.count)
                )
            )
        )
    }

    override fun incrementUnknownStat(
        applicationId: Id<ApplicationDefinition>,
        language: Locale,
        text: String
    ) {
        col.updateOne(
            and(
                Language eq language,
                ApplicationId eq applicationId,
                Text eq text
            ),
            inc(UnknownCount, 1)
        )
    }
}
