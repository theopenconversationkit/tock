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

package ai.tock.bot.connector.iadvize

import ai.tock.bot.connector.iadvize.model.request.MessageRequest
import ai.tock.bot.connector.iadvize.model.request.MessageRequest.MessageRequestJson
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.iadvize.client.graphql.ChatbotActionOrMessageInput
import ai.tock.iadvize.client.graphql.IadvizeGraphQLClient
import ai.tock.shared.jackson.mapper
import ai.tock.shared.resourceAsString
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.raw
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the deferred messaging flow.
 *
 * New design:
 * - handleRequest() automatically starts deferred mode if chatBotId is valid
 * - send() collects messages
 * - send() with lastAnswer=true triggers the flush via GraphQL
 * - forceFlushCoordinator() handles timeout/error scenarios
 */
class IadvizeDeferredEndFlowTest {
    private val applicationId = "appId"
    private val path = "/path"
    private val editorURL = "/editorUrl"
    private val firstMessage = "firstMessage"
    private val distributionRule = "distributionRuleId"
    private val distributionRuleUnavailableMessage = "distributionRuleUnavailableMessage"

    private lateinit var connector: IadvizeConnector
    private val controller: ConnectorController = mockk(relaxed = true)
    private val context: RoutingContext = mockk(relaxed = true)
    private val response: HttpServerResponse = mockk(relaxed = true)
    private val translator: I18nTranslator = mockk()
    private val graphQLCallCount = AtomicInteger(0)
    private val capturedMessages = mutableListOf<String>()

    private val testConversationId = "conv-123"
    private val testChatBotId = "42"
    private val testMetadata =
        mapOf(
            IadvizeConnectorMetadata.CONVERSATION_ID.name to testConversationId,
            IadvizeConnectorMetadata.CHAT_BOT_ID.name to testChatBotId,
            IadvizeConnectorMetadata.OPERATOR_ID.name to "sd-42",
            IadvizeConnectorMetadata.IADVIZE_ENV.name to "sd",
        )

