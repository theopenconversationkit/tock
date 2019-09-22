package fr.vsct.tock.bot.connector.ga

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import fr.vsct.tock.bot.connector.ga.GAAccountLinking.Companion.getUserId
import fr.vsct.tock.bot.connector.ga.GAAccountLinking.Companion.isUserAuthenticated
import fr.vsct.tock.bot.connector.ga.GAAccountLinking.Companion.switchTimeLine
import fr.vsct.tock.bot.connector.ga.model.request.GAConversation
import fr.vsct.tock.bot.connector.ga.model.request.GARequest
import fr.vsct.tock.bot.connector.ga.model.request.GASurface
import fr.vsct.tock.bot.connector.ga.model.request.GAUser
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.engine.user.UserState
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.tockInternalInjector
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
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
    fun `GIVEN new userId THEN new timeline is created with previous params and dialog`() {
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

        every {
            userTimelineDAO.loadWithLastValidDialog(
                previousUserId,
                storyDefinitionProvider = any()
            )
        } returns previousTimeline

        val capturedTimeline = slot<UserTimeline>()
        every {
            userTimelineDAO.save(any())
        } answers {}

        val controller: ConnectorController = mockk()
        every { controller.storyDefinitionLoader() } returns { mockk() }

        switchTimeLine(newUserId, previousUserId, controller)

        verify { userTimelineDAO.save(capture(capturedTimeline)) }
        assertEquals(newUserId, capturedTimeline.captured.playerId)
        assertEquals(previousUserPreferences, capturedTimeline.captured.userPreferences)
        assertEquals(previousUserState, capturedTimeline.captured.userState)
        assertEquals(newUserId, capturedTimeline.captured.dialogs.first().playerIds.first())
    }
}