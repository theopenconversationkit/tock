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
 * Where the acknowledged webhooks are queued: SQS (tock-serverless-sqs), Pub/Sub (tock-serverless-pubsub),
 * or [BackgroundRequestQueue] for a process that keeps running after its answer.
 */
fun interface RequestQueue {
    /** Queues [request]: it returns once the queue has stored it. */
    fun send(request: QueuedRequest)
}
