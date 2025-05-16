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

package ai.tock.bot.mongo

import ai.tock.bot.admin.annotation.BotAnnotation
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.test.TestPlan
import ai.tock.bot.admin.test.TestPlanExecution
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.user.PlayerId
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelStat
import org.litote.jackson.data.JacksonDataRegistry
import org.litote.kmongo.DataRegistry

/**
 *
 */
@DataRegistry(
    [
        I18nLabel::class,
        I18nLabelStat::class,
        BotApplicationConfiguration::class,
        BotConfiguration::class,
        StoryDefinitionConfiguration::class,
        TestPlan::class,
        TestPlanExecution::class,
        Intent::class,
        IntentWithoutNamespace::class,
        PlayerId::class,
        EventState::class,
        ConnectorType::class,
        ActionMetadata::class,
        BotAnnotation::class
    ]
)
@JacksonDataRegistry(
    [
        I18nLabel::class,
        I18nLabelStat::class,
        BotApplicationConfiguration::class,
        BotConfiguration::class,
        StoryDefinitionConfiguration::class,
        TestPlan::class,
        TestPlanExecution::class,
        Intent::class,
        IntentWithoutNamespace::class,
        PlayerId::class,
        ConnectorType::class
    ]
)
internal object BotDataRegistry
