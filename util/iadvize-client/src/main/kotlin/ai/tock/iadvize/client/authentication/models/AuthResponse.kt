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

package ai.tock.iadvize.client.authentication.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Representation of iadvize's authentication response
 */
data class AuthResponse(
    @JsonProperty("refresh_token")
    val refreshToken: String? = null,
    @JsonProperty("token_type")
    val tokenType: String? = null,
    @JsonProperty("access_token")
    val accessToken: String? = null,
    @JsonProperty("expires_in")
    val expiresIn: Number? = null,
)
