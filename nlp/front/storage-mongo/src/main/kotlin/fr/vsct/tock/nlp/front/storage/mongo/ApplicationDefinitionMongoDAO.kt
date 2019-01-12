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
import fr.vsct.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition_.Companion.Name
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition_.Companion.Namespace
import fr.vsct.tock.nlp.front.storage.mongo.MongoFrontConfiguration.asyncDatabase
import fr.vsct.tock.nlp.front.storage.mongo.MongoFrontConfiguration.database
import org.litote.kmongo.Id
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.reactivestreams.watchIndefinitely
import org.litote.kmongo.save

/**
 *
 */
internal object ApplicationDefinitionMongoDAO : ApplicationDefinitionDAO {

    private val col: MongoCollection<ApplicationDefinition> by lazy {
        val c = database.getCollection<ApplicationDefinition>()
        c.ensureUniqueIndex(Name, Namespace)
        c
    }
    private val asyncCol by lazy {
        asyncDatabase.getCollection<ApplicationDefinition>()
    }

    override fun listenApplicationDefinitionChanges(listener: () -> Unit) {
        asyncCol.watchIndefinitely { listener() }
    }

    override fun deleteApplicationById(id: Id<ApplicationDefinition>) {
        col.deleteOneById(id)
    }

    override fun save(application: ApplicationDefinition): ApplicationDefinition {
        col.save(application)
        return application
    }

    override fun getApplicationByNamespaceAndName(namespace: String, name: String): ApplicationDefinition? {
        return col.findOne(Name eq name, Namespace eq namespace)
    }

    override fun getApplicationById(id: Id<ApplicationDefinition>): ApplicationDefinition? {
        return col.findOneById(id)
    }

    override fun getApplications(): List<ApplicationDefinition> {
        return col.find().toList()
    }
}