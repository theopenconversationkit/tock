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

package fr.vsct.tock.nlp.api

import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.shared.codec.ApplicationDump
import fr.vsct.tock.nlp.front.shared.parser.QueryDescription
import fr.vsct.tock.shared.vertx.WebVerticle
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

/**
 *
 */
class NlpVerticle : WebVerticle(KotlinLogging.logger {}) {

    override val rootPath: String = "/rest/nlp"

    override fun configure() {
        val front = FrontClient

        blockingJsonPost("/parse") { _, query: QueryDescription ->
            front.parse(query)
        }

        blockingUploadPost("/dump/import") { _, dump: ApplicationDump ->
            front.import(dump.application.namespace, dump).modified
        }

        blockingJsonPost("/dump/import/plain") { _, dump: ApplicationDump ->
            front.import(dump.application.namespace, dump).modified
        }

    }

    override fun healthcheck(): (RoutingContext) -> Unit {
        return { it.response().end() }
    }
}