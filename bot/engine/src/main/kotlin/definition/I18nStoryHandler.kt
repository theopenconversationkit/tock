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

package ai.tock.bot.definition

import ai.tock.shared.InternalTockApi
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.I18nLocalizedLabel

/**
 * A [StoryHandler] with i18n utilities.
 *
 * Story handlers should usually not directly extend this class,
 * but instead extend [StoryHandlerBase] or [AsyncStoryHandlerBase].
 *
 * @see StoryHandlerBase
 * @see AsyncStoryHandlerBase
 */
interface I18nStoryHandler : StoryHandler, I18nKeyProvider {
    fun i18nKey(
        key: String,
        defaultLabel: CharSequence,
        vararg args: Any?,
    ): I18nLabelValue

    fun i18nKey(
        key: String,
        defaultLabel: CharSequence,
        defaultI18n: Set<I18nLocalizedLabel>,
        vararg args: Any?,
    ): I18nLabelValue

    var i18nNamespace: String
        @InternalTockApi set // should never be set by consumer code
}
