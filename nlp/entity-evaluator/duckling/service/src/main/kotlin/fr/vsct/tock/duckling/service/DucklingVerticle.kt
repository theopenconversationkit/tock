/*
 * Copyright (C) 2017 VSCT
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

package fr.vsct.tock.duckling.service

import clojure.lang.Keyword
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.shared.vertx.WebVerticle
import fr.vsct.tock.shared.vertx.blocking
import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext
import fr.vsct.tock.shared.jackson.mapper
import mu.KotlinLogging
import java.time.ZonedDateTime

/**
 *
 */
class DucklingVerticle : WebVerticle(KotlinLogging.logger {}) {

    data class ParseRequest(
            val language: String,
            val dimensions: List<String>,
            val referenceDate: ZonedDateTime,
            val textToParse: String)

    class KeywordSerializer : JsonSerializer<Keyword>() {
        override fun serialize(keyword: Keyword, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeString(keyword.name)
        }
    }

    init {
        mapper.registerModule(SimpleModule().addSerializer(Keyword::class.java, KeywordSerializer()))
    }

    override fun configure() {
        blockingJsonPost("/parse") {
            _, request: ParseRequest ->
            with(request) {
                DucklingBridge.parse(language, textToParse, dimensions, referenceDate)
            }
        }
    }

    override fun healthcheck(): (RoutingContext) -> Unit {
        return { context -> if (DucklingBridge.initialized) context.response().end() else context.fail(500) }
    }

    override fun startServer(startFuture: Future<Void>) {
        vertx.blocking<Boolean>(
                {
                    logger.info { "Start duckling initialization" }
                    DucklingBridge.initDuckling()
                    logger.info { "End duckling initialization" }
                    it.complete()
                },
                {
                    super.startServer(startFuture)
                })
    }
}