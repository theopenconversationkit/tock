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

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import org.apache.commons.codec.binary.Hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class AlcmeonAuthorisationHandler(secret: String) : Handler<RoutingContext> {
    private val signingKey =
        SecretKeySpec(
            secret.toByteArray(),
            "HmacSHA256",
        )
    private val mac = Mac.getInstance("HmacSHA256")

    init {
        mac.init(signingKey)
    }

    override fun handle(routingContext: RoutingContext) {
        val alcmeonWebhookSignature = routingContext.request().getHeader("X-Alcmeon-Webhook-Signature")

        val normalisedPath = routingContext.normalizedPath()
        val requestPayload = routingContext.body().asString()

        val signature = calculateSignature("$normalisedPath $requestPayload")

        if (alcmeonWebhookSignature == signature) {
            routingContext.next()
        } else {
            routingContext.response().setStatusCode(403).end("Invalid Alcmeon webhook signature")
        }
    }

    private fun calculateSignature(payload: String): String {
        return String(Hex().encode(mac.doFinal(payload.toByteArray())))
    }
}
