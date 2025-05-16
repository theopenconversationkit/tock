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

package ai.tock.nlp.admin.model

import ai.tock.nlp.front.shared.config.SearchMark

/**
 *
 */
open class PaginatedQuery(
    val start: Long = 0,
    val size: Int = 10,
    /**
     * If searchMark is not null, the results start with the elements after the [searchMark].
     */
    val searchMark: SearchMark? = null,
    /**
     * The optional sort parameters.
     */
    val sort: List<Pair<String, Boolean>>? = null
) : ApplicationScopedQuery()
