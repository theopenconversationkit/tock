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

package ai.tock.bot.admin.user

import java.time.ZonedDateTime
import java.util.Locale

/**
 *
 */
data class UserReportQuery(
    val namespace: String,
    val nlpModel: String,
    val language: Locale,
    val start: Long = 0,
    val size: Int = 1,
    val name: String? = null,
    val from: ZonedDateTime? = null,
    val to: ZonedDateTime? = null,
    val flags: Map<String, String?> = emptyMap(),
    /**
     * Display test users.
     */
    val displayTests: Boolean = false
)
