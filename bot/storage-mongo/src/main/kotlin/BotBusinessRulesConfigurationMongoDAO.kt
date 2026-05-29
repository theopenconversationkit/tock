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

import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfiguration
import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfigurationDAO
import ai.tock.bot.mongo.MongoBotConfiguration.asyncDatabase
import ai.tock.bot.mongo.MongoBotConfiguration.database
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.watch
import org.litote.kmongo.Id
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.reactivestreams.getCollectionOfName
import org.litote.kmongo.save

internal object BotBusinessRulesConfigurationMongoDAO : BotBusinessRulesConfigurationDAO {
    private const val COLLECTION_NAME = "bot_business_rules_configuration"
    internal val col = database.getCollection<BotBusinessRulesConfiguration>(COLLECTION_NAME)
    private val asyncCol = asyncDatabase.getCollectionOfName<BotBusinessRulesConfiguration>(COLLECTION_NAME)

    init {
        col.ensureUniqueIndex(BotBusinessRulesConfiguration::namespace, BotBusinessRulesConfiguration::botId)
    }

    override fun listenChanges(listener: () -> Unit) {
        asyncCol.watch { listener() }
    }

    override fun findByNamespaceAndBotId(
        namespace: String,
        botId: String,
    ): BotBusinessRulesConfiguration? {
        return col.findOne(
            BotBusinessRulesConfiguration::namespace eq namespace,
            BotBusinessRulesConfiguration::botId eq botId,
        )
    }

    override fun save(conf: BotBusinessRulesConfiguration): BotBusinessRulesConfiguration {
        col.save(conf)
        return conf
    }

    override fun delete(id: Id<BotBusinessRulesConfiguration>) {
        col.deleteOneById(id)
    }
}
