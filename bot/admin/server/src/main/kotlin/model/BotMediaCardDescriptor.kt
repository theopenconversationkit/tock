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

import ai.tock.bot.connector.media.MediaCardDescriptor
import ai.tock.bot.connector.media.MediaFileDescriptor
import ai.tock.bot.connector.media.MediaMessageType
import ai.tock.bot.connector.media.MediaMessageType.card
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.Translator

data class BotMediaCardDescriptor(
    val title: I18nLabel?,
    val subTitle: I18nLabel?,
    val file: MediaFileDescriptor?,
    val actions: List<BotMediaActionDescriptor> = emptyList(),
    val fillCarousel: Boolean = false
) : BotMediaMessageDescriptor {

    constructor(desc: MediaCardDescriptor, readOnly: Boolean = false) :
        this(
            desc.title?.let { Translator.saveIfNotExist(it, readOnly) },
            desc.subTitle?.let { Translator.saveIfNotExist(it, readOnly) },
            desc.file,
            desc.actions.map { BotMediaActionDescriptor(it, readOnly) },
            desc.fillCarousel
        )

    override val type: MediaMessageType = card

    override fun toDescriptor(): MediaCardDescriptor =
        MediaCardDescriptor(
            title?.let { I18nLabelValue(it) },
            subTitle?.let { I18nLabelValue(it) },
            file,
            actions.map { it.toDescriptor() },
            fillCarousel
        )
}
