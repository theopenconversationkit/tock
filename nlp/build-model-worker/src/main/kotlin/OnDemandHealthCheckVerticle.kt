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

package ai.tock.nlp.build

import ai.tock.nlp.build.ondemand.WorkerOnDemandVerticle
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.detailedHealthcheck
import io.vertx.ext.web.RoutingContext

/**
 *
 */
class OnDemandHealthCheckVerticle(
    private val workerOnDemandVerticles: List<WorkerOnDemandVerticle>,
) : WebVerticle() {
    override fun configure() {
        // do nothing
    }

    override fun defaultHealthcheck(): (RoutingContext) -> Unit {
        return {
            it.response()
                .setStatusCode(
                    if (workerOnDemandVerticles.none { workerOnDemandVerticle -> !workerOnDemandVerticle.isLoaded() }) {
                        200
                    } else {
                        500
                    },
                )
                .end()
        }
    }

    override fun detailedHealthcheck(): (RoutingContext) -> Unit =
        detailedHealthcheck(
            workerOnDemandVerticles.map {
                Pair(it.name(), { it.isLoaded() })
            },
            selfCheck = { workerOnDemandVerticles.none { !it.isLoaded() } },
        )
}
