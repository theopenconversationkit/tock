/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfiguration
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfigurationDAO
import ai.tock.bot.mongo.MongoBotConfiguration.asyncDatabase
import ai.tock.bot.mongo.MongoBotConfiguration.database
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.watch
import org.litote.kmongo.*
import org.litote.kmongo.reactivestreams.getCollectionOfName

internal object BotDocumentCompressorConfigurationMongoDAO : BotDocumentCompressorConfigurationDAO {

    private const val COLLECTION_NAME = "bot_document_compressor_configuration"
    internal val col = database.getCollection<BotDocumentCompressorConfiguration>(COLLECTION_NAME)
    private val asyncCol = asyncDatabase.getCollectionOfName<BotDocumentCompressorConfiguration>(COLLECTION_NAME)

    init {
        col.ensureUniqueIndex(BotDocumentCompressorConfiguration::namespace, BotDocumentCompressorConfiguration::botId)
    }

    override fun listenChanges(listener: () -> Unit) {
        asyncCol.watch { listener() }
    }

    override fun findByNamespaceAndBotId(
        namespace: String,
        botId: String
    ): BotDocumentCompressorConfiguration? {
        return col.findOne(
            BotDocumentCompressorConfiguration::namespace eq namespace,
            BotDocumentCompressorConfiguration::botId eq botId
        )
    }

    override fun findByNamespaceAndBotIdAndEnabled(
        namespace: String,
        botId: String,
        enabled: Boolean
    ): BotDocumentCompressorConfiguration? {
        return col.findOne(
            BotDocumentCompressorConfiguration::namespace eq namespace,
            BotDocumentCompressorConfiguration::botId eq botId,
            BotDocumentCompressorConfiguration::enabled eq enabled
        )
    }

    override fun save(conf: BotDocumentCompressorConfiguration): BotDocumentCompressorConfiguration {
        col.save(conf)
        return conf
    }

    override fun delete(id: Id<BotDocumentCompressorConfiguration>) {
        col.deleteOneById(id)
    }

}
