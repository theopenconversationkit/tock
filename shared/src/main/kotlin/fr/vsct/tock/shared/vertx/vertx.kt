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

package fr.vsct.tock.shared.vertx

import fr.vsct.tock.shared.Runner
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
val vertx = Vertx.vertx(VertxOptions().setMaxWorkerExecuteTime(1000 * 60L * 1000 * 1000000))

fun <T> Vertx.blocking(blockingHandler: (Future<T>) -> Unit, resultHandler: (AsyncResult<T>) -> Unit) {
    this.executeBlocking(
            { future: Future<T> ->
                try {
                    blockingHandler.invoke(future)
                } catch(throwable: Throwable) {
                    fr.vsct.tock.shared.vertx.logger.error(throwable) { throwable.message }
                    future.fail(throwable)
                }
            },
            false,
            {
                try {
                    resultHandler.invoke(it)
                } catch(e: Throwable) {
                    fr.vsct.tock.shared.vertx.logger.error(e) { e.message }
                }
            })
}

fun vertxRunner(): Runner {
    return object : Runner {
        override fun executeBlocking(runnable: () -> Unit) {
            vertx.blocking<Unit>({
                runnable.invoke()
                it.complete()
            }, {})
        }
    }
}


