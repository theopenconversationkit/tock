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

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.mongodb.client.MongoDatabase
import fr.vsct.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import fr.vsct.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import fr.vsct.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import fr.vsct.tock.nlp.front.service.storage.IntentDefinitionDAO
import fr.vsct.tock.nlp.front.service.storage.ModelBuildTriggerDAO
import fr.vsct.tock.shared.getDatabase

internal val MONGO_DATABASE: String = "tock_front_mongo_db"

val frontMongoModule = Kodein.Module {
    bind<MongoDatabase>(MONGO_DATABASE) with provider { getDatabase(MONGO_DATABASE) }
    bind<ApplicationDefinitionDAO>() with provider { ApplicationDefinitionMongoDAO }
    bind<IntentDefinitionDAO>() with provider { IntentDefinitionMongoDAO }
    bind<EntityTypeDefinitionDAO>() with provider { EntityTypeDefinitionMongoDAO }
    bind<ClassifiedSentenceDAO>() with provider { ClassifiedSentenceMongoDAO }
    bind<ModelBuildTriggerDAO>() with provider { ModelBuildTriggerMongoDAO }
}