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
import fr.vsct.tock.nlp.front.service.storage.IntentDefinitionDAO
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition_.Companion.Applications
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition_.Companion.Entities
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition_.Companion.Name
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition_.Companion.Namespace
import fr.vsct.tock.nlp.front.storage.mongo.MongoFrontConfiguration.asyncDatabase
import fr.vsct.tock.nlp.front.storage.mongo.MongoFrontConfiguration.database
import fr.vsct.tock.shared.watchSafely
import org.litote.kmongo.Id
import org.litote.kmongo.async.getCollection
import org.litote.kmongo.contains
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.save

/**
 *
 */
internal object IntentDefinitionMongoDAO : IntentDefinitionDAO {

    private val col: MongoCollection<IntentDefinition> by lazy {
        val c = database.getCollection<IntentDefinition>()
        c.ensureIndex(Applications)
        c.ensureUniqueIndex(Namespace, Name)
        c
    }

    private val asyncCol by lazy {
        asyncDatabase.getCollection<IntentDefinition>()
    }

    override fun listenIntentDefinitionChanges(listener: () -> Unit) {
        asyncCol.watchSafely { listener() }
    }

    override fun getIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition> {
        return col.find(Applications contains applicationId).toList()
    }

    override fun getIntentByNamespaceAndName(namespace: String, name: String): IntentDefinition? {
        return col.findOne(Name eq name, Namespace eq namespace)
    }

    override fun getIntentById(id: Id<IntentDefinition>): IntentDefinition? {
        return col.findOneById(id)
    }

    override fun save(intent: IntentDefinition) {
        col.save(intent)
    }

    override fun deleteIntentById(id: Id<IntentDefinition>) {
        col.deleteOneById(id)
    }

    override fun getIntentsUsingEntity(entityType: String): List<IntentDefinition> {
        return col.find(Entities.entityTypeName eq entityType).toList()
    }
}