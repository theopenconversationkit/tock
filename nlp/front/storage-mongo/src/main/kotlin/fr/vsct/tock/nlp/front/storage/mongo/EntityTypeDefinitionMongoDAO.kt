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
import com.mongodb.client.model.ReplaceOptions
import fr.vsct.tock.nlp.core.PredefinedValue_.Companion.Value
import fr.vsct.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition_.Companion.Name
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition_.Companion.PredefinedValues
import fr.vsct.tock.nlp.front.storage.mongo.MongoFrontConfiguration.asyncDatabase
import fr.vsct.tock.nlp.front.storage.mongo.MongoFrontConfiguration.database
import fr.vsct.tock.shared.watch
import org.litote.kmongo.and
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.pull
import org.litote.kmongo.pullByFilter
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.replaceOneWithFilter
import java.util.Locale

/**
 *
 */
internal object EntityTypeDefinitionMongoDAO : EntityTypeDefinitionDAO {

    private val col: MongoCollection<EntityTypeDefinition> by lazy {
        val c = database.getCollection<EntityTypeDefinition>()
        c.ensureUniqueIndex(Name)
        c
    }
    private val asyncCol by lazy {
        asyncDatabase.getCollection<EntityTypeDefinition>()
    }

    override fun listenEntityTypeChanges(listener: () -> Unit) {
        asyncCol.watch { listener() }
    }

    override fun save(entityType: EntityTypeDefinition) {
        col.replaceOneWithFilter(Name eq entityType.name, entityType, ReplaceOptions().upsert(true))
    }

    override fun getEntityTypeByName(name: String): EntityTypeDefinition? {
        return col.findOne(Name eq name)
    }

    override fun getEntityTypes(): List<EntityTypeDefinition> {
        return col.find().toList()
    }

    override fun deleteEntityTypeByName(name: String): Boolean {
        return col.deleteOne(Name eq name).deletedCount == 1L
    }

    override fun deletePredefinedValueByName(entityTypeName: String, predefinedValue: String) {
        col.updateOne(Name eq entityTypeName, pullByFilter(PredefinedValues, Value eq predefinedValue))
    }

    override fun deletePredefinedValueLabelByName(
        entityTypeName: String,
        predefinedValue: String,
        locale: Locale,
        label: String
    ) {
        col.updateOne(
            and(Name eq entityTypeName, PredefinedValues.value eq predefinedValue),
            pull(PredefinedValues.posOp.labels.keyProjection(locale), label)
        )
    }
}