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

package ai.tock.bot.connector.web.send

interface WebMessageContract {
    val actionId: String
    val text: String?
    val footnotes: List<Footnote>
    val buttons: List<Button>
    val deepLink: WebDeepLink?
    val card: WebCard?
    val carousel: WebCarousel?
    val widget: WebWidget?
    val image: WebImage?
    val version: String
}

data class WebMessageContent(
    override val actionId: String,
    override val text: String? = null,
    override val buttons: List<Button> = emptyList(),
    override val deepLink: WebDeepLink? = null,
    override val card: WebCard? = null,
    override val carousel: WebCarousel? = null,
    override val widget: WebWidget? = null,
    override val image: WebImage? = null,
    override val version: String = "1",
    override val footnotes: List<Footnote> = emptyList(),
) : WebMessageContract
