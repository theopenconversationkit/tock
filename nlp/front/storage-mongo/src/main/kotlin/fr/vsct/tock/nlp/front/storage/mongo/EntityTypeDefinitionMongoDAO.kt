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
import com.mongodb.client.model.UpdateOptions
import fr.vsct.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.storage.mongo.MongoFrontConfiguration.database
import org.litote.kmongo.createIndex
import org.litote.kmongo.deleteOne
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.replaceOne

/**
 *
 */
object EntityTypeDefinitionMongoDAO : EntityTypeDefinitionDAO {

    private val col: MongoCollection<EntityTypeDefinition> by lazy {
        val c = database.getCollection<EntityTypeDefinition>()
        c.createIndex("{'name':1}", IndexOptions().unique(true))
        c
    }

    override fun save(entityType: EntityTypeDefinition) {
        col.replaceOne("{'name':${entityType.name.json}}", entityType, UpdateOptions().upsert(true))
    }

    override fun getEntityTypeByName(name: String): EntityTypeDefinition? {
        return col.findOne("{'name':${name.json}}")
    }

    override fun getEntityTypes(): List<EntityTypeDefinition> {
        return col.find().toList()
    }

    override fun deleteEntityTypeByName(name: String) {
        col.deleteOne("{'name':${name.json}}")
    }
}