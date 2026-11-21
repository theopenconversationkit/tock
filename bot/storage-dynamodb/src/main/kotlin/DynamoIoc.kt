/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.storage.dynamodb

import ai.tock.bot.admin.bot.BotApplicationConfigurationRuntimeDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationRuntimeDAO
import ai.tock.bot.engine.feature.FeatureRuntimeDAO
import ai.tock.bot.engine.feature.asRuntimeDAO
import ai.tock.bot.engine.user.UserLock
import ai.tock.bot.engine.user.UserTimelineRuntimeDAO
import ai.tock.translator.I18nRuntimeDAO
import ai.tock.translator.asRuntimeDAO
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider

private fun isInClasspath(className: String): Boolean =
    try {
        Class.forName(className, false, Kodein::class.java.classLoader)
        true
    } catch (_: ClassNotFoundException) {
        false
    }

/**
 * The runtime DAOs and the user lock stored in DynamoDB, for a serverless process (see tock-serverless-runtime).
 * The tables are prefixed by `tock_dynamodb_table_prefix`, created when `tock_dynamodb_create_tables` is true
 * (the default); `tock_dynamodb_endpoint` targets DynamoDB Local.
 *
 * The DAOs of the connectors are bound when the connector is in the classpath (WhatsApp Cloud).
 */
val botDynamoModule =
    Kodein.Module(allowSilentOverride = true) {
        bind<UserTimelineRuntimeDAO>() with provider { UserTimelineDynamoDAO }
        bind<BotApplicationConfigurationRuntimeDAO>() with provider { BotApplicationConfigurationDynamoDAO }
        bind<StoryDefinitionConfigurationRuntimeDAO>() with provider { StoryDefinitionConfigurationDynamoDAO }
        bind<FeatureRuntimeDAO>() with provider { featureRuntimeDAO }
        bind<I18nRuntimeDAO>() with provider { i18nRuntimeDAO }
        bind<UserLock>() with provider { DynamoUserLock }
        if (isInClasspath("ai.tock.bot.connector.whatsapp.cloud.database.repository.PayloadWhatsAppCloudDAO")) {
            import(whatsAppCloudDynamoModule, allowOverride = true)
        }
    }

private val featureRuntimeDAO: FeatureRuntimeDAO by lazy { FeatureDynamoDAO.asRuntimeDAO() }

private val i18nRuntimeDAO: I18nRuntimeDAO by lazy { I18nDynamoDAO.asRuntimeDAO() }
