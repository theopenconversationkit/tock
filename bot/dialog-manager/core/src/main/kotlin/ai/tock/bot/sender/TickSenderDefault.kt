/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.sender

/**
 * Default implementation of [TickSenderDefault]
 * - It uses a standard output to show messages sent
 * - It stores all messages in history
 * - Useful for testing
 */
class TickSenderDefault : TickSender {

    private val history = mutableListOf<String>()

    override fun sendById(id: String) {
        addMessage(SEND, id, ID)
    }

    override fun endById(id: String) {
        addMessage(END, id, ID)
    }

    override fun sendPlainText(text: String) {
        addMessage(SEND, text, TEXT)
    }

    override fun endPlainText(text: String) {
        addMessage(END, text, TEXT)
    }

    override fun end() {
        addMessage(END, "", TEXT)
    }

    private fun addMessage(operation: String, element: String, type: String) {
        history.add("$operation message [$type : $element]")
    }

    fun getHistory() = history.toList()

    companion object {
        private const val SEND = "Send"
        private const val END = "End"
        private const val TEXT = "TEXT"
        private const val ID = "ID"
    }
}