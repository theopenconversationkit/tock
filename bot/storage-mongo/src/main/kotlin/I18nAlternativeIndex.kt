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

package ai.tock.bot.mongo

import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import java.time.Instant
import java.util.Locale

@Data(internal = true)
@JacksonData(internal = true)
internal data class I18nAlternativeIndex(
    val labelId: Id<I18nLabel>,
    val namespace: String,
    val locale: Locale,
    val interfaceType: UserInterfaceType,
    val connectorId: String?,
    val contextId: String,
    val index: Int,
    val date: Instant = Instant.now(),
) {
    constructor(
        label: I18nLabel,
        localized: I18nLocalizedLabel,
        alternativeIndex: Int,
        contextId: String,
    ) : this(
        label._id,
        label.namespace,
        localized.locale,
        localized.interfaceType,
        localized.connectorId,
        contextId,
        alternativeIndex,
    )
}
