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

package fr.vsct.tock.nlp.front.storage.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.UpdateOptions
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.Classification
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.inbox
import fr.vsct.tock.nlp.front.shared.config.EntityDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.nlp.front.shared.config.SentencesQueryResult
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.MongoOperator.`in`
import org.litote.kmongo.MongoOperator.elemMatch
import org.litote.kmongo.MongoOperator.gt
import org.litote.kmongo.MongoOperator.lte
import org.litote.kmongo.MongoOperator.ne
import org.litote.kmongo.MongoOperator.pull
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.count
import org.litote.kmongo.deleteMany
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.find
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.replaceOne
import org.litote.kmongo.updateMany
import java.time.Instant
import java.util.Locale

/**
 *
 */
object ClassifiedSentenceMongoDAO : ClassifiedSentenceDAO {

    private val logger = KotlinLogging.logger {}

    private data class ClassifiedSentenceCol(
        val text: String,
        val fullText: String = text,
        val language: Locale,
        val applicationId: Id<ApplicationDefinition>,
        val creationDate: Instant,
        val updateDate: Instant,
        val status: ClassifiedSentenceStatus,
        val classification: Classification,
        val lastIntentProbability: Double? = null,
        val lastEntityProbability: Double? = null
    ) {

        constructor(sentence: ClassifiedSentence) :
                this(
                    textKey(sentence.text),
                    sentence.text,
                    sentence.language,
                    sentence.applicationId,
                    sentence.creationDate,
                    sentence.updateDate,
                    sentence.status,
                    sentence.classification,
                    sentence.lastIntentProbability,
                    sentence.lastEntityProbability
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
                lastEntityProbability
            )
    }

