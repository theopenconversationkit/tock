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

import ai.tock.shared.Executor
import ai.tock.shared.booleanProperty
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.vertx
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Internal object used to manage websocket events.
 */
object WebSocketController {

    private class Handler(
        var pushHandler: ((String) -> Unit)? = null,
        var receiveHandler: ((String) -> Unit)? = null
    )

    /**
     * Is websocket enabled ?
     */
    val websocketEnabled: Boolean = booleanProperty("tock_websocket_enabled", false)

    private val authorizedKeys: MutableSet<String> = CopyOnWriteArraySet()

    private val executor: Executor get() = injector.provide()

    fun registerAuthorizedKey(key: String) {
        authorizedKeys.add(key)
    }

    internal fun isAuthorizedKey(key: String?): Boolean = key != null && authorizedKeys.contains(key)

    private val handlers: MutableMap<String, Handler> = ConcurrentHashMap()

    internal fun setPushHandler(id: String, handler: ((String) -> Unit)) {
        val context = vertx.orCreateContext
        handlers.getOrPut(id, { Handler() }).pushHandler = { content ->
            context.runOnContext {
                handler(content)
            }
        }
    }

    fun getPushHandler(id: String): ((String) -> Unit)? = handlers[id]?.pushHandler

    fun setReceiveHandler(id: String, handler: ((String) -> Unit)) {
        handlers.getOrPut(id, { Handler() }).receiveHandler = {
            executor.executeBlocking {
                handler(it)
            }
        }
    }

    fun getReceiveHandler(id: String): ((String) -> Unit)? = handlers[id]?.receiveHandler

    internal fun removePushHandler(id: String) {
        handlers[id]?.pushHandler = null
    }
}
