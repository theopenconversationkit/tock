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

package ai.tock.bot.connector.alcmeon

import org.apache.commons.codec.binary.Hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Verifies the `X-Alcmeon-Webhook-Signature` of the webhooks: the HMAC-SHA256 of `<path> <body>`.
 */
class AlcmeonSignatureVerifier(
    secret: String,
) {
    private val signingKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")

    /** True if [signature] is the one of [path] and [body]. */
    fun verify(
        path: String,
        body: String,
        signature: String?,
    ): Boolean = signature == calculateSignature("$path $body")

    // a Mac is not thread safe: one by verification
    private fun calculateSignature(payload: String): String = String(Hex().encode(Mac.getInstance("HmacSHA256").apply { init(signingKey) }.doFinal(payload.toByteArray())))
}
