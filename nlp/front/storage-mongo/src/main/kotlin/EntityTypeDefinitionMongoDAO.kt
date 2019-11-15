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

package ai.tock.nlp.front.storage.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import ai.tock.nlp.core.DictionaryData
import ai.tock.nlp.core.DictionaryData_.Companion.EntityName
import ai.tock.nlp.core.DictionaryData_.Companion.Namespace
import ai.tock.nlp.core.DictionaryData_.Companion.Values
import ai.tock.nlp.core.PredefinedValue_.Companion.Value
import ai.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.EntityTypeDefinition_.Companion.Name
import ai.tock.nlp.front.shared.config.EntityTypeDefinition_.Companion.PredefinedValues
import ai.tock.nlp.front.storage.mongo.MongoFrontConfiguration.asyncDatabase
import ai.tock.nlp.front.storage.mongo.MongoFrontConfiguration.database
import ai.tock.shared.error
import ai.tock.shared.name
import ai.tock.shared.namespace
import ai.tock.shared.watch
import mu.KotlinLogging
import org.litote.kmongo.and
import ai.tock.shared.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.exists
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.not
import org.litote.kmongo.pull
import org.litote.kmongo.pullByFilter
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.replaceOneWithFilter
import org.litote.kmongo.size
import java.util.Locale

/**
 *
 */
internal object EntityTypeDefinitionMongoDAO : EntityTypeDefinitionDAO {

    private val logger = KotlinLogging.logger {}

    private val col: MongoCollection<EntityTypeDefinition> by lazy {
        val c = database.getCollection<EntityTypeDefinition>()
        c.ensureUniqueIndex(Name)
        c
    }
    private val asyncCol by lazy {
        asyncDatabase.getCollection<EntityTypeDefinition>()
    }
    private val dictionaryCol: MongoCollection<DictionaryData> by lazy {
        val c = database.getCollection<DictionaryData>()
        c.ensureUniqueIndex(Namespace, EntityName)
        c
    }
    private val asyncDictionaryCol by lazy {
        asyncDatabase.getCollection<DictionaryData>()
    }

    override fun listenEntityTypeChanges(listener: () -> Unit) {
        asyncCol.watch { listener() }
    }

    init {
        //TODO remove this in 20.3
        try {
            col.find(and(PredefinedValues.exists(), not(PredefinedValues.size(0))))
                .forEach {
                    save(DictionaryData(it.name.namespace(), it.name.name(), it.predefinedValues))
                    save(it.copy(predefinedValues = emptyList(), dictionary = true))
                }
        } catch (t: Throwable) {
            logger.error(t)
        }
    }

    override fun save(entityType: EntityTypeDefinition) {
        col.replaceOneWithFilter(Name eq entityType.name, entityType, ReplaceOptions().upsert(true))
    }

    override fun save(data: DictionaryData) {
        dictionaryCol.replaceOneWithFilter(
            and(Namespace eq data.namespace, EntityName eq data.entityName),
            data,
            ReplaceOptions().upsert(true)
        )
    }

    override fun getAllDictionaryData(): List<DictionaryData> = dictionaryCol.find().toList()

    override fun getDictionaryDataByEntityName(qualifiedName: String): DictionaryData? =
        dictionaryCol.findOne(Namespace eq qualifiedName.namespace(), EntityName eq qualifiedName.name())

    override fun getDictionaryDataByNamespace(namespace: String): List<DictionaryData> =
        dictionaryCol.find(Namespace eq namespace).toList()

    override fun listenDictionaryDataChanges(listener: () -> Unit) {
        asyncDictionaryCol.watch { listener() }
    }

    override fun getEntityTypeByName(name: String): EntityTypeDefinition? {
        return col.findOne(Name eq name)
    }

    override fun getEntityTypes(): List<EntityTypeDefinition> {
        return col.find().toList()
    }

    override fun deleteEntityTypeByName(name: String): Boolean {
        dictionaryCol.deleteOne(and(Namespace eq name.namespace(), EntityName eq name.name()))
        return col.deleteOne(Name eq name).deletedCount == 1L
    }

    override fun deletePredefinedValueByName(entityTypeName: String, predefinedValue: String) {
        dictionaryCol.updateOne(
            and(
                Namespace eq entityTypeName.namespace(),
                EntityName eq entityTypeName.name()
            ),
            pullByFilter(Values, Value eq predefinedValue))
    }

    override fun deletePredefinedValueLabelByName(
        entityTypeName: String,
        predefinedValue: String,
        locale: Locale,
        label: String
    ) {
        dictionaryCol.updateOne(
            and(
                Namespace eq entityTypeName.namespace(),
                EntityName eq entityTypeName.name(),
                Values.value eq predefinedValue
            ),
            pull(Values.posOp.labels.keyProjection(locale), label)
        )
    }
}