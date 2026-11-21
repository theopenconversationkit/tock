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

package ai.tock.serverless

import ai.tock.serverless.RequestWork.Companion.runTracked
import ai.tock.shared.Executor
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.injector
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import ai.tock.shared.provide
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.concurrent.TimeoutException

/**
 * Handles a request in a serverless process: dispatches it to the routes of the connectors, or runs a periodic
 * task, then waits for all the work it started before answering, as the process is frozen once it has answered.
 *
 * The periodic tasks are run by `POST <tock_serverless_tasks_path>/<name>` (`/_tasks` by default) with the
 * `X-Auth-Token` header set to `tock_serverless_tasks_token`: the route does not exist when the token is not set.
 */
object ServerlessRequestHandler {
    private val logger = KotlinLogging.logger {}
    private val workTimeoutMs = longProperty("tock_serverless_request_work_timeout_ms", 120_000)
    private val tasksPath = property("tock_serverless_tasks_path", "/_tasks")
    private val tasksToken = propertyOrNull("tock_serverless_tasks_token")

    private val executor: TrackingExecutor
        get() = injector.provide<Executor>() as? TrackingExecutor ?: error("the Executor of a serverless process must be a TrackingExecutor")

    fun handle(request: HttpRequest): HttpResponse {
        val work = RequestWork()
        val response =
            work.runTracked {
                runBlocking(RequestWork.current.asContextElement(work)) {
                    if (request.path.startsWith("$tasksPath/") && request.method == HttpMethod.POST) {
                        runTask(request)
                    } else {
                        ServerlessBotServer.dispatch(request)
                    }
                }
            }
        return try {
            work.awaitIdle(workTimeoutMs)
            response
        } catch (e: TimeoutException) {
            logger.error(e) { "the work of ${request.method} ${request.path} did not end in $workTimeoutMs ms" }
            response
        }
    }

    private fun runTask(request: HttpRequest): HttpResponse {
        if (tasksToken == null || tasksToken != request.header("X-Auth-Token")) {
            return HttpResponse.status(404)
        }
        val name = request.path.removePrefix("$tasksPath/")
        val task = executor.periodicTasks[name] ?: return HttpResponse.status(404)
        executor.executeBlocking(task)
        return HttpResponse.status(202)
    }
}
