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

package ai.tock.bot.engine.message

/**
 * A sub element of [GenericMessage].
 */
data class GenericElement(
    val attachments: List<Attachment> = emptyList(),
    val choices: List<Choice> = emptyList(),
    val texts: Map<String, String> = emptyMap(),
    val locations: List<Location> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
) {

    /**
     * Transforms a [GenericMessage] into a [GenericElement].
     */
    constructor(message: GenericMessage) : this(
        message.attachments,
        message.choices,
        message.texts,
        message.locations,
        message.metadata
    )
}
