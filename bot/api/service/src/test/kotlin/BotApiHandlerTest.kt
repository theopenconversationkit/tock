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
import ai.tock.bot.api.model.message.bot.I18nText
import ai.tock.bot.api.model.message.bot.Sentence
import ai.tock.bot.api.model.websocket.ResponseData
import ai.tock.bot.api.service.BotApiClientController
import ai.tock.bot.api.service.BotApiDefinitionProvider
import ai.tock.bot.api.service.BotApiHandler
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.DialogState
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.nlp.api.client.model.NlpIntentQualifier
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class   BotApiHandlerTest {

    private val provider: BotApiDefinitionProvider = mockk()
    private val configuration: BotConfiguration = mockk {
        every { apiKey } returns "key"
        every { webhookUrl } returns null
    }

    private val bus: BotBus = mockk(relaxed = true) {
        every { action } returns mockk<SendSentence> {
            every { stringText } returns "user text"
            every { getBusContextValue<Set<StoryDefinition>>("_viewed_stories_tock_switch") } returns emptySet()
        }
        every { dialog } returns mockk<Dialog>{
            every { state } returns mockk<DialogState> {
                every{ nextActionState } returns mockk<NextUserActionState>{
                    every {intentsQualifiers} returns listOf(NlpIntentQualifier("intent1",0.5), NlpIntentQualifier("intent2",0.5))
                }
            }
        }

    }

    private val clientController: BotApiClientController = mockk {
        every { send(any()) } returns
                ResponseData(
                    "requestId",
                    BotResponse(
                        storyId = "storyId",
                        step = null,
                        messages = listOf(Sentence(I18nText("user text"))),
                        context = mockk()
                    )
                ) andThen
                ResponseData(
                    "requestId",
                    BotResponse(
                        storyId = "storyId",
                        step = null,
                        messages = listOf(Sentence(I18nText("user text"))),
                        context = mockk(),
                    )
                )
    }

    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO = mockk {
        every { getStoryDefinitionsByNamespaceBotIdStoryId(any(), any(), any()) } returns
                mockk {
                    every { findEnabledEndWithStoryId(any()) } returns "endWithStoryId"
                }
    }

    @BeforeEach
    fun before() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein.invoke {
                bind<StoryDefinitionConfigurationDAO>() with provider { storyDefinitionDAO }
            }
        )
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

        assertEquals(bus.dialog.state.nextActionState?.intentsQualifiers, listOf(NlpIntentQualifier("intent1",0.5), NlpIntentQualifier("intent2",0.5)))
    }
}
