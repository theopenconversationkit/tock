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

package ai.tock.serverless.pubsub

import ai.tock.serverless.queue.QueuedRequest
import ai.tock.serverless.queue.RequestQueue
import ai.tock.serverless.queue.RequestQueueing
import ai.tock.shared.jackson.mapper
import ai.tock.shared.property
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.time.Instant
import java.util.Base64

/**
 * Queues the webhooks in a Google Cloud Pub/Sub topic, with the REST API: no client library. A push subscription
 * delivers them to `<tock_serverless_queue_path>/pubsub`.
 *
 * With [orderingKeys], the messages of a group are delivered in order: the subscription must enable the
 * message ordering. The duplicates are dropped by [RequestQueueing].
 *
 * @param topic `projects/<project>/topics/<topic>`
 * @param endpoint the Pub/Sub API (`tock_serverless_pubsub_endpoint`), or the emulator
 * @param accessToken the OAuth token of the service account: from the metadata server of Cloud Run by default,
 * null for the emulator
 */
class PubSubRequestQueue(
    private val topic: String,
    private val queueing: () -> RequestQueueing,
    private val endpoint: String = property("tock_serverless_pubsub_endpoint", "https://pubsub.googleapis.com"),
    private val accessToken: () -> String? = MetadataServerToken(),
    private val orderingKeys: Boolean = false,
    private val client: HttpClient = HttpClient.newHttpClient(),
) : RequestQueue {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private data class PubSubMessage(
        val data: String,
        val attributes: Map<String, String>,
        val orderingKey: String? = null,
    )

    private data class PublishRequest(
        val messages: List<PubSubMessage>,
    )

    override fun send(request: QueuedRequest) {
        val message =
            PubSubMessage(
                data = Base64.getEncoder().encodeToString(queueing().message(request).toByteArray()),
                attributes = mapOf("deduplicationKey" to request.deduplicationKey),
                orderingKey = request.groupId.takeIf { orderingKeys },
            )
        val response =
            client.send(
                HttpRequest
                    .newBuilder(URI("$endpoint/v1/$topic:publish"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .apply { accessToken()?.let { header("Authorization", "Bearer $it") } }
                    .POST(HttpRequest.BodyPublishers.ofByteArray(mapper.writeValueAsBytes(PublishRequest(listOf(message)))))
                    .build(),
                BodyHandlers.ofString(),
            )
        check(response.statusCode() in 200..299) { "Pub/Sub publish failed: ${response.statusCode()} ${response.body()}" }
    }
}

/**
 * The access token of the service account of the instance, from the metadata server (Cloud Run, GCE), cached
 * until a minute before it expires. `GCE_METADATA_HOST` replaces the metadata server, as for the Google libraries.
 */
class MetadataServerToken(
    private val metadataHost: String = System.getenv("GCE_METADATA_HOST") ?: "metadata.google.internal",
    private val client: HttpClient = HttpClient.newHttpClient(),
) : () -> String? {
    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Token(
        val access_token: String,
        val expires_in: Long,
    )

    @Volatile
    private var cached: Pair<String, Instant>? = null

    @Synchronized
    override fun invoke(): String {
        cached?.takeIf { it.second.isAfter(Instant.now()) }?.let { return it.first }
        val response =
            client.send(
                HttpRequest
                    .newBuilder(URI("http://$metadataHost/computeMetadata/v1/instance/service-accounts/default/token"))
                    .header("Metadata-Flavor", "Google")
                    .timeout(Duration.ofSeconds(5))
                    .build(),
                BodyHandlers.ofString(),
            )
        check(response.statusCode() == 200) { "no token from the metadata server: ${response.statusCode()}" }
        val token = mapper.readValue<Token>(response.body())
        cached = token.access_token to Instant.now().plusSeconds(token.expires_in - 60)
        return token.access_token
    }
}