    @BeforeEach
    fun setUp() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein {
                import(sharedTestModule)
            },
        )

        connector =
            IadvizeConnector(
                applicationId,
                path,
                editorURL,
                firstMessage,
                distributionRule,
                null,
                distributionRuleUnavailableMessage,
                null,
            )

        every { context.response() } returns response
        every { response.putHeader("Content-Type", "application/json") } returns response
        every { controller.botConfiguration.name } returns "botName"
        every { controller.botDefinition.botId } returns "botId"
        every { controller.botDefinition.i18nTranslator(any(), any(), any(), any()) } returns translator

        val messageSlot = slot<CharSequence>()
        every { translator.translate(capture(messageSlot)) } answers {
            I18nLabelValue("", "", "", messageSlot.captured).raw
        }

        // Reset counters
        graphQLCallCount.set(0)
        capturedMessages.clear()

        // Mock GraphQL client
        mockkConstructor(IadvizeGraphQLClient::class)
        every {
            anyConstructed<IadvizeGraphQLClient>().sendProactiveActionOrMessage(
                any(),
                any(),
                any(),
            )
        } answers {
            graphQLCallCount.incrementAndGet()
            val actionOrMessage = thirdArg<ChatbotActionOrMessageInput>()
            actionOrMessage.chatbotMessage?.chatbotSimpleTextMessage?.let {
                synchronized(capturedMessages) {
                    capturedMessages.add(it)
                }
            }
            true
        }
    }

    @AfterEach
    fun tearDown() {
        tockInternalInjector = KodeinInjector()
        unmockkAll()
    }

    private fun createCallback(): IadvizeConnectorCallback {
        val requestJson =
            mapper.readValue(
                resourceAsString("/request_message_text.json"),
                MessageRequestJson::class.java,
            )
        val request = MessageRequest(requestJson, testConversationId)

        return IadvizeConnectorCallback(
            applicationId,
            controller,
            Locale.FRENCH,
            context,
            request,
            distributionRule,
            distributionRuleUnavailableMessage,
        )
    }

    private fun createSendSentence(
        text: String,
        lastAnswer: Boolean = false,
    ): SendSentence {
        return SendSentence(
            playerId = PlayerId("test-user"),
            applicationId = applicationId,
            recipientId = PlayerId("test-bot"),
            text = text,
            metadata = ActionMetadata(lastAnswer = lastAnswer),
        )
    }

    // ==============================
    // DeferredMessageCoordinator Tests
    // ==============================

    @Test
    fun `coordinator start sends HTTP response immediately`() {
        // Given
        val callback = createCallback()
        val coordinator = DeferredMessageCoordinator(callback, testMetadata)

        // When
        coordinator.start()

        // Then - HTTP response should have been sent
        verify { response.putHeader("Content-Type", "application/json") }
        verify { response.end(any<String>()) }
        assertTrue(coordinator.hasStarted())
    }

    @Test
    fun `coordinator collect adds message to queue`() {
        // Given
        val callback = createCallback()
        val coordinator = DeferredMessageCoordinator(callback, testMetadata)
        coordinator.start()

        // When
        coordinator.collect(IadvizeConnectorCallback.ActionWithDelay(createSendSentence("Test"), 0))

        // Then
        assertEquals(1, coordinator.messageCount())
    }

    @Test
    fun `coordinator end flushes messages`() {
        // Given
        val callback = createCallback()
        val coordinator = DeferredMessageCoordinator(callback, testMetadata)
        coordinator.start()
        coordinator.collect(IadvizeConnectorCallback.ActionWithDelay(createSendSentence("Msg1"), 0))
        coordinator.collect(IadvizeConnectorCallback.ActionWithDelay(createSendSentence("Msg2"), 0))

        val flushedActions = mutableListOf<String>()

        // When
        val ended =
            coordinator.end { action ->
                (action.action as? SendSentence)?.text?.toString()?.let { flushedActions.add(it) }
            }

        // Then
        assertTrue(ended)
        assertTrue(coordinator.hasEnded())
        assertEquals(2, flushedActions.size)
        assertTrue(flushedActions.contains("Msg1"))
        assertTrue(flushedActions.contains("Msg2"))
        assertEquals(0, coordinator.messageCount()) // Queue is drained
    }

    @Test
    fun `coordinator end is idempotent`() {
        // Given
        val callback = createCallback()
        val coordinator = DeferredMessageCoordinator(callback, testMetadata)
        coordinator.start()
        coordinator.collect(IadvizeConnectorCallback.ActionWithDelay(createSendSentence("Msg"), 0))

        var callCount = 0

        // When - call end twice
        val firstEnd = coordinator.end { callCount++ }
        val secondEnd = coordinator.end { callCount++ }

        // Then - only first call should execute
        assertTrue(firstEnd)
        kotlin.test.assertFalse(secondEnd)
        assertEquals(1, callCount)
    }

    @Test
    fun `coordinator forceEnd flushes with error message`() {
        // Given
        val callback = createCallback()
        val coordinator = DeferredMessageCoordinator(callback, testMetadata)
        coordinator.start()
        coordinator.collect(IadvizeConnectorCallback.ActionWithDelay(createSendSentence("Collected"), 0))

        val flushedActions = mutableListOf<String>()
        val errorAction = IadvizeConnectorCallback.ActionWithDelay(createSendSentence("Error occurred"), 0)

        // When
        val ended =
            coordinator.forceEnd(
                sendAction = { action ->
                    (action.action as? SendSentence)?.text?.toString()?.let { flushedActions.add(it) }
                },
                errorAction = errorAction,
                logMessage = "Test timeout",
            )

        // Then
        assertTrue(ended)
        assertTrue(coordinator.hasEnded())
        assertEquals(2, flushedActions.size)
        assertTrue(flushedActions.contains("Collected"))
        assertTrue(flushedActions.contains("Error occurred"))
    }

    // ==============================
    // IadvizeConnector.send() Tests
    // ==============================

    @Test
    fun `send in deferred mode collects message`() {
        // Given
        val callback = createCallback()
        callback.deferredCoordinator = DeferredMessageCoordinator(callback, testMetadata)
        callback.deferredCoordinator?.start()

        // When
        connector.send(createSendSentence("Collected message"), callback, 0)

        // Then - message collected, no GraphQL call yet
        assertEquals(0, graphQLCallCount.get())
        assertEquals(1, callback.deferredCoordinator?.messageCount())
    }

    @Test
    fun `send with lastAnswer true triggers flush via GraphQL`() {
        // Given
        val callback = createCallback()
        callback.deferredCoordinator = DeferredMessageCoordinator(callback, testMetadata)
        callback.deferredCoordinator?.start()

        // When
        connector.send(createSendSentence("Final answer", lastAnswer = true), callback, 0)

        // Then - message sent via GraphQL
        Thread.sleep(100) // Allow queue to process
        assertEquals(1, graphQLCallCount.get())
        assertEquals("Final answer", capturedMessages.firstOrNull())
        assertNull(callback.deferredCoordinator) // Cleaned up
    }

    @Test
    fun `multiple sends collected then flushed on lastAnswer`() {
        // Given
        val callback = createCallback()
        callback.deferredCoordinator = DeferredMessageCoordinator(callback, testMetadata)
        callback.deferredCoordinator?.start()

        // When
        connector.send(createSendSentence("Message 1"), callback, 0)
        connector.send(createSendSentence("Message 2"), callback, 0)
        connector.send(createSendSentence("Final", lastAnswer = true), callback, 0)

        // Then
        Thread.sleep(100)
        assertEquals(3, graphQLCallCount.get())
        assertTrue(capturedMessages.contains("Message 1"))
        assertTrue(capturedMessages.contains("Message 2"))
        assertTrue(capturedMessages.contains("Final"))
    }

    // ==============================
    // Standard Mode Tests
    // ==============================

    @Test
    fun `send in standard mode adds to callback actions`() {
        // Given
        val callback = createCallback()
        // No coordinator = standard mode

        // When
        connector.send(createSendSentence("Standard message"), callback, 0)

        // Then - added to callback.actions, no GraphQL
        assertEquals(0, graphQLCallCount.get())
        assertEquals(1, callback.actions.size)
    }

    @Test
    fun `send in standard mode with lastAnswer sends HTTP response`() {
        // Given
        val callback = createCallback()
        // No coordinator = standard mode

        // When
        connector.send(createSendSentence("Final", lastAnswer = true), callback, 0)

        // Then - HTTP response sent
        verify { response.putHeader("Content-Type", "application/json") }
        verify { response.end(any<String>()) }
    }

    // ==============================
    // Integration Test: Simulated RAG Flow
    // ==============================

    @Test
    fun `RAG flow simulation with automatic deferred mode`() {
        // Given - simulate what handleRequest() does for deferred mode
        val callback = createCallback()
        callback.deferredCoordinator = DeferredMessageCoordinator(callback, testMetadata)
        callback.deferredCoordinator?.start() // Sends HTTP 200

        // Verify HTTP 200 sent
        verify { response.putHeader("Content-Type", "application/json") }
        verify { response.end(any<String>()) }

        // Simulate RAG processing...
        connector.send(createSendSentence("Debug info"), callback, 0)

        // RAG sends final answer with end()
        connector.send(createSendSentence("RAG Response with footnotes", lastAnswer = true), callback, 0)

        // Then - all messages flushed via GraphQL
        Thread.sleep(100)
        assertEquals(2, graphQLCallCount.get())
        assertTrue(capturedMessages.contains("Debug info"))
        assertTrue(capturedMessages.contains("RAG Response with footnotes"))
        assertNull(callback.deferredCoordinator)
    }
}
