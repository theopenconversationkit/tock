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

package fr.vsct.tock.bot.mongo

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.mongodb.client.MongoDatabase
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfigurationDAO
import fr.vsct.tock.bot.admin.dialog.DialogReportDAO
import fr.vsct.tock.bot.admin.test.TestPlanDAO
import fr.vsct.tock.bot.admin.user.UserReportDAO
import fr.vsct.tock.bot.engine.feature.FeatureDAO
import fr.vsct.tock.bot.engine.user.UserLock
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.getAsyncDatabase
import fr.vsct.tock.shared.getDatabase
import fr.vsct.tock.translator.I18nDAO

internal const val MONGO_DATABASE: String = "tock_bot_mongo_db"

val botMongoModule = Kodein.Module {
    bind<MongoDatabase>(MONGO_DATABASE) with provider { getDatabase(MONGO_DATABASE) }
    bind<com.mongodb.async.client.MongoDatabase>(MONGO_DATABASE) with provider { getAsyncDatabase(MONGO_DATABASE) }
    bind<BotApplicationConfigurationDAO>() with provider { BotApplicationConfigurationMongoDAO }
    bind<StoryDefinitionConfigurationDAO>() with provider { StoryDefinitionConfigurationMongoDAO }
    bind<I18nDAO>() with provider { I18nMongoDAO }
    bind<UserTimelineDAO>() with provider { UserTimelineMongoDAO }
    bind<UserReportDAO>() with provider { UserTimelineMongoDAO }
    bind<DialogReportDAO>() with provider { UserTimelineMongoDAO }
    bind<TestPlanDAO>() with provider { TestPlanMongoDAO }
    bind<UserLock>() with provider { MongoUserLock }
    bind<FeatureDAO>() with provider { FeatureMongoDAO }
}
