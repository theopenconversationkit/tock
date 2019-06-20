/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.bot.engine

import fr.vsct.tock.shared.booleanProperty
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

object WebSocketController {

    private class Handler(
        var pushHandler: ((String) -> Unit)? = null,
        var receiveHandler: ((String) -> Unit)? = null)

    val websocketEnabled: Boolean = booleanProperty("tock_websocket_enabled", false)

    private val authorizedKeys: MutableSet<String> = CopyOnWriteArraySet()

    fun registerAuthorizedKey(key: String) {
        authorizedKeys.add(key)
    }

    fun isAuthorizedKey(key: String?): Boolean = key != null && authorizedKeys.contains(key)

    private val handlers: MutableMap<String, Handler> = ConcurrentHashMap()

    fun setPushHandler(id: String, handler: ((String) -> Unit)) {
        handlers.getOrPut(id, { Handler() }).pushHandler = handler
    }

    fun getPushHandler(id: String): ((String) -> Unit)? = handlers[id]?.pushHandler

    fun setReceiveHandler(id: String, handler: ((String) -> Unit)) {
        handlers.getOrPut(id, { Handler() }).receiveHandler = handler
    }

    fun getReceiveHandler(id: String): ((String) -> Unit)? = handlers[id]?.receiveHandler

    fun removeHandler(id: String) {
        handlers.remove(id)
    }
}