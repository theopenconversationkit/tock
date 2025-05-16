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

package ai.tock.bot.engine.action

/**
 * Footnote to refer to a source document
 * Basic format: {identifier}. {title} {link:url},
 */
data class Footnote(
    /**
     * A footnote identifier
     */
    val identifier: CharSequence,
    /**
     * A footnote title
     */
    val title: CharSequence,
    /**
     * A footnote link
     */
    val url: String?,
    /**
     * A footnote content
     */
    val content: String?,
    /**
     * A footnote score
     */
    val score: Float?,
)
