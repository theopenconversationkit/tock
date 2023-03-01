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

package ai.tock.bot.connector.web

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence

internal class WebMessageProcessor(private val processMarkdown: Boolean) {

    fun process(action: Action): WebMessage? {
        return if (action is SendSentence) {
            val stringText = action.stringText

            if (stringText != null) {
                WebMessage(postProcess(stringText))
            } else {
                postProcess(action.message(webConnectorType) as? WebMessage)
            }
        } else {
            null
        }
    }

    private fun postProcess(message: WebMessage?): WebMessage? {
        if (message?.text != null) {
            return message.copy(text = postProcess(message.text))
        }

        return message
    }

    private fun postProcess(text: String): String {
        if (processMarkdown) {
            return WebMarkdown.markdown(text)
        }

        return text
    }
}
