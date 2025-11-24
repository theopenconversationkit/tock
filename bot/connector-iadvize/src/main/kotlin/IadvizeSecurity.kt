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

package ai.tock.bot.connector.iadvize

import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.jackson.mapper
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.math.BigInteger
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Utility object : calculate HMAC signature (protocol used for iAdvize message authentication)
 */
class IadvizeSecurity(private val secretToken: String) {
    companion object {
        const val HMAC_SHA256 = "HmacSHA256"
        const val HEADER_NAME = "X-iAdvize-Signature"
        const val ERROR_MESSAGE = "IAdvize signature validation failed : Invalid hash"
    }

    private val logger = KotlinLogging.logger {}

    fun validatePayloads(context: RoutingContext) {
        val payloads =
            if (HttpMethod.GET == context.request().method()) {
                // For GET requests, hash signature is computed by hashing the raw query string
                context.request().query()
            } else {
                // For POST, PUT... requests, hash signature is computed by hashing the raw body string
                context.body().asString()
            } ?: ""

        val xIAdvizeSignatureComputed = calculateHmacSha256(secretToken, payloads)
        val xIAdvizeSignatureHeader = context.request().getHeader(HEADER_NAME)

        if (xIAdvizeSignatureComputed != xIAdvizeSignatureHeader) {
            val error =
                mapper.writeValueAsString(
                    Error(secretToken, payloads, xIAdvizeSignatureComputed, xIAdvizeSignatureHeader),
                )

            logger.error { "$ERROR_MESSAGE $error" }
            throw BadRequestException(ERROR_MESSAGE)
        }
    }

    private fun calculateHmacSha256(
        secretKey: String,
        message: String,
    ): String {
        val mac = Mac.getInstance(HMAC_SHA256)
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), HMAC_SHA256)
        mac.init(secretKeySpec)
        val hmacSha256 = mac.doFinal(message.toByteArray(Charsets.UTF_8))

        return String.format("sha256=%064x", BigInteger(1, hmacSha256))
    }
}

data class Error(
    val secretToken: String,
    val payloads: String,
    val xIAdvizeSignatureComputed: String,
    val xIAdvizeSignatureHeader: String,
)
