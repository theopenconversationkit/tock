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

package ai.tock.nlp.api

import ai.tock.shared.http.HttpRequest

/**
 * The protection of the [NlpApiRoutes], as `tock_nlp_protect_path` protects `NlpVerticle`.
 */
sealed interface NlpApiSecurity {
    /**
     * No authentication: for a runtime that only trusted callers reach (a private network, a function whose
     * URL is protected by the cloud provider). Refused when `nlpverticle_tock_nlp_protect_path` is set.
     */
    data object Unprotected : NlpApiSecurity

    /**
     * Authenticates the caller: each route then only serves the namespace that [namespace] returns
     * (the organization of the user for `NlpVerticle`), and answers 401 otherwise.
     */
    fun interface Namespace : NlpApiSecurity {
        /** The namespace that the caller of [request] may access, or null if it is not authenticated. */
        suspend fun namespace(request: HttpRequest): String?
    }
}
