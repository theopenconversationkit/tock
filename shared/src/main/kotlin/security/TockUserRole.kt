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

package ai.tock.shared.security

import mu.KotlinLogging

/**
 * The user roles for [TockUser].
 */
enum class TockUserRole {

    /**
     * A nlp user is allowed to qualify and search sentences, but not to update applications or builds.
     */
    nlpUser,

    /**
     *  A faq nlp user is allowed to qualify and search sentences, and train the FAQ, but not to update applications or builds.
     */
    @Deprecated(message = "Use the 'nlpUser' role instead")
    faqNlpUser,

    /**
     *  A faq bot user is allowed to qualify and search sentences, and train the FAQ, but not to update applications or builds.
     */
    @Deprecated(message = "Use the 'botUser' role instead")
    faqBotUser,
    /**
     * A bot user is allowed to modify answer & i18n, and to consult dialogs and conversations.
     */
    botUser,
    /**
     * An admin is allowed to update applications and builds, and to export/intent sentences dump.
     */
    admin,
    /**
     * A technical admin has access to all encrypted sentence, and to export/intent application dumps.
     */
    technicalAdmin;

    companion object {
        private val logger = KotlinLogging.logger {}

        fun toRole(role: String): TockUserRole? =
            try {
                valueOf(role.trim())
            } catch (e: Exception) {
                logger.error { "unknown role : $role" }
                null
            }
    }
}
