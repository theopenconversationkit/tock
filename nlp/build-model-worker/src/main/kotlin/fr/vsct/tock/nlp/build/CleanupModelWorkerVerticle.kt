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

package fr.vsct.tock.nlp.build

import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.shared.error
import io.vertx.core.AbstractVerticle
import mu.KotlinLogging

/**
 *
 */
class CleanupModelWorkerVerticle : AbstractVerticle() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun start() {
        vertx.setPeriodic(12 * 60 * 60 * 1000, {
            try {
                logger.debug { "remove orphan models..." }
                FrontClient.deleteOrphans()
                logger.debug { "end remove orphan models" }
            } catch (t: Throwable) {
                logger.error(t)
            }
        })
    }
}