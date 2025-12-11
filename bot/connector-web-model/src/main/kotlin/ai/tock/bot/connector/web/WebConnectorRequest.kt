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

package ai.tock.bot.connector.web

import java.util.Locale

data class FeedbackParams(
    val actionId: String,
    val vote: String? = null,
)

interface WebConnectorRequestContract {
    val query: String?
    val payload: String?
    val userId: String
    val locale: Locale
    val ref: String?
    val connectorId: String?
    val returnsHistory: Boolean get() = false
    val sourceWithContent: Boolean get() = false
    val streamedResponse: Boolean get() = false
    val feedback: FeedbackParams?
}

data class WebConnectorRequestContent(
    override val query: String? = null,
    override val payload: String? = null,
    override val userId: String,
    override val locale: Locale,
    override val ref: String? = null,
    override val connectorId: String? = null,
    override val returnsHistory: Boolean = false,
    override val sourceWithContent: Boolean = false,
    override val streamedResponse: Boolean = false,
    override val feedback: FeedbackParams? = null,
) : WebConnectorRequestContract
