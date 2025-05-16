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

package ai.tock.bot.connector.whatsapp.cloud.database.repository

import ai.tock.bot.connector.whatsapp.cloud.database.model.PayloadWhatsAppCloud
import ai.tock.shared.TOCK_BOT_DATABASE
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.intProperty
import ai.tock.shared.longProperty
import ai.tock.shared.property
import com.github.salomonbrys.kodein.instance
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import mu.KotlinLogging
import org.litote.kmongo.*
import java.util.concurrent.TimeUnit

object PayloadWhatsAppCloudMongoDAO : PayloadWhatsAppCloudDAO {

    /**
     * Name of the MongoDB database collection used to store WhatsApp payloads.
     */
    private val collectionName = property("tock_whatsapp_payload", "whatsapp_payload")
    private val payloadTTL = longProperty("tock_whatsapp_payload_ttl_days", 10)
    private val uuidRegex = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")

    private val database: MongoDatabase by injector.instance(
        TOCK_BOT_DATABASE
    )

    private val logger = KotlinLogging.logger {}

    private val collection: MongoCollection<PayloadWhatsAppCloud>
    private fun MongoDatabase.collectionExists(collectionName: String): Boolean =
        listCollectionNames().contains(collectionName)

    init {
        database.createCollectionIfNotExists()
        collection = database.getCollection<PayloadWhatsAppCloud>(collectionName)
        collection.addExpiryIndex()
    }

    override fun getPayloadById(id: String): String? {
        return if (isUUID(id)) {
            collection.findOneById(id)?.payload
        } else {
            id
        }
    }

    override fun save(payloadWhatsAppCloud: PayloadWhatsAppCloud) {
        collection.save(payloadWhatsAppCloud)
    }

    private fun MongoDatabase.createCollectionIfNotExists() {
        if (!database.collectionExists(collectionName)) {
            try {
                createCollection(collectionName)
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }

    private fun MongoCollection<PayloadWhatsAppCloud>.addExpiryIndex() {
        try {
            this.ensureIndex(
                PayloadWhatsAppCloud::payloadExpireDate,
                indexOptions = IndexOptions().expireAfter(payloadTTL, TimeUnit.DAYS)
            )
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    private fun isUUID(uuid: String): Boolean {
        return uuidRegex.matches(uuid)
    }
}
