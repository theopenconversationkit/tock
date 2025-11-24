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

import ai.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import ai.tock.shared.getAsyncDatabase
import ai.tock.shared.getDatabase
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.mongodb.client.MongoDatabase
import org.junit.jupiter.api.BeforeEach

/**
 *
 */
abstract class AbstractTest {
    @Suppress("ktlint:standard:property-naming")
    protected val TOCK_DOCUMENT_DB_ON_PROPERTY: String = "tock_document_db_on"

    @BeforeEach
    fun before() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein {
                import(sharedTestModule)
                bind<MongoDatabase>(MONGO_DATABASE) with provider { getDatabase(MONGO_DATABASE) }
                bind<com.mongodb.reactivestreams.client.MongoDatabase>(MONGO_DATABASE) with
                    provider {
                        getAsyncDatabase(
                            MONGO_DATABASE,
                        )
                    }
                bind<EntityTypeDefinitionDAO>() with provider { EntityTypeDefinitionMongoDAO }
                import(moreBindingModules())
            },
        )
    }

    /**
     * Add binding modules
     * is useful when overriden
     * per default no module initiated
     */
    protected open fun moreBindingModules(): Kodein.Module = Kodein.Module {}
}
