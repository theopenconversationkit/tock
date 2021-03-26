/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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
package ai.tock.bot.connector.web.channel

import ai.tock.shared.TOCK_BOT_DATABASE
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.watch
import com.github.salomonbrys.kodein.instance
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.CreateCollectionOptions
import mu.KotlinLogging
import org.litote.kmongo.getCollection
import org.litote.kmongo.reactivestreams.getCollectionOfName
import org.litote.kmongo.save

internal object ChannelMongoDAO : ChannelDAO {
    private val collectionName = "web_channel_event"
    private val asyncDatabase: com.mongodb.reactivestreams.client.MongoDatabase by injector.instance(
        TOCK_BOT_DATABASE
    )
    private val database: MongoDatabase by injector.instance(
        TOCK_BOT_DATABASE
    )
    private val logger = KotlinLogging.logger {}
    private val asyncWebChannelResponseCol: com.mongodb.reactivestreams.client.MongoCollection<ChannelEvent>
    private val webChannelResponseCol: MongoCollection<ChannelEvent>

    private fun MongoDatabase.collectionExists(collectionName: String): Boolean =
        listCollectionNames().contains(collectionName)

    init {
        if (!database.collectionExists(collectionName)) {
            try {
                database
                    .createCollection(
                        collectionName,
                        CreateCollectionOptions()
                            .capped(true)
                            .sizeInBytes(100000000)
                            .maxDocuments(50000)
                    )
            } catch (ex: Exception) {
                logger.error(ex)
            }
        }
        asyncWebChannelResponseCol = asyncDatabase.getCollectionOfName(collectionName)
        webChannelResponseCol = database.getCollection<ChannelEvent>(collectionName)
    }

    override fun listenChanges(listener: (channelEvent: ChannelEvent) -> Unit) {
        asyncWebChannelResponseCol.watch {
            val channelEvent = it.fullDocument
            if (channelEvent != null)
                listener(channelEvent)
        }
    }

    override fun save(channelEvent: ChannelEvent) {
        webChannelResponseCol.save(channelEvent)
    }
}
