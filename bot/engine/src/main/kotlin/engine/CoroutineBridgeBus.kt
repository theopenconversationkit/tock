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

package ai.tock.bot.engine

import ai.tock.bot.definition.AsyncStoryHandler
import ai.tock.bot.engine.action.Action
import ai.tock.shared.Executor
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.shared.injector
import ai.tock.shared.provide
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@ExperimentalTockCoroutines
internal interface CoroutineBridgeBus: BotBus {
    val customActionSender: AtomicReference<((Action, Long) -> Unit)?>
    val coroutineScope: AtomicReference<CoroutineScope>

    fun handleAsyncStory(storyHandler: AsyncStoryHandler): Boolean {
        return handleAsyncStory(storyHandler::handle)
    }

    fun handleAsyncStory(op: suspend (AsyncBotBus) -> Unit): Boolean {
        coroutineScope.get()?.run {
            val asyncBus = coroutineContext[AsyncBotBus.Ref]?.bus
            if (asyncBus != null) {
                launch {
                    op(asyncBus)
                }
            }
            return true
        }
        return false
    }

    fun doSend(actionToSend: Action, delay: Long)

    /**
     * @return a callback to force-close the message queue
     */
    fun deferMessageSending(scope: CoroutineScope): () -> Unit {
        data class QueuedAction(val action: Action, val delay: Long)

        val messageChannel = Channel<QueuedAction>(Channel.BUFFERED)
        customActionSender.set { action, delay ->
            // we queue in the current thread to preserve message ordering
            scope.launch(start = CoroutineStart.UNDISPATCHED) {
                messageChannel.send(QueuedAction(action, delay))
                // the following code may happen in a different thread if the channel's buffer was full
                if (action.metadata.lastAnswer) {
                    messageChannel.close()
                }
            }
        }
        scope.launch(injector.provide<Executor>().asCoroutineDispatcher()) {
            for ((action, delay) in messageChannel) {
                doSend(action, delay)
            }
        }
        return { messageChannel.close() }
    }
}
