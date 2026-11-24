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

import ai.tock.shared.jackson.mapper
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Sends the response of an iAdvize request, whatever the server.
 */
interface IadvizeResponder {
    /** Answers the JSON of [response]. */
    fun json(response: Any)

    /** Answers an empty 200. */
    fun end()

    /** Fails the request (500). */
    fun fail(throwable: Throwable)
}

/** The JSON of an iAdvize response. */
internal fun iadvizeJson(response: Any): String =
    mapper.writeValueAsString(response).also {
        logger.debug { "iAdvize json response: $it" }
    }

/**
 * The responder of a Vert.x route: the former behaviour.
 */
internal class VertxIadvizeResponder(
    private val context: RoutingContext,
) : IadvizeResponder {
    override fun json(response: Any) {
        context.response().putHeader("Content-Type", "application/json").end(iadvizeJson(response))
    }

    override fun end() {
        context.response().end()
    }

    override fun fail(throwable: Throwable) {
        context.fail(throwable)
    }
}
