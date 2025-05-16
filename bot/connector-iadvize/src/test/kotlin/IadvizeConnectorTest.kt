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

import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.connector.iadvize.model.payload.TextPayload
import ai.tock.bot.connector.iadvize.model.request.IadvizeRequest
import ai.tock.bot.connector.iadvize.model.request.MessageRequest
import ai.tock.bot.connector.iadvize.model.request.MessageRequest.MessageRequestJson
import ai.tock.bot.connector.iadvize.model.response.conversation.Duration
import ai.tock.bot.connector.iadvize.model.response.conversation.QuickReply
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.*
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.iadvize.client.graphql.IadvizeGraphQLClient
import ai.tock.shared.jackson.mapper
import ai.tock.shared.loadProperties
import ai.tock.shared.resourceAsString
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.raw
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import io.mockk.*
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

/**
 *
 */
class IadvizeConnectorTest {

    val applicationId = "appId"
    val path = "/path"
    val editorURL = "/editorUrl"
    val firstMessage = "firstMessage"
    val distributionRule = "distributionRuleId"
    val distributionRuleUnavailableMessage = "distributionRuleUnavailableMessage"
    val connector: IadvizeConnector by lazy {
        IadvizeConnector(
            applicationId,
            path,
            editorURL,
            firstMessage,
            distributionRule,
            null,
            distributionRuleUnavailableMessage,
            null
        )
    }
    val controller: ConnectorController = mockk(relaxed = true)
    val context: RoutingContext = mockk(relaxed = true)
    val response: HttpServerResponse = mockk(relaxed = true)
    val translator: I18nTranslator = mockk()
    val botName = "botName"
    val botId = "botId"
    val operateurId = "operateurId"
    val conversationId = "conversationId"


    private val marcus: String = "MARCUS"
    private val marcus1: String = "MARCUS1"
    private val marcus2: String = "MARCUS2"

    private val iadvizeGraphQLClient: IadvizeGraphQLClient = mockk(relaxed = true)

