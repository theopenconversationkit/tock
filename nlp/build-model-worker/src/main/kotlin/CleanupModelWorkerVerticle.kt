/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import com.github.salomonbrys.kodein.instance
import ai.tock.nlp.front.client.FrontClient
import ai.tock.shared.Executor
import ai.tock.shared.booleanProperty
import ai.tock.shared.injector
import io.vertx.core.AbstractVerticle
import mu.KotlinLogging
import java.time.Duration

/**
 *
 */
class CleanupModelWorkerVerticle : AbstractVerticle() {

    private val executor: Executor by injector.instance()

    override fun start() {
        if(BuildModelWorker.cleanupModelEnabled) {
            executor.setPeriodic(Duration.ofHours(12), {
                BuildModelWorker.cleanupModel()
            })
        }
    }
}