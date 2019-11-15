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

package ai.tock.nlp.build

import ai.tock.shared.jackson.mapper
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.makeDetailedHealthcheck
import io.vertx.ext.web.RoutingContext

/**
 *
 */
class HealthCheckVerticle(
    val buildVerticle: BuildModelWorkerVerticle
) : WebVerticle() {

    override fun configure() {
        //do nothing
    }

    override fun defaultHealthcheck(): (RoutingContext) -> Unit =
        { context ->
            context.response().end(
                mapper.writeValueAsString(
                    listOf(
                    "current build" to !buildVerticle.canAnalyse.get()
                    )
                )
            )
        }

    override fun detailedHealthcheck(): (RoutingContext) -> Unit = makeDetailedHealthcheck(
        selfCheck = { !buildVerticle.canAnalyse.get() }
    )

}