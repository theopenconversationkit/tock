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
package ai.tock.nlp.systemone

import ai.tock.shared.longProperty
import ai.tock.shared.property

/**
 * Configuration for a System One API-compatible decision-model endpoint.
 *
 * No local/self-hosted inference is supported - this client always targets a REST endpoint
 * (Cloudflare's hosted API is supported via a configured [endpointUrl]).
 */
data class SystemOneConfiguration(
    /**
     * The Bearer token used to authenticate against the configured endpoint.
     */
    val apiToken: String = property("tock_systemone_api_token", ""),
    /**
     * Full decision-model endpoint URL, including any provider-specific path segments.
     */
    val endpointUrl: String = property("tock_systemone_endpoint_url", ""),
    /**
     * The model identifier sent in the request body (e.g. "clef" or "clef-flash").
     */
    val model: String = property("tock_systemone_model", "clef-flash"),
    /**
     * Request timeout in milliseconds.
     */
    val timeoutInMs: Long = longProperty("tock_systemone_timeout_ms", 10000L),
)
