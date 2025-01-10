/*
 * Copyright (C) 2017/2025 e-voyageurs technologies
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


import ai.tock.bot.connector.web.WebConnectorRequest
import ai.tock.bot.connector.web.WebConnectorResponseContent
import ai.tock.bot.engine.event.MetadataEvent
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler
import com.launchdarkly.eventsource.background.BackgroundEventSource
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class WebConnectorSSEIntegrationTest {

    @Test
    fun `send SSE message`() {
        // Creating the EventSource object
        val eventSource = BackgroundEventSource
            .Builder(
                SseEventHandler(),
                EventSource.Builder(
                    ConnectStrategy
                        .http(URI.create("http://localhost:8080/invictus/sse/direct").toURL())
                        // Specifying custom request headers
                        .header(
                            "message",
                            mapper.writeValueAsString(
                                WebConnectorRequest(
                                    userId = "userId",
                                    locale = Locale.FRENCH,
                                    streamedResponse = true,
                                    query = "Bonjour"
                                )
                            )
                        )
                        .connectTimeout(3, TimeUnit.SECONDS)
                        // Setting the maximum connection time, longer than the maximum connection time set by the server
                        .readTimeout(600, TimeUnit.SECONDS)
                )
            )
            .threadPriority(Thread.MAX_PRIORITY)
            .build()

        // Starting the EventSource connection
        eventSource.start()
        Thread.sleep(10000000000)
    }
}

class SseEventHandler() : BackgroundEventHandler {

    override fun onOpen() {
        // Write logic for handling successful SSE connection
        println("open connection")
    }

    override fun onClosed() {
        // Write logic for handling SSE connection closure
        println("close connection - exiting")
        exitProcess(0)
    }

    override fun onMessage(event: String, messageEvent: MessageEvent) {
        // Write logic for handling arrival of SSE events
        println("Event: $event")
        println("Message: ${messageEvent.data}")
        if (event == "message") {
            val message: WebConnectorResponseContent = mapper.readValue(messageEvent.data)
            if (message.metadata[MetadataEvent.LAST_ANSWER_METADATA] == "true") {
                println("Last answer - exiting")
                exitProcess(0)
            }
        }
        // event: String = Name of the channel or topic the event belongs to
        // messageEvent.lastEventId: String = ID of the arrived event
        // messageEvent.data: String = Data of the arrived event
    }

    override fun onComment(comment: String) {
        println("comment: $comment")
    }

    override fun onError(t: Throwable) {
        t.printStackTrace()
        // Write logic for handling errors before or after SSE connection

        // If the server responds with an error other than 2XX, com.launchdarkly.eventsource.StreamHttpErrorException: Server returned HTTP error 401 exception occurs
        // If the client sets a shorter connection time than the server, error=com.launchdarkly.eventsource.StreamIOException: java.net.SocketTimeoutException: timeout exception occurs
        // If the server terminates the connection due to exceeding the connection time, error=com.launchdarkly.eventsource.StreamClosedByServerException: Stream closed by server exception occurs
    }
}
