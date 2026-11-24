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

import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * What is sent in the queue: the JSON of a [QueuedRequest], signed with a secret shared by the producer and
 * the consumer. The consumer route is reachable by anyone who can call the function (behind the Lambda Web
 * Adapter, it shares the port of the webhooks): without the signature, a forged message could replay a request
 * from an allowed address, or with forged headers.
 */
data class SignedQueuedRequest(
    val request: String,
    val signature: String,
) {
    fun toJson(): String = mapper.writeValueAsString(this)

    /** The request, if the signature is valid. */
    fun verify(secret: String): QueuedRequest? =
        QueuedRequest.fromJson(request).takeIf {
            MessageDigest.isEqual(hmac(secret, request).toByteArray(), signature.toByteArray())
        }

    companion object {
        fun sign(
            request: QueuedRequest,
            secret: String,
        ): SignedQueuedRequest = request.toJson().let { SignedQueuedRequest(it, hmac(secret, it)) }

        fun fromJson(json: String): SignedQueuedRequest = mapper.readValue(json)

        private fun hmac(
            secret: String,
            payload: String,
        ): String {
            val mac = Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(secret.toByteArray(), "HmacSHA256")) }
            return mac.doFinal(payload.toByteArray()).joinToString("") { "%02x".format(it) }
        }
    }
}
