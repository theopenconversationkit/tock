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

package ai.tock.bot.connector.media

import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.GenericMessage

/**
 * A [MediaMessage] action.
 */
data class MediaAction(val title: CharSequence, var url: String? = null) : MediaMessage {
    override fun toGenericMessage(): GenericMessage? = GenericMessage(choices = listOf(toChoice()))

    internal fun toChoice(): Choice =
        if (url == null) {
            Choice.fromText(title.toString())
        } else {
            Choice(
                SendChoice.EXIT_INTENT,
                mapOf(
                    SendChoice.URL_PARAMETER to url!!,
                    SendChoice.TITLE_PARAMETER to title.toString(),
                ),
            )
        }
}
