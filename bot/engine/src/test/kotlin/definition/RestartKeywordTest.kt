package ai.tock.bot.definition

import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotEngineTest
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.RestartListener
import ai.tock.bot.engine.nlp.BuiltInKeywordListener
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class RestartKeywordTest : BotEngineTest() {
    class TestRestartListener : RestartListener {
        companion object {
            val calls = mutableListOf<Pair<BotBus, Dialog>>()
        }

        override fun onRestart(
            bus: BotBus,
            previousDialog: Dialog,
        ) {
            calls.add(bus to previousDialog)
        }
    }

    @Test
    fun `restart keyword is a built-in keyword`() {
        assertEquals(Intent.keyword, BuiltInKeywordListener.handleKeyword("_restart_"))
    }

    @Test
    fun `GIVEN a conversation WHEN restarting THEN a new dialog is started and the user is kept`() {
        TestRestartListener.calls.clear()
        val previousDialog = bus.dialog
        bus.userPreferences.firstName = "Marie"
        bus.userTimeline.userState.setFlag("eula", Duration.ofDays(1), "accepted")
        val dialogCount = userTimeline.dialogs.size

        BotDefinitionBase.restartKeywordHandler(bus)

        assertEquals(dialogCount + 1, userTimeline.dialogs.size)
        val newDialog = userTimeline.currentDialog!!
        assertNotSame(previousDialog, newDialog)
        assertTrue(newDialog.stories.isEmpty())
        assertNull(newDialog.state.currentIntent)
        assertTrue(newDialog.state.entityValues.isEmpty())
        assertEquals(setOf(userId, botId), newDialog.playerIds)
        // the user is kept
        assertEquals("Marie", userTimeline.userPreferences.firstName)
        assertTrue(userTimeline.userState.flags.containsKey("eula"))
        // the listeners are called with the previous dialog
        assertEquals(1, TestRestartListener.calls.size)
        assertSame(previousDialog, TestRestartListener.calls.first().second)
        // the answer ends the previous conversation
        assertTrue(
            previousDialog
                .allActions()
                .last()
                .metadata.lastAnswer,
        )
    }
}
