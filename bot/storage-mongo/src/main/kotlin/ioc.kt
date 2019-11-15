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
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.mongodb.client.MongoDatabase
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.admin.test.TestPlanDAO
import ai.tock.bot.admin.user.UserReportDAO
import ai.tock.bot.engine.dialog.DialogFlowDAO
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.user.UserLock
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.shared.getAsyncDatabase
import ai.tock.shared.getDatabase
import ai.tock.translator.I18nDAO

internal const val MONGO_DATABASE: String = "tock_bot_mongo_db"

val botMongoModule = Kodein.Module {
    bind<MongoDatabase>(MONGO_DATABASE) with provider { getDatabase(MONGO_DATABASE) }
    bind<com.mongodb.reactivestreams.client.MongoDatabase>(MONGO_DATABASE) with provider {
        getAsyncDatabase(
            MONGO_DATABASE
        )
    }
    bind<BotApplicationConfigurationDAO>() with provider { BotApplicationConfigurationMongoDAO }
    bind<StoryDefinitionConfigurationDAO>() with provider { StoryDefinitionConfigurationMongoDAO }
    bind<I18nDAO>() with provider { I18nMongoDAO }
    bind<UserTimelineDAO>() with provider { UserTimelineMongoDAO }
    bind<UserReportDAO>() with provider { UserTimelineMongoDAO }
    bind<DialogReportDAO>() with provider { UserTimelineMongoDAO }
    bind<TestPlanDAO>() with provider { TestPlanMongoDAO }
    bind<UserLock>() with provider { MongoUserLock }
    bind<FeatureDAO>() with provider { FeatureMongoDAO }
    bind<DialogFlowDAO>() with provider { DialogFlowMongoDAO }
}
