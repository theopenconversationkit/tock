/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.storage.dynamodb

import ai.tock.bot.connector.whatsapp.cloud.database.model.PayloadWhatsAppCloud
import ai.tock.bot.connector.whatsapp.cloud.database.repository.PayloadWhatsAppCloudDAO
import ai.tock.shared.longProperty
import ai.tock.storage.dynamodb.DynamoStore.runtimeTable
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import java.time.Instant

/**
 * The payloads of the WhatsApp Cloud buttons, in the runtime table (`WAPAYLOAD#<id>` / `P`): they expire
 * `tock_whatsapp_payload_ttl_days` (10) after their expiration date, as the Mongo TTL index does.
 */
object PayloadWhatsAppCloudDynamoDAO : PayloadWhatsAppCloudDAO {
    private val payloadTtlSeconds = longProperty("tock_whatsapp_payload_ttl_days", 10) * 24 * 3600
    private val uuidRegex = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")

    private const val SORT_KEY = "P"
    private const val PAYLOAD = "payload"

    private fun pk(id: String) = "WAPAYLOAD#$id"

    /** The payload of [id], or [id] itself when it is not the id of a stored payload (a short payload). */
    override fun getPayloadById(id: String): String? =
        if (uuidRegex.matches(id)) {
            DynamoStore
                .getItem(runtimeTable, pk(id), SORT_KEY)
                // the DynamoDB TTL removes the items late
                ?.takeIf { (it.long(TTL) ?: Long.MAX_VALUE) > Instant.now().epochSecond }
                ?.str(PAYLOAD)
        } else {
            id
        }

    override fun save(payloadWhatsAppCloud: PayloadWhatsAppCloud) {
        DynamoStore.putItem(
            runtimeTable,
            DynamoStore.key(pk(payloadWhatsAppCloud.id), SORT_KEY) +
                mapOf(
                    PAYLOAD to s(payloadWhatsAppCloud.payload),
                    TTL to n(payloadWhatsAppCloud.payloadExpireDate.toInstant().epochSecond + payloadTtlSeconds),
                ),
        )
    }
}

/** The DAO of the WhatsApp Cloud connector: imported by [botDynamoModule] when the connector is in the classpath. */
internal val whatsAppCloudDynamoModule =
    Kodein.Module(allowSilentOverride = true) {
        bind<PayloadWhatsAppCloudDAO>() with provider { PayloadWhatsAppCloudDynamoDAO }
    }
