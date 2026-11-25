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

package ai.tock.shared.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Where the work that outlives a request runs: the processing of a message received by a webhook
 * that has already been acknowledged, for instance.
 *
 * In a long-running process, it is a [CoroutineScope] (the one of a verticle). In a serverless function,
 * whose process is frozen once the response is sent, the implementation keeps track of the launched jobs
 * so that the request waits for them.
 */
interface ExecutionScope {
    /**
     * Launches [block] without waiting for it.
     */
    fun launch(block: suspend CoroutineScope.() -> Unit): Job
}

/**
 * An [ExecutionScope] that launches the jobs in a [CoroutineScope].
 */
class CoroutineExecutionScope(
    private val scope: CoroutineScope,
) : ExecutionScope {
    override fun launch(block: suspend CoroutineScope.() -> Unit): Job = scope.launch(block = block)
}
