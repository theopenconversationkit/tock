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

package fr.vsct.tock.bot.admin.bot

import org.litote.kmongo.Id

/**
 *
 */
interface BotApplicationConfigurationDAO {

    /**
     * Listen changes on application configurations.
     */
    fun listenChanges(listener: () -> Unit)

    fun save(conf: BotApplicationConfiguration): BotApplicationConfiguration

    fun updateIfNotManuallyModified(conf: BotApplicationConfiguration): BotApplicationConfiguration

    fun getConfigurationsByNamespaceAndNlpModel(namespace: String, nlpModel: String): List<BotApplicationConfiguration>

    fun getConfigurations(): List<BotApplicationConfiguration>

    fun getConfigurationById(id: Id<BotApplicationConfiguration>): BotApplicationConfiguration?

    fun getConfigurationByApplicationIdAndBotId(applicationId: String, botId: String): BotApplicationConfiguration?

    fun getConfigurationsByBotId(botId: String): List<BotApplicationConfiguration>

    fun delete(conf: BotApplicationConfiguration)
}