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

package ai.tock.nlp.front.shared.monitoring

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.SearchMark
import org.litote.kmongo.Id
import java.time.Instant
import java.util.Locale

/**
 *
 */
data class ParseRequestLogQuery(
    val applicationId: Id<ApplicationDefinition>,
    val language: Locale?,
    val start: Long = 0,
    val size: Int = 1,
    val search: String? = null,
    val onlyExactMatch: Boolean = false,
    val searchMark: SearchMark? = null,
    val clientDevice: String? = null,
    val clientId: String? = null,
    val sinceDate: Instant? = null,
    /**
     * Display test logs.
     */
    val displayTests: Boolean = false,
)
