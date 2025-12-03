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

package ai.tock.bot.connector.ga.model.request

import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import mu.KotlinLogging
import java.util.Locale

data class GAUser(
    val profile: GAUserProfile? = null,
    val accessToken: String? = null,
    val permissions: Set<GAPermission>? = null,
    val userVerificationStatus: GAUserVerificationStatus = GAUserVerificationStatus.VERIFIED,
    val locale: String = defaultLocale.toLanguageTag(),
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun findLocale(): Locale =
        try {
            Locale.forLanguageTag(locale)
        } catch (e: Exception) {
            logger.error(e)
            defaultLocale
        }
}
