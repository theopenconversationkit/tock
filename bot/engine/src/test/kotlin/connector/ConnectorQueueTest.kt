package ai.tock.bot.connector

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.user.PlayerId
import ai.tock.shared.SimpleExecutor
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class ConnectorQueueTest {

    private val executor = SimpleExecutor(10)

    @Test
    fun `preserve the order in which messages are sent when the first action is slower than the following`() {
        val queue = ConnectorQueue(executor)
        val action1a = `given action`("user1")
        val action2a = `given action`("user1")
        val action3a = `given action`("user1")
        val action1b = `given action`("user1")
        val action2b = `given action`("user1")
        val action3b = `given action`("user1")
        val send = spyk({ action: Action -> })
        send(action1a)
        queue.add(action1b, 0, { action -> Thread.sleep(1000); send(action); })
        send(action2a)
        queue.add(action2b, 0, { action -> Thread.sleep(500); send(action); })
        send(action3a)
        queue.add(action3b, 0, send)
        verify(timeout = 5000, ordering = Ordering.SEQUENCE) {
            send(action1a)
            send(action2a)
            send(action3a)
            send(action1b)
            send(action2b)
            send(action3b)
        }
    }

    @Test
    fun `each user has their own queue which keeps the order in which messages are sent`() {
        val queue = ConnectorQueue(executor)
        val user1Action1 = `given action`("user1")
        val user1Action2 = `given action`("user1")
        val user2Action1 = `given action`("user2")
        val user2Action2 = `given action`("user2")
        val send = spyk({ action: Action -> })
        queue.add(user1Action1, 0, { action -> Thread.sleep(1500); send(action); })
        queue.add(user1Action2, 0, { action -> Thread.sleep(1000); send(action); })
        queue.add(user2Action1, 0, { action -> Thread.sleep(500); send(action); })
        queue.add(user2Action2, 0, { action -> Thread.sleep(0); send(action); })
        verify(timeout = 5000, ordering = Ordering.SEQUENCE) {
            send(user2Action1)
            send(user2Action2)
            send(user1Action1)
            send(user1Action2)
        }
    }

    private fun `given action`(recipientId: String): Action {
        val action = mockk<Action>(relaxed = true)
        every {
            action.recipientId
        } returns PlayerId(recipientId)
        return action
    }
}