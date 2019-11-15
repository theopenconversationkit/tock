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

package ai.tock.bot.mongo

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.mongodb.client.MongoDatabase
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.shared.getAsyncDatabase
import ai.tock.shared.getDatabase
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import org.junit.jupiter.api.BeforeEach
import org.litote.kmongo.deleteMany

/**
 *
 */
abstract class AbstractTest(val initDb: Boolean = true) {

    companion object {

        init {
            System.setProperty("tock_bot_encrypted_flags", "test1,test2")
            System.setProperty("tock_encrypt_pass", "dev")
        }
    }

    @BeforeEach
    fun before() {
        if (initDb) {
            tockInternalInjector = KodeinInjector()
            tockInternalInjector.inject(Kodein {
                import(sharedTestModule)
                bind<MongoDatabase>(MONGO_DATABASE) with provider { getDatabase(MONGO_DATABASE) }
                bind<com.mongodb.reactivestreams.client.MongoDatabase>(MONGO_DATABASE) with provider {
                    getAsyncDatabase(
                        MONGO_DATABASE
                    )
                }
                bind<BotApplicationConfigurationDAO>() with provider { BotApplicationConfigurationMongoDAO }
                bind<FeatureDAO>() with provider { FeatureMongoDAO }
            }
            )
            UserTimelineMongoDAO.dialogCol.deleteMany()
            UserTimelineMongoDAO.userTimelineCol.deleteMany()
        } else {
            tockInternalInjector = KodeinInjector()
            tockInternalInjector.inject(Kodein {
                import(sharedTestModule)
            })
        }
    }

}