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

package fr.vsct.tock.bot.admin.test

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.shared.defaultLocale
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale

/**
 *
 */
data class TestPlan(
        val dialogs: List<TestDialogReport>,
        val name: String,
        val applicationId: String,
        val namespace: String,
        val nlpModel: String,
        val botApplicationConfigurationId: Id<BotApplicationConfiguration>,
        val locale: Locale = defaultLocale,
        val startAction: Message? = null,
        val targetConnectorType: ConnectorType = ConnectorType.none,
        val _id: Id<TestPlan> = newId()
) {
}