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

package ai.tock.duckling.service

import ai.tock.shared.error
import ai.tock.shared.jackson.mapper
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.blocking
import ai.tock.shared.vertx.detailedHealthcheck
import clojure.lang.Keyword
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import io.vertx.core.Promise
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 *
 */
class DucklingVerticle : WebVerticle() {
    data class ParseRequest(
        val language: String,
        val dimensions: List<String>,
        val referenceDate: ZonedDateTime,
        val referenceTimezone: ZoneId,
        val textToParse: String,
    )

    class KeywordSerializer : JsonSerializer<Keyword>() {
        override fun serialize(
            keyword: Keyword,
            gen: JsonGenerator,
            provider: SerializerProvider,
        ) {
            gen.writeString(keyword.name)
        }
    }

    init {
        mapper.registerModule(SimpleModule().addSerializer(Keyword::class.java, KeywordSerializer()))
    }

    override val logger: KLogger = KotlinLogging.logger {}

    override fun configure() {
        blockingJsonPost("/parse") { _, request: ParseRequest ->
            with(request) {
                DucklingBridge.parse(language, textToParse, dimensions, referenceDate, referenceTimezone)
            }
        }
    }

    override fun defaultHealthcheck(): (RoutingContext) -> Unit {
        return { context -> if (DucklingBridge.initialized) context.response().end() else context.fail(500) }
    }

    override fun detailedHealthcheck(): (RoutingContext) -> Unit =
        detailedHealthcheck(
            listOf(
                Pair("duckling_bridge", { DucklingBridge.initialized }),
            ),
        )

    override fun startServer(promise: Promise<Void>) {
        vertx.blocking<Boolean>(
            {
                try {
                    logger.info { "Start duckling initialization" }
                    DucklingBridge.initDuckling()
                    logger.info { "End duckling initialization" }
                    it.complete()
                } catch (e: Exception) {
                    logger.error(e)
                    it.fail(e)
                }
            },
            {
                if (it.succeeded()) {
                    super.startServer(promise)
                } else {
                    promise.fail(it.cause())
                }
            },
        )
    }
}
