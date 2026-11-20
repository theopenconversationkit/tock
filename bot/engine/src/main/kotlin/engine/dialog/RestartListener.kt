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

package ai.tock.bot.engine.dialog

import ai.tock.bot.engine.BotBus
import ai.tock.shared.Loader

@Volatile
private var internalRestartListeners: List<RestartListener>? = null

internal val restartListeners: List<RestartListener>
    get() =
        internalRestartListeners
            ?: Loader.loadServices<RestartListener>().also { internalRestartListeners = it }

/**
 * Called when a user restarts the conversation (see [ai.tock.bot.engine.nlp.BuiltInKeywordListener.restartKeyword]).
 *
 * Implementations are loaded with the [java.util.ServiceLoader]: declare them in
 * `META-INF/services/ai.tock.bot.engine.dialog.RestartListener`.
 *
 * Use it to reset the custom state of the bot (for instance, data stored outside the dialog). Do not send messages:
 * the answer to the user is sent by the restart handler.
 */
interface RestartListener {
    /**
     * Called after a new [Dialog] has been started for the user.
     * [previousDialog] is the dialog of the conversation that has just been closed.
     */
    fun onRestart(
        bus: BotBus,
        previousDialog: Dialog,
    )
}
