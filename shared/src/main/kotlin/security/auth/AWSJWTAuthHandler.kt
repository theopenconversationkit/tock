/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.shared.security.auth

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.impl.JWTAuthHandlerImpl
import mu.KLogger
import mu.KotlinLogging

internal class AWSJWTAuthHandler(authProvider: JWTAuth, skip: String?) : JWTAuthHandlerImpl(authProvider, skip) {

    private val skip: String? = null
    private val logger: KLogger = KotlinLogging.logger {}

    private val options: JsonObject? = JsonObject()

    override fun parseCredentials(context: RoutingContext, handler: Handler<AsyncResult<JsonObject>>) {

        if (skip != null && context.normalisedPath().startsWith(skip)) {
            context.next()
            return
        }

        parseAuthorization(context, true) { parseAuthorization ->
            if (parseAuthorization.failed()) {
                handler.handle(Future.failedFuture(parseAuthorization.cause()))
                return@parseAuthorization
            }
            val jwtToken = context.request().getHeader("x-amzn-oidc-data")
            logger.trace { "jwtToken:$jwtToken" }

            handler.handle(
                Future.succeededFuture(
                    JsonObject().put("jwt", jwtToken).put("options", options)
                )
            )
        }
    }
}