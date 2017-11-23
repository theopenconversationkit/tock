/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.shared.security

import mu.KotlinLogging

/**
 * The user roles for [TockUser].
 */
enum class TockUserRole {

    user, admin, technicalAdmin;

    companion object {
        private val logger = KotlinLogging.logger {}

        fun toRole(role: String): TockUserRole? =
                try {
                    valueOf(role)
                } catch (e: Exception) {
                    logger.error { "unknown role : $role" }
                    null
                }
    }

}