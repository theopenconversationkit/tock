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

package ai.tock.serverless.queue

import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID

/**
 * A request received by a webhook, acknowledged at once and queued, to be processed by another invocation.
 *
 * @property groupId the messages of a group are delivered in order by the FIFO queues (SQS FIFO, Pub/Sub ordering keys)
 * @property deduplicationKey the key under which the request is processed only once
 */
data class QueuedRequest(
    val id: String,
    val method: HttpMethod,
    val path: String,
    val headers: Map<String, List<String>>,
    val queryParameters: Map<String, List<String>>,
    val body: String,
    val remoteAddress: String?,
    val groupId: String,
    val deduplicationKey: String,
    val rawQuery: String? = null,
) {
    /** The request to replay. */
    fun toHttpRequest(): HttpRequest = HttpRequest(method, path, headers, queryParameters, body = Base64.getDecoder().decode(body), remoteAddress = remoteAddress, rawQuery = rawQuery)

    /** The JSON sent in the queue. */
    fun toJson(): String = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String): QueuedRequest = mapper.readValue(json)

        /**
         * Queues [request]. The deduplication key is a hash of its path and body: a webhook sent twice
         * (a retry of the sender, a redelivery of the queue) is processed once.
         */
        fun of(
            request: HttpRequest,
            groupId: String = request.path,
        ): QueuedRequest =
            QueuedRequest(
                id = UUID.randomUUID().toString(),
                method = request.method,
                path = request.path,
                headers = request.headers,
                queryParameters = request.queryParameters,
                body = Base64.getEncoder().encodeToString(request.body),
                remoteAddress = request.remoteAddress,
                groupId = groupId,
                deduplicationKey = sha256(request.path.toByteArray() + 0 + request.body),
                rawQuery = request.rawQuery,
            )

        private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
