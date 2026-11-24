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

package ai.tock.serverless.queue

/**
 * Processes the queued requests in the background of the same process: for a process that keeps running
 * after its answer (Cloud Run billed per instance, a development server), not for AWS Lambda.
 */
class BackgroundRequestQueue(
    private val consumer: () -> RequestQueueing,
) : RequestQueue {
    override fun send(request: QueuedRequest) {
        val message = consumer().message(request)
        // outside of the current request: the webhook does not wait for it
        Thread.ofVirtual().start {
            consumer().processMessage(message)
        }
    }
}
