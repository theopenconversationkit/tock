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

import ai.tock.shared.coroutines.ExecutionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch

/**
 * The [ExecutionScope] of a serverless process: the coroutines launched while a request is handled keep its
 * [RequestWork] in their context, so that the work they start is tracked too, and the request waits for them.
 */
class TrackingExecutionScope : ExecutionScope {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        val work = RequestWork.current.get()
        return if (work == null) {
            scope.launch(block = block)
        } else {
            scope.launch(RequestWork.current.asContextElement(work), block = block).also {
                work.track(it.asCompletableFuture())
            }
        }
    }
}
