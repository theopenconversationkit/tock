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
package ai.tock.bot.connector.web.channel

import com.mongodb.reactivestreams.client.MongoDatabase as ReactiveMongoDatabase
import ai.tock.shared.TOCK_BOT_DATABASE
import ai.tock.shared.ensureIndex
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.watch
import com.github.salomonbrys.kodein.instance
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.CreateCollectionOptions
import mu.KotlinLogging
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.litote.kmongo.reactivestreams.getCollectionOfName
import org.litote.kmongo.save
import org.litote.kmongo.setTo
import org.litote.kmongo.updateOneById

internal object ChannelMongoDAO : ChannelDAO {
    private const val COLLECTION_NAME = "web_channel_event"
    private val asyncDatabase: ReactiveMongoDatabase by injector.instance(
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
        if (!database.collectionExists(COLLECTION_NAME)) {
            try {
                database
                    .createCollection(
                        COLLECTION_NAME,
                        CreateCollectionOptions()
                            .capped(true)
                            .sizeInBytes(100000000)
                            .maxDocuments(50000)
                    )
            } catch (e: Exception) {
                logger.error(e)
            }
        }
        asyncWebChannelResponseCol = asyncDatabase.getCollectionOfName(COLLECTION_NAME)
        webChannelResponseCol = database.getCollection<ChannelEvent>(COLLECTION_NAME)
        try {
            webChannelResponseCol.ensureIndex(ChannelEvent::appId, ChannelEvent::recipientId, ChannelEvent::status)
            // TODO add an index with TTL on ChannelEvent::enqueuedAt once MongoDB supports it (cf. https://jira.mongodb.org/browse/SERVER-77586)
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    override fun listenChanges(listener: ChannelEvent.Handler) {
        asyncWebChannelResponseCol.watch {
            val channelEvent = it.fullDocument
            if (channelEvent != null) {
                process(channelEvent, listener)
            }
        }
    }

    override fun handleMissedEvents(
        appId: String,
        recipientId: String,
        handler: ChannelEvent.Handler
    ) {
        webChannelResponseCol.find(
            and(
                ChannelEvent::appId eq appId,
                ChannelEvent::recipientId eq recipientId,
                ChannelEvent::status eq ChannelEvent.Status.ENQUEUED,
            )
        ).forEach { event ->
            process(event, handler)
        }
    }

    private fun process(event: ChannelEvent, handler: ChannelEvent.Handler) {
        try {
            if (handler(event)) {
                webChannelResponseCol.updateOneById(event._id, ChannelEvent::status setTo ChannelEvent.Status.PROCESSED)
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    override fun save(channelEvent: ChannelEvent) {
        webChannelResponseCol.save(channelEvent)
    }
}
