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

package ai.tock.serverless.sqs

import ai.tock.serverless.queue.QueuedRequest
import ai.tock.serverless.queue.RequestQueue
import ai.tock.serverless.queue.RequestQueueing
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.services.sqs.SqsClient

/**
 * Queues the webhooks in Amazon SQS. The queue triggers the function (Lambda event source mapping, with the
 * batch item failures reported), and the Lambda Web Adapter posts the events to `<tock_serverless_queue_path>/sqs`
 * (`AWS_LWA_PASS_THROUGH_PATH`).
 *
 * On a FIFO queue (its URL ends with `.fifo`), the messages of a group are delivered in order, and SQS drops the
 * duplicates sent within 5 minutes; [RequestQueueing] drops the others.
 * A message is at most 1 MiB: the webhook, encoded in base 64, with its headers.
 */
class SqsRequestQueue(
    private val queueUrl: String,
    private val queueing: () -> RequestQueueing,
    private val client: SqsClient = SqsClient.builder().httpClient(UrlConnectionHttpClient.create()).build(),
) : RequestQueue {
    private val fifo = queueUrl.endsWith(".fifo")

    override fun send(request: QueuedRequest) {
        client.sendMessage { message ->
            message.queueUrl(queueUrl).messageBody(queueing().message(request))
            if (fifo) {
                message.messageGroupId(request.groupId).messageDeduplicationId(request.deduplicationKey)
            }
        }
    }
}
