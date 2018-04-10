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

package fr.vsct.tock.bot.engine.user

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.mongodb.client.MongoDatabase
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.mongo.BotApplicationConfigurationMongoDAO
import fr.vsct.tock.bot.mongo.MONGO_DATABASE
import fr.vsct.tock.bot.mongo.UserTimelineCol
import fr.vsct.tock.shared.sharedTestModule
import fr.vsct.tock.shared.tockInternalInjector
import org.junit.jupiter.api.BeforeEach
import org.litote.kmongo.KFlapdoodleRule.Companion.rule

/**
 *
 */
abstract class AbstractTest {

    companion object {

        init {
            System.setProperty("tock_bot_encrypted_flags", "test1,test2")
            System.setProperty("tock_encrypt_pass", "dev")
        }
    }

    @BeforeEach
    fun before() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(Kodein {
            import(sharedTestModule)
            val rule = rule<UserTimelineCol>()
            bind<MongoDatabase>(MONGO_DATABASE) with provider { rule.database }
            bind<BotApplicationConfigurationDAO>() with provider { BotApplicationConfigurationMongoDAO }
        }
        )
    }

}