    @BeforeEach
    fun before() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein {
                import(sharedTestModule)
            }
        )

        every { context.response() } returns response
        every { response.putHeader("Content-Type", "application/json") } returns response
        every { controller.botConfiguration.name } returns botName
        every { controller.botDefinition.botId } returns botId
        every { context.pathParam("idOperator") } returns operateurId
        every { context.pathParam("idConversation") } returns conversationId

        every { controller.botDefinition.i18nTranslator(any(), any(), any(), any()) } returns translator

        val messageSlot = slot<CharSequence>()
        every { translator.translate(capture(messageSlot)) } answers {
            I18nLabelValue(
                "",
                "",
                "",
                messageSlot.captured
            ).raw
        }

        // Force date to expected date
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns LocalDateTime.of(2022, 4, 8, 16, 52, 37)
        every { LocalDateTime.of(2022, 4, 8, 16, 52, 37) } answers { callOriginal() }

    }

    @AfterEach
    fun after() {
        tockInternalInjector = KodeinInjector()
    }

    @Test
    fun handleRequestWithoutQuickReply_shouldHandleWell_MessageMarcus() {
        val iAdvizeRequest: IadvizeRequest = getIadvizeRequestMessage("/request_message_text.json", conversationId)
        val expectedResponse: String = resourceAsStringMinified("/response_message_marcus.json")

        val action1 = SendSentence(PlayerId("MockPlayerId"), "applicationId", PlayerId("recipientId"), "MARCUS1")
        val action2 = SendSentence(PlayerId("MockPlayerId"), "applicationId", PlayerId("recipientId"), "MARCUS2")
        val connectorData = slot<ConnectorData>()
        every { controller.handle(any(), capture(connectorData)) } answers {
            val callback = connectorData.captured.callback as IadvizeConnectorCallback
            callback.addAction(action1, 0)
            callback.addAction(action2, 0)
            callback.eventAnswered(action2)
        }

        connector.handleRequest(
            controller,
            context,
            iAdvizeRequest
        )

        verify { controller.handle(any(), any()) }

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)
    }

    @Test
    fun handleRequestWithQuickReply_shouldHandleWell_MessageMarcus() {
        val iAdvizeRequest: IadvizeRequest = getIadvizeRequestMessage("/request_message_text.json", conversationId)
        val expectedResponse: String = resourceAsStringMinified("/response_message_quickreply.json")

        val iadvizeReply: IadvizeReply =
            IadvizeMessage(TextPayload("MARCUS"), mutableListOf(QuickReply("MARCUS_YES"), QuickReply("MARCUS_NO")))
        val iadvizeConnectorMessage = IadvizeConnectorMessage(iadvizeReply)

        val action = SendSentence(
            PlayerId("MockPlayerId"),
            "applicationId",
            PlayerId("recipientId"),
            text = null,
            messages = mutableListOf(iadvizeConnectorMessage)
        )
        val connectorData = slot<ConnectorData>()
        every { controller.handle(any(), capture(connectorData)) } answers {
            val callback = connectorData.captured.callback as IadvizeConnectorCallback
            callback.addAction(action, 0)
            callback.eventAnswered(action)
        }

        connector.handleRequest(
            controller,
            context,
            iAdvizeRequest
        )

        verify { controller.handle(any(), any()) }

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)
    }

    @Test
    fun handleRequestWithIadvizeTransfer_shouldHandleWell_MessageTransfer() {
        val iAdvizeRequest: IadvizeRequest = getIadvizeRequestMessage("/request_message_text.json", conversationId)
        val expectedResponse: String = resourceAsStringMinified("/response_message_transfer.json")

        val iadvizeTransfer: IadvizeReply = IadvizeTransfer(0)
        val iadvizeConnectorMessage = IadvizeConnectorMessage(iadvizeTransfer)

        val action = SendSentence(
            PlayerId("MockPlayerId"),
            "applicationId",
            PlayerId("recipientId"),
            text = null,
            messages = mutableListOf(iadvizeConnectorMessage)
        )
        val connectorData = slot<ConnectorData>()

        every { iadvizeGraphQLClient.isAvailable(distributionRule) } returns true
        every { controller.handle(any(), capture(connectorData)) } answers {
            val callback = connectorData.captured.callback as IadvizeConnectorCallback
            callback.iadvizeGraphQLClient = iadvizeGraphQLClient
            callback.addAction(action, 0)
            callback.eventAnswered(action)

        }

        connector.handleRequest(
            controller,
            context,
            iAdvizeRequest
        )

        verify { controller.handle(any(), any()) }

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)
    }

    @Test
    fun handleRequestWithIadvizeTransfer_shouldHandleWell_MessageUnavailable() {
        val iAdvizeRequest: IadvizeRequest = getIadvizeRequestMessage("/request_message_text.json", conversationId)
        val expectedResponse: String = resourceAsStringMinified("/response_message_unavailable.json")

        val iadvizeTransfer: IadvizeReply = IadvizeTransfer(0)
        val iadvizeConnectorMessage = IadvizeConnectorMessage(iadvizeTransfer)

        val action = SendSentence(
            PlayerId("MockPlayerId"),
            "applicationId",
            PlayerId("recipientId"),
            text = null,
            messages = mutableListOf(iadvizeConnectorMessage)
        )
        val connectorData = slot<ConnectorData>()
        every { iadvizeGraphQLClient.isAvailable(distributionRule) } returns false

        every { controller.handle(any(), capture(connectorData)) } answers {
            val callback = connectorData.captured.callback as IadvizeConnectorCallback
            callback.iadvizeGraphQLClient = iadvizeGraphQLClient
            callback.addAction(action, 0)
            callback.eventAnswered(action)
        }

        connector.handleRequest(
            controller,
            context,
            iAdvizeRequest
        )

        verify { controller.handle(any(), any()) }

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)
    }

    @Test
    fun handleRequestWithIadvizeMultipartMessage_shouldHandleWell_MessageMultipartTransfer() {
        val iAdvizeRequest: IadvizeRequest = getIadvizeRequestMessage("/request_message_text.json", conversationId)
        val expectedResponse: String = resourceAsStringMinified("/response_message_multipart_transfer.json")

        val iadvizeMultipartReply = IadvizeMultipartReply(
            IadvizeAwait(Duration(100, millis)),
            IadvizeMessage("message info"),
            IadvizeTransfer(Duration(20, seconds)),
            IadvizeMessage("message after timeout"),
            IadvizeClose()
        )
        val iadvizeConnectorMessage = IadvizeConnectorMessage(iadvizeMultipartReply)

        val action = SendSentence(
            PlayerId("MockPlayerId"),
            "applicationId",
            PlayerId("recipientId"),
            text = null,
            messages = mutableListOf(iadvizeConnectorMessage)
        )

        val connectorData = slot<ConnectorData>()

        every { iadvizeGraphQLClient.isAvailable(distributionRule) } returns true

        every { controller.handle(any(), capture(connectorData)) } answers {
            val callback = connectorData.captured.callback as IadvizeConnectorCallback
            callback.iadvizeGraphQLClient = iadvizeGraphQLClient
            callback.addAction(action, 0)
            callback.eventAnswered(action)
        }

        connector.handleRequest(
            controller,
            context,
            iAdvizeRequest
        )

        verify { controller.handle(any(), any()) }

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)
    }

    @Test
    fun handlerGetBots_shouldHandleWell_TockBot() {
        val expectedResponse: String = resourceAsStringMinified("/response_get_bots.json")

        connector.handlerGetBots(context, controller)

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)
    }

    @Test
    fun handlerGetBot_shouldHandleWell_TockBot() {
        val expectedResponse: String = resourceAsStringMinified("/response_get_bot.json")

        connector.handlerGetBot(context, controller)

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)
    }

    @Test
    fun handlerUpdateBot_shouldHandleWell_TockBot() {
        val expectedResponse: String = resourceAsStringMinified("/response_get_bot.json")

        connector.handlerUpdateBot(context, controller)

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)
    }

    @Test
    fun handlerStrategies_shouldHandleWell_TockBot() {
        val expectedResponse: String = resourceAsStringMinified("/response_strategy.json")

        connector.handlerStrategies(context, controller)

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)
    }

    @Test
    fun handlerFirstMessage_shouldHandleWell_TockBot() {
        val expectedResponse: String = resourceAsStringMinified("/response_first_message.json")

        connector.handlerFirstMessage(context, controller)

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)
    }

    @Test
    fun handlerStartConversation_shouldHandleWell_TockBot() {
        val request: String = resourceAsStringMinified("/request_history.json")
        val expectedResponse: String = resourceAsStringMinified("/response_start_conversation.json")
        every { context.body().asString() } returns request

        connector.handlerStartConversation(context, controller)

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)
    }

    @Test
    fun handlerConversation_shouldHandleWell_TockBot() {
        val request: String = resourceAsStringMinified("/request_message_text.json")
        val expectedResponse: String = resourceAsStringMinified("/response_message_marcus.json")
        every { context.body().asString() } returns request

        val action1 = SendSentence(PlayerId("MockPlayerId"), "applicationId", PlayerId("recipientId"), "MARCUS1")
        val action2 = SendSentence(PlayerId("MockPlayerId"), "applicationId", PlayerId("recipientId"), "MARCUS2")
        val connectorData = slot<ConnectorData>()
        every { controller.handle(any(), capture(connectorData)) } answers {
            val callback = connectorData.captured.callback as IadvizeConnectorCallback
            callback.addAction(action1, 0)
            callback.addAction(action2, 0)
            callback.eventAnswered(action2)
        }

        connector.handlerConversation(context, controller)

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)
    }

    @Test
    fun handlerEchoConversation_shouldHandleWell_TockBot() {
        val request: String = resourceAsStringMinified("/request_message_text.json")
        val requestEchoMarcus1: String = resourceAsStringMinified("/request_echo_marcus1.json")
        val requestEchoMarcus2: String = resourceAsStringMinified("/request_echo_marcus2.json")
        val expectedResponse: String = resourceAsStringMinified("/response_message_marcus.json")
        every { context.body().asString() } returns request

        val action1 = SendSentence(PlayerId("MockPlayerId"), "applicationId", PlayerId("recipientId"), "MARCUS1")
        val action2 = SendSentence(PlayerId("MockPlayerId"), "applicationId", PlayerId("recipientId"), "MARCUS2")
        val connectorData = slot<ConnectorData>()
        every { controller.handle(any(), capture(connectorData)) } answers {
            val callback = connectorData.captured.callback as IadvizeConnectorCallback
            callback.addAction(action1, 0)
            callback.addAction(action2, 0)
            callback.eventAnswered(action2)
        }

        connector.handlerConversation(context, controller)

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)

        //echo1
        every { context.body().asString() } returns requestEchoMarcus1
        connector.handlerConversation(context, controller)
        verify { response.end() }

        //echo2
        every { context.body().asString() } returns requestEchoMarcus2
        connector.handlerConversation(context, controller)
        verify { response.end() }
    }

    @Test
    fun handlerConversationUnsupport_shouldDontCrash_TockBot() {
        val request: String = resourceAsStringMinified("/request_message_unsupport.json")
        val expectedResponse: String = resourceAsStringMinified("/response_message_unsupport.json")
        every { context.body().asString() } returns request

        val properties: Properties = loadProperties("/iadvize.properties")
        val messageUnsupported: String = properties.getProperty("tock_iadvize_unsupported_message_request")
        val unsupportedAnswer = I18nLabelValue("", "", "", messageUnsupported)
        every { translator.translate(messageUnsupported) } returns unsupportedAnswer.raw

        connector.handlerConversation(context, controller)

        val messageResponse = slot<String>()
        verify { response.end(capture(messageResponse)) }
        assertEquals(expectedResponse, messageResponse.captured)

    }

    private fun getIadvizeRequestMessage(json: String, idConversation: String): IadvizeRequest {
        val messageRequestJson: MessageRequestJson = mapper.readValue(
            resourceAsString(json),
            MessageRequestJson::class.java
        )
        return MessageRequest(messageRequestJson, idConversation)
    }

    private fun resourceAsStringMinified(path: String): String {
        val text = resourceAsString(path)
        return (ObjectMapper().readTree(text) as JsonNode).toString()
    }

}
