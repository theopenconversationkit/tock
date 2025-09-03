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

import ai.tock.bot.definition.AsyncStoryDefinition
import ai.tock.bot.definition.AsyncStoryHandler
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.action.Action
import ai.tock.shared.Executor
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.shared.injector
import ai.tock.shared.longProperty
import ai.tock.shared.provide
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import mu.KotlinLogging

@ExperimentalTockCoroutines
internal abstract class CoroutineBridgeBus: BotBus {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val defaultMaxWaitMillis = longProperty("tock_bot_story_switch_max_wait_ms", 9000L)
    }

    val coroutineScope = AtomicReference<CoroutineScope>()
    protected val customActionSender = AtomicReference<((Action, Long) -> Unit)?>()
    internal var maxWaitMillis = defaultMaxWaitMillis

    fun handleAsyncStory(storyHandler: AsyncStoryHandler, callingStoryId: String): Boolean {
        return handleAsyncStory(callingStoryId, storyHandler::handle)
    }

    @Suppress("DEPRECATION")
    override fun handleAndSwitchStory(
        storyDefinition: StoryDefinition,
        starterIntent: Intent
    ) {
        if (storyDefinition is AsyncStoryDefinition) {
            handleAndSwitchStory(storyDefinition, starterIntent)
        } else {
            super.handleAndSwitchStory(storyDefinition, starterIntent)
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Do not switch to an AsyncStoryDefinition from a synchronous story")
    @ExperimentalTockCoroutines
    override fun handleAndSwitchStory(
        storyDefinition: AsyncStoryDefinition,
        starterIntent: Intent
    ) {
        val currentStoryId = story.definition.id
        switchStory(storyDefinition, starterIntent)
        hasCurrentSwitchStoryProcess = false
        val storyHandler = storyDefinition.storyHandler

        if (!handleAsyncStory(storyHandler, currentStoryId)) {
            storyHandler.handle(this)
        }
    }

    fun handleAsyncStory(callingStoryId: String, op: suspend (AsyncBotBus) -> Unit): Boolean {
        coroutineScope.get()?.run {
            val asyncBus = coroutineContext[AsyncBotBus.Ref]?.bus
            if (asyncBus != null) {
                val lock = Object()
                val done = AtomicBoolean()
                launch {
                    op(asyncBus)
                    synchronized(lock) {
                        done.set(true)
                        lock.notify()
                    }
                }
                synchronized(lock) {
                    // Relinquish the thread if we are waiting too long
                    // (we take the risk of race conditions over the risk of deadlock)
                    lock.wait(maxWaitMillis)
                    if (!done.get()) {
                        logger.warn { "Timed out waiting for async story ${story.definition.id} from $callingStoryId" }
                    }
                }
            }
            return true
        }
        return false
    }

    abstract fun doSend(actionToSend: Action, delay: Long)

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
