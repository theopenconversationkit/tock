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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.ga.GAConnectorCallback.ActionWithDelay
import ai.tock.bot.connector.ga.model.request.GAConversation
import ai.tock.bot.connector.ga.model.request.GADevice
import ai.tock.bot.connector.ga.model.request.GARequest
import ai.tock.bot.connector.ga.model.request.GASurface
import ai.tock.bot.connector.ga.model.request.GAUser
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.translator.EMPTY_TRANSLATED_STRING
import ai.tock.translator.raw
import io.mockk.every
import io.mockk.mockk
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 */
class RoutingContextHolderTest {

    private val bus: BotBus = mockk(relaxed = true)
    private val context: RoutingContext = mockk(relaxed = true)
    private val controller: ConnectorController = mockk(relaxed = true)

    @BeforeEach
    fun init() {
        every { bus.translate(any<CharSequence>()) } answers { firstArg<CharSequence>().raw }
        every { bus.translateAndReturnBlankAsNull(any()) } answers {
            firstArg<CharSequence?>()?.raw ?: EMPTY_TRANSLATED_STRING
        }
        every { context.response() } returns mockk(relaxed = true)
    }

    @Test
    fun `GIVEN response with many basic cards WHEN send to ga THEN keep only the First one `() {
        with(bus) {
            val firstMessage = gaMessage("ok")
            val secondMessage = gaMessage(
                richResponse(
                    basicCard(
                        "title1",
                        null,
                        "formattedText1",
                        null,
                        gaButton("button1", "butonUrl1")
                    ),
                    listOf(
                        "suggestion1",
                        "suggestion2",
                        "suggestion3",
                        "suggestion4",
                        "suggestion5",
                        "suggestion6",
                        "suggestion7",
                        "suggestion8",
                        "suggestion9",
                        "suggestion10",
                        "suggestion11",
                        "suggestion12"
                    )
                )
            )
            val thirdMessage = gaMessage(
                richResponse(
                    basicCard(
                        "title2",
                        "subtitle2",
                        "formattedText2",
                        gaImage("url2", "acc2"),
                        gaButton("button2", "butonUrl2")
                    )
                )
            )

            val playerId = PlayerId("id1", PlayerType.user)

            val holder = GAConnectorCallback(
                "",
                controller,
                context,
                GARequest(
                    GAUser(),
                    GADevice(),
                    GASurface(emptyList()),
                    GAConversation(),
                    emptyList(),
                    false,
                    emptyList()
                ),
                listOf(firstMessage, secondMessage, thirdMessage).map {
                    ActionWithDelay(
                        SendSentence(
                            playerId,
                            "appId",
                            playerId,
                            null,
                            mutableListOf(it)
                        )
                    )
                }.toMutableList()
            )

            val result = holder.buildResponse()
            val richResponse = result.expectedInputs!!.first().inputPrompt.richInitialPrompt
            assertEquals(2, richResponse.items.size)
            val basicCard = richResponse.items[1].basicCard!!
            assertEquals("title1", basicCard.title)
            assertEquals("", basicCard.subtitle)
            assertEquals("formattedText1", basicCard.formattedText)
            assertEquals(null, basicCard.image)
            assertEquals(1, basicCard.buttons.size)
            assertEquals(gaButton("button1", "butonUrl1"), basicCard.buttons.first())
            assertEquals(8, richResponse.suggestions.size)
        }
    }
}
