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

    class ActionSender {
        fun send(a: Action) {
        }
    }

    @Test
    fun `preserve the order in which messages are sent when the first action is slower than the following`() {
        val queue = ConnectorQueue(executor)
        val action1a = `given action`("user1")
        val action2a = `given action`("user1")
        val action3a = `given action`("user1")
        val action1b = `given action`("user1")
        val action2b = `given action`("user1")
        val action3b = `given action`("user1")

        val send = spyk(ActionSender())
        send.send(action1a)
        queue.add(action1b, 0) { action -> Thread.sleep(1000); send.send(action); }
        send.send(action2a)
        queue.add(action2b, 0) { action -> Thread.sleep(500); send.send(action); }
        send.send(action3a)
        queue.add(action3b, 0, send::send)
        verify(timeout = 5000, ordering = Ordering.SEQUENCE) {
            send.send(action1a)
            send.send(action2a)
            send.send(action3a)
            send.send(action1b)
            send.send(action2b)
            send.send(action3b)
        }
    }

    @Test
    fun `preserve the order in which messages are sent when the first action preparation is slower than the following`() {
        val queue = ConnectorQueue(executor)
        val action1a = `given action`("user1")
        val action2a = `given action`("user1")
        val action3a = `given action`("user1")
        val action1b = `given action`("user1")
        val action2b = `given action`("user1")
        val action3b = `given action`("user1")

        val send = spyk(ActionSender())
        val prepare = spyk<(Action) -> Action>({ it }, name = "prepare")
        send.send(prepare(action1a))
        queue.add(action1b, 0, { Thread.sleep(1000); prepare(it) }, send::send)
        send.send(prepare(action2a))
        queue.add(action2b, 0, { Thread.sleep(500); prepare(it) }, send::send)
        send.send(prepare(action3a))
        queue.add(action3b, 0, { prepare(it) }, send::send)
        verify(timeout = 5000, ordering = Ordering.SEQUENCE) {
            prepare(action1a)
            send.send(action1a)
            prepare(action2a)
            send.send(action2a)
            prepare(action3a)
            send.send(action3a)
            // Prepare doesn't wait
            prepare(action3b)
            prepare(action2b)
            prepare(action1b)
            // send waits for prepare and previous send
            send.send(action1b)
            send.send(action2b)
            send.send(action3b)
        }
    }

    @Test
    fun `each user has their own queue which keeps the order in which messages are sent`() {
        val queue = ConnectorQueue(executor)
        val user1Action1 = `given action`("user1")
        val user1Action2 = `given action`("user1")
        val user2Action1 = `given action`("user2")
        val user2Action2 = `given action`("user2")
        val send = spyk(ActionSender())
        queue.add(user1Action1, 0) { action -> Thread.sleep(1500); send.send(action); }
        queue.add(user1Action2, 0) { action -> Thread.sleep(1000); send.send(action); }
        queue.add(user2Action1, 0) { action -> Thread.sleep(500); send.send(action); }
        queue.add(user2Action2, 0) { action -> Thread.sleep(0); send.send(action); }
        verify(timeout = 5000, ordering = Ordering.SEQUENCE) {
            send.send(user2Action1)
            send.send(user2Action2)
            send.send(user1Action1)
            send.send(user1Action2)
        }
    }
}

private fun `given action`(recipientId: String): Action {
    val action = mockk<Action>(relaxed = true)
    every {
        action.recipientId
    } returns PlayerId(recipientId)
    return action
}
