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

import ai.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import ai.tock.nlp.front.service.storage.FaqDefinitionDAO
import ai.tock.nlp.front.service.storage.FaqSettingsDAO
import ai.tock.nlp.front.service.storage.IntentDefinitionDAO
import ai.tock.nlp.front.service.storage.ModelBuildTriggerDAO
import ai.tock.nlp.front.service.storage.NamespaceConfigurationDAO
import ai.tock.nlp.front.service.storage.ParseRequestLogDAO
import ai.tock.nlp.front.service.storage.TestModelDAO
import ai.tock.nlp.front.service.storage.UserActionLogDAO
import ai.tock.nlp.front.service.storage.UserNamespaceDAO
import ai.tock.shared.TOCK_FRONT_DATABASE
import ai.tock.shared.getAsyncDatabase
import ai.tock.shared.getDatabase
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.mongodb.client.MongoDatabase

const val MONGO_DATABASE: String = TOCK_FRONT_DATABASE

val frontMongoModule =
    Kodein.Module {
        bind<MongoDatabase>(MONGO_DATABASE) with provider { getDatabase(MONGO_DATABASE) }
        bind<com.mongodb.reactivestreams.client.MongoDatabase>(MONGO_DATABASE) with provider { getAsyncDatabase(MONGO_DATABASE) }
        bind<ApplicationDefinitionDAO>() with provider { ApplicationDefinitionMongoDAO }
        bind<IntentDefinitionDAO>() with provider { IntentDefinitionMongoDAO }
        bind<EntityTypeDefinitionDAO>() with provider { EntityTypeDefinitionMongoDAO }
        bind<ClassifiedSentenceDAO>() with provider { ClassifiedSentenceMongoDAO }
        bind<ModelBuildTriggerDAO>() with provider { ModelBuildTriggerMongoDAO }
        bind<ParseRequestLogDAO>() with provider { ParseRequestLogMongoDAO }
        bind<TestModelDAO>() with provider { TestModelMongoDAO }
        bind<UserActionLogDAO>() with provider { UserActionLogMongoDAO }
        bind<UserNamespaceDAO>() with provider { UserNamespaceMongoDAO }
        bind<FaqDefinitionDAO>() with provider { FaqDefinitionMongoDAO }
        bind<FaqSettingsDAO>() with provider { FaqSettingsMongoDAO }
        bind<NamespaceConfigurationDAO>() with provider { NamespaceConfigurationMongoDAO }
    }
