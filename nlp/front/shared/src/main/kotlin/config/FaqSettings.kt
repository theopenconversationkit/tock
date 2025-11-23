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

package ai.tock.nlp.front.shared.config

import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

@JacksonData(internal = true)
data class FaqSettings(
    /**
     * The unique [Id] of the settings.
     */
    val _id: Id<FaqSettings> = newId(),
    /**
     * The application id.
     */
    val applicationId: Id<ApplicationDefinition>,
    /**
     * Is the satisfaction story is asked ?
     */
    val satisfactionEnabled: Boolean = false,
    /**
     * The satisfaction story identifier.
     */
    val satisfactionStoryId: String? = null,
    /**
     * Faq creation date
     */
    val creationDate: Instant,
    /**
     * Faq update date
     */
    val updateDate: Instant,
) {
    fun toFaqSettingsQuery(): FaqSettingsQuery {
        return FaqSettingsQuery(satisfactionEnabled, satisfactionStoryId)
    }
}