    private val col: MongoCollection<ClassifiedSentenceCol> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<ClassifiedSentenceCol>("classified_sentence")
        c.ensureIndex("{'text':1,'language':1,'applicationId':1}", IndexOptions().unique(true))
        c.ensureIndex("{'language':1,'applicationId':1,'status':1}")
        c.ensureIndex("{'status':1}")
        c.ensureIndex("{'updateDate':1}")
        c.ensureIndex("{'language':1, 'status':1, 'classification.intentId':1}")
        c
    }

    override fun getSentences(
        intents: Set<Id<IntentDefinition>>?,
        language: Locale?,
        status: ClassifiedSentenceStatus?
    ): List<ClassifiedSentence> {
        if (intents == null && language == null && status == null) {
            error("at least one parameter should be not null")
        }
        val query = listOfNotNull(
            if (intents != null) "'classification.intentId':{\$in:${intents.json}}" else null,
            if (language != null) "'language':${language.json}" else null,
            if (status != null) "'status':${status.json}" else null
        ).joinToString(prefix = "{", postfix = "}")

        return col.find(query).toList().map { it.toSentence() }
    }

    override fun switchSentencesStatus(sentences: List<ClassifiedSentence>, newStatus: ClassifiedSentenceStatus) {
        sentences.forEach {
            save(it.copy(status = newStatus))
        }
    }

    override fun deleteSentencesByStatus(status: ClassifiedSentenceStatus) {
        col.deleteMany("{'status':${status.json}}")
    }

    override fun deleteSentencesByApplicationId(applicationId: Id<ApplicationDefinition>) {
        col.deleteMany("{'applicationId':${applicationId.json}}")
    }

    override fun save(sentence: ClassifiedSentence) {
        col.replaceOne(
            "{'text':${textKey(sentence.text).json},'language':${sentence.language.json},'applicationId':${sentence.applicationId.json}}",
            ClassifiedSentenceCol(sentence),
            UpdateOptions().upsert(true)
        )
    }

    override fun search(query: SentencesQuery): SentencesQueryResult {
        with(query) {
            val filterStatus = listOfNotNull(
                if (status.isEmpty()) null else status.joinToString(",", "$`in`:[", "]") { "'$it'" },
                if (status.isNotEmpty() || notStatus == null) null else "$ne:${notStatus!!.json}"
            ).joinToString(",", "status:{", "}")

            val filterBase =
                listOf(
                    "'applicationId':${applicationId.json}",
                    if (language == null) null else "'language':${language!!.json}",
                    if (search.isNullOrBlank()) null else if (query.onlyExactMatch) "'text':${search!!.json}" else "'fullText':/${search!!.trim()}/i",
                    if (intentId == null) null else "'classification.intentId':${intentId!!.json}",
                    if (filterStatus.isEmpty()) null else filterStatus,
                    if (entityType == null) null else "'classification.entities.type':${entityType!!.json}",
                    if (entityRole == null) null else "'classification.entities.role':${entityRole!!.json}",
                    if (modifiedAfter == null)
                        if (searchMark == null) null else "updateDate:{$lte: ${searchMark!!.date.json}}"
                    else if (searchMark == null) "updateDate:{$gt: ${modifiedAfter!!.json}}"
                    else "updateDate:{$lte: ${searchMark!!.date.json}, $gt: ${modifiedAfter!!.json}}"
                ).toBsonFilter()

            logger.debug { filterBase }
            val count = col.count(filterBase)
            logger.debug { "count : $count" }
            if (count > start) {
                val list = col
                    .find(filterBase)
                    .sort(Sorts.descending("updateDate"))
                    .skip(start.toInt())
                    .limit(size)

                return SentencesQueryResult(count, list.map { it.toSentence() }.toList())
            } else {
                return SentencesQueryResult(0, emptyList())
            }
        }
    }

    override fun switchSentencesIntent(
        applicationId: Id<ApplicationDefinition>,
        oldIntentId: Id<IntentDefinition>,
        newIntentId: Id<IntentDefinition>
    ) {
        col.updateMany(
            "{'applicationId':${applicationId.json}, 'classification.intentId':${oldIntentId.json}}",
            "{$set: {'classification.intentId':${newIntentId.json},'classification.entities':[],'status':'${inbox}'}}"
        )
    }

    override fun switchSentencesIntent(sentences: List<ClassifiedSentence>, newIntentId: Id<IntentDefinition>) {
        //TODO updateMany
        sentences.forEach {
            if (newIntentId.toString() == Intent.UNKNOWN_INTENT) {
                save(it.copy(classification = it.classification.copy(newIntentId, emptyList())))
            } else {
                save(it.copy(classification = it.classification.copy(newIntentId)))
            }
        }
    }

    override fun switchSentencesEntity(
        sentences: List<ClassifiedSentence>,
        oldEntity: EntityDefinition,
        newEntity: EntityDefinition
    ) {
        //TODO updateMany
        sentences.forEach {
            val selectedEntities =
                it.classification.entities.filter { it.role == oldEntity.role && it.type == oldEntity.entityTypeName }
            save(
                it.copy(
                    classification = it.classification.copy(
                        entities = it.classification.entities.filterNot { it.role == oldEntity.role && it.type == oldEntity.entityTypeName }
                                + selectedEntities.map {
                            it.copy(
                                type = newEntity.entityTypeName,
                                role = newEntity.role
                            )
                        }
                    )
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
            "{'applicationId':${applicationId.json}, 'classification.intentId':${intentId.json}, 'classification.entities':{$elemMatch:{type:${entityType.json},'role':${role.json}}}}",
            "{$pull:{'classification.entities':{'role':${role.json}}}}"
        )
    }

    override fun removeSubEntityFromSentences(
        applicationId: Id<ApplicationDefinition>,
        entityType: String,
        role: String
    ) {
        //TODO use 10 levels when this is resolved:  https:jira.mongodb.org/browse/SERVER-831
        (1..1).forEach { removeSubEntitiesFromSentence(applicationId, entityType, role, it) }
    }

    private fun removeSubEntitiesFromSentence(
        applicationId: Id<ApplicationDefinition>,
        entityType: String,
        role: String,
        level: Int
    ) {
        val baseFilter = "classification.entities" + (2..level).joinToString("") { ".subEntities" }
        col.updateMany(
            """{
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
        }""",
            """{
                    $pull:{
                        '${baseFilter.replace(".subEntities", ".$.subEntities")}.$.subEntities':{
                            'role':${role.json}
                            }
                        }
                    }"""
        )
    }
}