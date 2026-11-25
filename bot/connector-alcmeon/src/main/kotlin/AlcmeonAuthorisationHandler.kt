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

/**
 * The Vert.x handler of the signature: the connector uses [AlcmeonSignatureVerifier], which needs no server.
 */
class AlcmeonAuthorisationHandler(
    secret: String,
) : Handler<RoutingContext> {
    private val verifier = AlcmeonSignatureVerifier(secret)

    override fun handle(routingContext: RoutingContext) {
        val alcmeonWebhookSignature = routingContext.request().getHeader("X-Alcmeon-Webhook-Signature")

        if (verifier.verify(routingContext.normalizedPath(), routingContext.body().asString() ?: "", alcmeonWebhookSignature)) {
            routingContext.next()
        } else {
            routingContext.response().setStatusCode(403).end("Invalid Alcmeon webhook signature")
        }
    }
}
