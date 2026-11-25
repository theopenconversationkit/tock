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

package ai.tock.bot.admin.test

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.message.Message
import ai.tock.shared.defaultLocale
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale

/**
 * A test plan is a set of dialogs to replay.
 */
data class TestPlan(
    /**
     * The dialogs of the test.
     */
    val dialogs: List<TestDialogReport>,
    /**
     * The name of the test.
     */
    val name: String,
    /**
     * The tested application identifier.
     */
    val applicationId: String,
    /**
     * The namespace of the nlp model.
     */
    val namespace: String,
    /**
     * The name of the nlp model.
     */
    val nlpModel: String,
    /**
     * The bot configuration id.
     */
    val botApplicationConfigurationId: Id<BotApplicationConfiguration>,
    /**
     * The locale of the test.
     */
    val locale: Locale = defaultLocale,
    /**
     * The optional action to play, before starting the test.
     */
    val startAction: Message? = null,
    /**
     * The [ConnectorType] tested.
     */
    val targetConnectorType: ConnectorType = ConnectorType.none,
    /**
     * The identifier of the test plan.
     */
    val _id: Id<TestPlan> = newId(),
)
