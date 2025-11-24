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

package ai.tock.bot.admin.model

import ai.tock.bot.connector.media.MediaActionDescriptor
import ai.tock.bot.connector.media.MediaMessageType
import ai.tock.bot.connector.media.MediaMessageType.action
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.Translator

data class BotMediaActionDescriptor(val title: I18nLabel, var url: String? = null) : BotMediaMessageDescriptor {
    constructor(desc: MediaActionDescriptor, readOnly: Boolean = false) : this(Translator.saveIfNotExist(desc.title, readOnly), desc.url)

    override val type: MediaMessageType = action

    override fun toDescriptor(): MediaActionDescriptor = MediaActionDescriptor(I18nLabelValue(title), url.takeUnless { it.isNullOrBlank() })
}
