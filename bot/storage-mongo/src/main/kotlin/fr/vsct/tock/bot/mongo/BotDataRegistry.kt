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

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.story.StoryDefinitionConfiguration
import fr.vsct.tock.bot.admin.test.TestPlan
import fr.vsct.tock.bot.admin.test.TestPlanExecution
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.engine.dialog.EventState
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.translator.I18nLabel
import fr.vsct.tock.translator.I18nLabelStat
import org.litote.kmongo.DataRegistry
import org.litote.jackson.data.JacksonDataRegistry

/**
 *
 */
@DataRegistry(
    [
        I18nLabel::class,
        I18nLabelStat::class,
        BotApplicationConfiguration::class,
        StoryDefinitionConfiguration::class,
        TestPlan::class,
        TestPlanExecution::class,
        Intent::class,
        PlayerId::class,
        EventState::class,
        ConnectorType::class
    ]
)
@JacksonDataRegistry(
    [
        I18nLabel::class,
        I18nLabelStat::class,
        BotApplicationConfiguration::class,
        StoryDefinitionConfiguration::class,
        TestPlan::class,
        TestPlanExecution::class,
        Intent::class,
        PlayerId::class
    ]
)
internal object BotDataRegistry