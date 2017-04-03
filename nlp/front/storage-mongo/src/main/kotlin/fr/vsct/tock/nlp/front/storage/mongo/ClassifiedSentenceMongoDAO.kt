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
import fr.vsct.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.inbox
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.nlp.front.shared.config.SentencesQueryResult
import org.litote.kmongo.MongoOperator.`in`
import org.litote.kmongo.MongoOperator.elemMatch
import org.litote.kmongo.MongoOperator.ne
import org.litote.kmongo.MongoOperator.pull
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.count
import org.litote.kmongo.createIndex
import org.litote.kmongo.deleteMany
import org.litote.kmongo.find
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.replaceOne
import org.litote.kmongo.updateMany
import java.util.Locale

/**
 *
 */
object ClassifiedSentenceMongoDAO : ClassifiedSentenceDAO {

    private val col: MongoCollection<ClassifiedSentence> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<ClassifiedSentence>()
        c.createIndex("{'text':1,'language':1,'applicationId':1}", IndexOptions().unique(true))
        c.createIndex("{'language':1,'applicationId':1,'status':1}")
        c.createIndex("{'status':1}")
        c.createIndex("{'language':1, 'status':1, 'classification.intentId':1}")
        c
    }

    override fun getSentences(intents: Set<String>, language: Locale, status: ClassifiedSentenceStatus): List<ClassifiedSentence> {
        return col.find("{'language':${language.json},'status':${status.json},'classification.intentId':{\$in:${intents.json}}}").toList()
    }

    override fun getSentences(status: ClassifiedSentenceStatus): List<ClassifiedSentence> {
        return col.find("{'status':${status.json}}").toList()
    }

    override fun switchStatus(sentences: List<ClassifiedSentence>, newStatus: ClassifiedSentenceStatus) {
        sentences.forEach {
            save(it.copy(status = newStatus))
        }
    }

    override fun deleteSentencesByStatus(status: ClassifiedSentenceStatus) {
        col.deleteMany("{'status':${status.json}}")
    }

    override fun save(sentence: ClassifiedSentence) {
        col.replaceOne("{'text':${sentence.text.json},'language':${sentence.language.json},'applicationId':${sentence.applicationId.json}}", sentence, UpdateOptions().upsert(true))
    }

    override fun search(query: SentencesQuery): SentencesQueryResult {
        with(query) {
            val filterStatus = listOfNotNull(
                    if (status.isEmpty()) null else status.map { "'$it'" }.joinToString(",", "${`in`}:[", "]"),
                    if (status.isNotEmpty() || notStatus == null) null else "${ne}:${notStatus!!.json}"
            ).joinToString(",", "status:{", "}")

            val filter =
                    listOfNotNull(
                            "'applicationId':${applicationId.json}",
                            "'language':${language.json}",
                            if (search.isNullOrBlank()) null else if (query.onlyExactMatch) "'text':${search!!.json}" else "'text':/${search!!.trim()}/i",
                            if (intentId.isNullOrBlank()) null else "'classification.intentId':${intentId!!.json}",
                            if (filterStatus.isEmpty()) null else filterStatus
                    ).joinToString(",", "{", "}")
            val count = col.count(filter)
            if (count > start) {
                val list = col.find(filter)
                        .skip(start.toInt()).limit(size).sort(Sorts.descending("_id")).toList()
                return SentencesQueryResult(count, list)
            } else {
                return SentencesQueryResult(0, emptyList())
            }
        }

    }

    override fun switchIntent(applicationId: String, oldIntentId: String, newIntentId: String) {
        col.updateMany("{'applicationId':${applicationId.json}, 'classification.intentId':${oldIntentId.json}}", "{${set}: {'classification.intentId':${newIntentId.json},'classification.entities':[],'status':'${inbox}'}}")
    }

    override fun removeEntity(applicationId: String, intentId: String, entityType: String, role: String) {
        col.updateMany("{'applicationId':${applicationId.json}, 'classification.intentId':${intentId.json}, 'classification.entities':{${elemMatch}:{'role':${role.json}}}}", "{${pull}:{'classification.entities':{'role':${role.json}}}}")
    }
}