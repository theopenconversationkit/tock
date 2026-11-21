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

import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.http.HttpResponse
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import java.time.Duration
import java.util.Base64

/**
 * The acknowledgment of the webhooks, and the processing of the queued ones.
 *
 * @property queue where the webhooks are queued
 * @property queuedPaths the POST routes acknowledged at once and queued (the webhooks of the connectors)
 * @property secret signs the queued messages, see [SignedQueuedRequest]
 * @property deduplication remembers the processed requests: it must be shared by the instances, see the
 * `DynamoDeduplicationStore` of tock-serverless-dynamodb
 * @property deduplicationTtl how long a processed request is remembered
 * @property groupId the group of a request: the FIFO queues deliver the requests of a group in order. By default,
 * the path: all the webhooks of a connector are processed one after the other. A group per user is enough to keep
 * the order of the messages of each user, and lets the users be processed in parallel.
 * @property process replays a request: the dispatch to the routes, waiting for the work it starts
 */
class RequestQueueing(
    private val queue: RequestQueue,
    private val queuedPaths: Set<String>,
    private val secret: String,
    private val deduplication: DeduplicationStore = InMemoryDeduplicationStore(),
    private val deduplicationTtl: Duration = Duration.ofDays(1),
    private val groupId: (HttpRequest) -> String = { it.path },
    internal var process: (HttpRequest) -> HttpResponse = { HttpResponse.status(404) },
) {
    private val logger = KotlinLogging.logger {}

    init {
        require(secret.length >= 16) { "the secret of the queued requests must have at least 16 characters" }
    }

    /** True if [request] is acknowledged at once and queued. */
    fun isQueued(request: HttpRequest): Boolean = request.method == HttpMethod.POST && request.path in queuedPaths

    /** Queues [request], and answers 200 once the queue has stored it (500 if it failed: the sender retries). */
    fun acknowledge(request: HttpRequest): HttpResponse =
        try {
            queue.send(QueuedRequest.of(request, groupId(request)).also { logger.debug { "queue ${it.id} ${it.path}" } })
            HttpResponse.status(200)
        } catch (e: Exception) {
            logger.error(e) { "${request.path} could not be queued" }
            HttpResponse.status(500)
        }

    /** The message to send in the queue for [request]. */
    fun message(request: QueuedRequest): String = SignedQueuedRequest.sign(request, secret).toJson()

    /**
     * Processes a message of the queue, once.
     *
     * @return false if the message must be delivered again: it could not be processed, the route answered 5xx
     */
    fun processMessage(message: String): Boolean {
        val request =
            runCatching { SignedQueuedRequest.fromJson(message).verify(secret) }.getOrNull()
                ?: return true.also { logger.error { "a queued message without a valid signature is ignored" } }
        if (!deduplication.markIfAbsent(request.deduplicationKey, deduplicationTtl)) {
            logger.info { "duplicate ${request.path} ${request.id} ignored" }
            return true
        }
        val processed =
            try {
                process(request.toHttpRequest()).status < 500
            } catch (e: Exception) {
                logger.error(e) { "${request.path} ${request.id} failed" }
                false
            }
        if (!processed) {
            deduplication.unmark(request.deduplicationKey)
        }
        return processed
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class SqsRecord(
        val messageId: String,
        val body: String,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class SqsEvent(
        @com.fasterxml.jackson.annotation.JsonProperty("Records") val records: List<SqsRecord> = emptyList(),
    )

    private data class BatchItemFailure(
        val itemIdentifier: String,
    )

    private data class SqsBatchResponse(
        val batchItemFailures: List<BatchItemFailure>,
    )

    /**
     * Processes an SQS event, as the Lambda Web Adapter forwards it (`AWS_LWA_PASS_THROUGH_PATH`).
     * The answer lists the failed messages: the event source mapping must report the batch item failures.
     */
    fun processSqsEvent(request: HttpRequest): HttpResponse {
        val event = mapper.readValue<SqsEvent>(request.body)
        val failures = event.records.filterNot { processMessage(it.body) }.map { BatchItemFailure(it.messageId) }
        return HttpResponse.json(mapper.writeValueAsString(SqsBatchResponse(failures)))
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class PubSubMessage(
        val data: String,
        val messageId: String? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class PubSubPush(
        val message: PubSubMessage,
    )

    /**
     * Processes a message pushed by a Pub/Sub subscription: 2xx acknowledges it, 5xx lets Pub/Sub retry.
     */
    fun processPubSubPush(request: HttpRequest): HttpResponse {
        val push = mapper.readValue<PubSubPush>(request.body)
        val message = String(Base64.getDecoder().decode(push.message.data))
        return HttpResponse.status(if (processMessage(message)) 204 else 503)
    }
}
