package fr.vsct.tock.shared.security

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.impl.JWTAuthHandlerImpl
import mu.KLogger
import mu.KotlinLogging

class AWSJWTAuthHandlerImpl(authProvider: JWTAuth, skip: String?) : JWTAuthHandlerImpl(authProvider, skip) {

    private val skip: String? = null
    val logger: KLogger = KotlinLogging.logger {}

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
            logger.info { jwtToken }

            handler.handle(
                Future.succeededFuture(
                    JsonObject().put("jwt",jwtToken).put(
                        "options",
                        options
                    )
                )
            )
        }
    }
}