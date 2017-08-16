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

import botModule
import com.github.salomonbrys.kodein.Kodein
import fr.vsct.tock.bot.admin.user.UserReportQuery
import fr.vsct.tock.bot.mongo.UserTimelineMongoDAO
import fr.vsct.tock.bot.mongo.botMongoModule
import fr.vsct.tock.shared.defaultNamespace
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.sharedModule
import org.junit.Test
import java.util.Locale

/**
 *
 */
class UserTimelineMongoDAOIntegrationTest {

    init {
        injector.inject(Kodein {
            listOf(sharedModule, botModule, botMongoModule).forEach { import(it) }
        })
    }

    @Test
    fun testSearch() {
        println(UserTimelineMongoDAO.search(
                UserReportQuery(
                        defaultNamespace,
                        "bot_open_data",
                        Locale.FRENCH,
                        flags = mapOf("tock_profile_loaded" to "true")
                )
        ))
    }
}