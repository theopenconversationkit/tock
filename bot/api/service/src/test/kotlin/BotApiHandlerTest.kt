/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.api.model.BotResponse
import ai.tock.bot.api.model.ResponseContext
import ai.tock.bot.api.model.UserRequest
import ai.tock.bot.api.model.message.bot.I18nText
import ai.tock.bot.api.model.message.bot.Sentence
import ai.tock.bot.api.model.websocket.ResponseData
import ai.tock.bot.api.service.BotApiClientController
import ai.tock.bot.api.service.BotApiDefinitionProvider
import ai.tock.bot.api.service.BotApiHandler
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.api.service.toUserRequest
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.DialogState
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.bot.engine.user.PlayerId
import ai.tock.nlp.api.client.model.NlpIntentQualifier
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class BotApiHandlerTest {

    companion object {
        private const val TOCK_ACTIONS_HISTORY_ENABLE_PROPERTY = "tock_bot_api_actions_history_to_client_bus"
    }

    private val provider: BotApiDefinitionProvider = mockk()
    private val configuration: BotConfiguration = mockk {
        every { apiKey } returns "key"
        every { webhookUrl } returns null
    }

    private val bus: BotBus = mockk(relaxed = true) {
        every { action } returns mockk<SendSentence> {
            every { metadata } returns ActionMetadata()
            every { stringText } returns "user text"
            every { getBusContextValue<Set<StoryDefinition>>("_viewed_stories_tock_switch") } returns emptySet()
        }
        every { dialog } returns mockk<Dialog> {
            every { state } returns mockk<DialogState> {
                every { nextActionState } returns mockk<NextUserActionState> {
                    every { intentsQualifiers } returns listOf(
                        NlpIntentQualifier("intent1", 0.5),
                        NlpIntentQualifier("intent2", 0.5)
                    )
                }
            }
        }

    }

    private val clientController: BotApiClientController = mockk {
        every { send(any(), any()) } answers {
            @Suppress("UNCHECKED_CAST")
            (args[1] as (ResponseData?) -> Unit).invoke(
                ResponseData(
                    "requestId",
                    BotResponse(
                        storyId = "storyId",
                        step = null,
                        messages = listOf(Sentence(I18nText("user text"))),
                        context = ResponseContext("requestId")
                    )
                )
            )
        }
    }

    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO = mockk {
        every { getStoryDefinitionByNamespaceAndBotIdAndStoryId(any(), any(), any()) } returns
                mockk {
                    every { findEnabledEndWithStoryId(any()) } returns "endWithStoryId"
                }
    }

    private val userTimelineDAO: UserTimelineDAO = mockk {
        justRun { save(any(), any() as BotDefinition) }
    }

    @BeforeEach
    fun before() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein.invoke {
                bind<StoryDefinitionConfigurationDAO>() with provider { storyDefinitionDAO }
                bind<UserTimelineDAO>() with provider { userTimelineDAO }
            }
        )
    }

    @AfterEach
    fun cleanup() {
        clearProperties()
    }

    private fun clearProperties() {
        System.clearProperty(TOCK_ACTIONS_HISTORY_ENABLE_PROPERTY)
    }

    @Test
    fun `Configured ending story is taken into account`() {
        val handler = BotApiHandler(provider, configuration, clientController)

        handler.send(bus)

        verify { bus.setBusContextValue("_viewed_stories_tock_switch", any()) }
        verify { bus.handleAndSwitchStory(any(), any()) }
    }

    @Test
    fun `nextIntentQualifiers are taking in account`() {
        val handler = BotApiHandler(provider, configuration, clientController)

        handler.send(bus)

        assertEquals(
            bus.dialog.state.nextActionState?.intentsQualifiers,
            listOf(NlpIntentQualifier("intent1", 0.5), NlpIntentQualifier("intent2", 0.5))
        )
    }

    @Test
    fun `actions history present in bus with actions`() {
        System.setProperty(
            TOCK_ACTIONS_HISTORY_ENABLE_PROPERTY, "true"
        )

        val mockedBus: BotBus = bus.apply {
            every { bus.dialog } returns mockk {
                every { getBusContextValue<Set<StoryDefinition>>("_viewed_stories_tock_switch") } returns emptySet()
                every { dialog.allActions() } returns listOf(
                    SendSentence(PlayerId("user"), "appId", PlayerId("bot"), "user Sentence"),
                    SendSentence(PlayerId("bot"), "appId", PlayerId("user"), "bot Sentence")
                )
            }
        }

        val handler = BotApiHandler(provider, configuration, clientController)

        handler.send(mockedBus)

        val slot = slot<UserRequest>()
        verify { clientController.send(capture(slot), any()) }

        assertEquals(mockedBus.toUserRequest().context.actionsHistory, slot.captured.context.actionsHistory)
        assertEquals(mockedBus.toUserRequest().context.actionsHistory?.size, 2)
        assertTrue(mockedBus.toUserRequest().context.actionsHistory != null)
    }

    @Test
    fun `actions history property not set THEN no actions history in bus is present`() {
        System.setProperty(
            TOCK_ACTIONS_HISTORY_ENABLE_PROPERTY, "false"
        )

        val mockedBus: BotBus = bus.apply {
            every { bus.dialog } returns mockk {
                every { getBusContextValue<Set<StoryDefinition>>("_viewed_stories_tock_switch") } returns emptySet()
                every { dialog.allActions() } returns listOf(
                    SendSentence(PlayerId("user"), "appId", PlayerId("bot"), "user Sentence"),
                    SendSentence(PlayerId("bot"), "appId", PlayerId("user"), "bot Sentence")
                )
            }
        }

        val handler = BotApiHandler(provider, configuration, clientController)

        handler.send(mockedBus)

        val slot = slot<UserRequest>()
        verify { clientController.send(capture(slot), any()) }

        assertEquals(null, slot.captured.context.actionsHistory)
        assertEquals(mockedBus.toUserRequest().context.actionsHistory?.size, null)
        assertTrue(mockedBus.toUserRequest().context.actionsHistory == null)
    }

    @Test
    fun `actions history property set and no actions history in bus`() {
        System.setProperty(
            TOCK_ACTIONS_HISTORY_ENABLE_PROPERTY, "true"
        )

        val mockedBus: BotBus = bus.apply {
            every { bus.dialog } returns mockk {
                every { getBusContextValue<Set<StoryDefinition>>("_viewed_stories_tock_switch") } returns emptySet()
                every { dialog.allActions() } returns emptyList()
            }
        }

        val handler = BotApiHandler(provider, configuration, clientController)

        handler.send(mockedBus)

        val slot = slot<UserRequest>()
        verify { clientController.send(capture(slot), any()) }

        assertEquals(emptyList(), slot.captured.context.actionsHistory)
        assertEquals(0, mockedBus.toUserRequest().context.actionsHistory?.size)
        assertTrue(mockedBus.toUserRequest().context.actionsHistory != null)
    }


}
