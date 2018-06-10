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
import fr.vsct.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition_.Companion.Name
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition_.Companion.PredefinedValues
import fr.vsct.tock.nlp.front.storage.mongo.MongoFrontConfiguration.database
import org.litote.kmongo.bson
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.pullByFilter
import org.litote.kmongo.replaceOneWithFilter
import org.litote.kmongo.updateOne
import java.util.Locale

/**
 *
 */
object EntityTypeDefinitionMongoDAO : EntityTypeDefinitionDAO {

    private val col: MongoCollection<EntityTypeDefinition> by lazy {
        val c = database.getCollection<EntityTypeDefinition>()
        c.ensureUniqueIndex(Name)
        c
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
        //TODO support of non @Data annotated collections
        col.updateOne(Name eq entityTypeName, pullByFilter(PredefinedValues, "{value:${predefinedValue.json}}".bson))
    }

    override fun deletePredefinedValueSynonymByName(
        entityTypeName: String,
        predefinedValue: String,
        locale: Locale,
        synonym: String
    ) {
        //TODO kmongo map & positional projection
        col.updateOne(
            "{name:${entityTypeName.json}, predefinedValues:{\$exists:true}}",
            "{\$pull:{'predefinedValues.\$.synonyms.${locale.toLanguageTag()}':${synonym.json}}}"
        )
    }
}