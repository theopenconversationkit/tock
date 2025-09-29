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

import ai.tock.bot.connector.ga.GAAccountLinking.Companion.getUserId
import ai.tock.bot.connector.ga.GAAccountLinking.Companion.isUserAuthenticated
import ai.tock.bot.connector.ga.GAAccountLinking.Companion.switchTimeLine
import ai.tock.bot.connector.ga.model.request.GAConversation
import ai.tock.bot.connector.ga.model.request.GARequest
import ai.tock.bot.connector.ga.model.request.GASurface
import ai.tock.bot.connector.ga.model.request.GAUser
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.engine.user.UserState
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GAAccountLinkingTest {

    private val userTimelineDAO: UserTimelineDAO = mockk()

    init {
        tockInternalInjector = KodeinInjector().apply {
            inject(
                Kodein {
                    bind<UserTimelineDAO>() with singleton { userTimelineDAO }
                }
            )
        }
    }

    private val connectedUserRequest = GARequest(
        GAUser(accessToken = "userId|token"),
        surface = GASurface(emptyList()),
        conversation = GAConversation(
            "conversationId"
        ),
        inputs = emptyList(),
        device = null,
        availableSurfaces = emptyList()
    )

    private val notConnectedUserRequest = GARequest(
        GAUser(),
        surface = GASurface(emptyList()),
        conversation = GAConversation(
            "conversationId"
        ),
        inputs = emptyList(),
        device = null,
        availableSurfaces = emptyList()
    )

    @Test
    fun `GIVEN access token THEN user is authenticated`() {
        assertTrue(
            isUserAuthenticated(
                connectedUserRequest
            )
        )
    }

    @Test
    fun `GIVEN no access token THEN user is not authenticated`() {
        assertFalse(
            isUserAuthenticated(
                notConnectedUserRequest
            )
        )
    }

    @Test
    fun `GIVEN access token THEN userId is first part of access token`() {
        assertEquals(
            "userId",
            getUserId(
                connectedUserRequest
            )
        )
    }

    @Test
    fun `GIVEN no access token THEN userId is conversation id`() {
        assertEquals(
            "conversationId",
            getUserId(
                notConnectedUserRequest
            )
        )
    }

    @Test
    fun `GIVEN new userId THEN new timeline is created with previous params and dialog`() = runBlocking {
        val previousUserId = PlayerId("previousUserId")
        val previousUserPreferences = UserPreferences()
        val previousUserState = UserState(Instant.now())
        val previousDialogs = mutableListOf(
            Dialog(playerIds = setOf(previousUserId))
        )

        val previousTimeline = UserTimeline(
            previousUserId,
            dialogs = previousDialogs,
            userPreferences = previousUserPreferences,
            userState = previousUserState
        )

        val newUserId = PlayerId("newUserId")

        coEvery {
            userTimelineDAO.loadWithLastValidDialog(
                any(),
                previousUserId,
                storyDefinitionProvider = any()
            )
        } returns previousTimeline

        val capturedTimeline = slot<UserTimeline>()
        coEvery {
            userTimelineDAO.save(any(), any<BotDefinition>())
        } answers {}

        val controller: ConnectorController = mockk()
        val botDefinition: BotDefinition = mockk()
        every { controller.storyDefinitionLoader(any()) } returns { mockk() }
        every { controller.botDefinition } returns botDefinition
        every { botDefinition.namespace } returns "namespace"

        switchTimeLine("appId", newUserId, previousUserId, controller)

        coVerify { userTimelineDAO.save(capture(capturedTimeline), any<BotDefinition>()) }
        assertEquals(newUserId, capturedTimeline.captured.playerId)
        assertEquals(previousUserPreferences, capturedTimeline.captured.userPreferences)
        assertEquals(previousUserState, capturedTimeline.captured.userState)
        assertEquals(newUserId, capturedTimeline.captured.dialogs.first().playerIds.first())
    }
}
