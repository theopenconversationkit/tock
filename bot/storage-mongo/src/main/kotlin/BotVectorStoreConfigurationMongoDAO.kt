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

import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfiguration
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfigurationDAO
import ai.tock.bot.mongo.MongoBotConfiguration.asyncDatabase
import ai.tock.bot.mongo.MongoBotConfiguration.database
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.watch
import org.litote.kmongo.*
import org.litote.kmongo.reactivestreams.getCollectionOfName

internal object BotVectorStoreConfigurationMongoDAO : BotVectorStoreConfigurationDAO {

    private const val COLLECTION_NAME = "bot_vector_store_configuration"
    internal val col = database.getCollection<BotVectorStoreConfiguration>(COLLECTION_NAME)
    private val asyncCol = asyncDatabase.getCollectionOfName<BotVectorStoreConfiguration>(COLLECTION_NAME)

    init {
        col.ensureUniqueIndex(BotVectorStoreConfiguration::namespace, BotVectorStoreConfiguration::botId)
    }

    override fun listenChanges(listener: () -> Unit) {
        asyncCol.watch { listener() }
    }

    override fun findByNamespaceAndBotId(
        namespace: String,
        botId: String
    ): BotVectorStoreConfiguration? {
        return col.findOne(
            BotVectorStoreConfiguration::namespace eq namespace,
            BotVectorStoreConfiguration::botId eq botId
        )
    }

    override fun findByNamespaceAndBotIdAndEnabled(
        namespace: String,
        botId: String,
        enabled: Boolean
    ): BotVectorStoreConfiguration? {
        return col.findOne(
            BotVectorStoreConfiguration::namespace eq namespace,
            BotVectorStoreConfiguration::botId eq botId,
            BotVectorStoreConfiguration::enabled eq enabled
        )
    }

    override fun save(conf: BotVectorStoreConfiguration): BotVectorStoreConfiguration {
        col.save(conf)
        return conf
    }

    override fun delete(id: Id<BotVectorStoreConfiguration>) {
        col.deleteOneById(id)
    }

}
