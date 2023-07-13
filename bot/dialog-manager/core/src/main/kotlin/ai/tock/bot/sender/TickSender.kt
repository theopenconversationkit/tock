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
 * A tick sender
 */
interface TickSender {

    /**
     * Send bot response by answer id
     */
    fun sendById(id: String)

    /**
     * End bot response by answer id
     */
    fun endById(id: String)

    /**
     * Send bot response with a plain text
     */
    fun sendPlainText(text: String)

    /**
     * End bot response with a plain text
     */
    fun endPlainText(text: String)

    /**
     * End bot response without any message text
     */
    fun end()
}