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

package ai.tock.shared.security.auth

import ai.tock.shared.addJacksonConverter
import ai.tock.shared.create
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

/**
 *
 */
internal object RetrofitGithubClient {

    data class GithubUser(val login: String)

    private interface GithubApi {

        @GET("/user")
        fun user(@Header("Authorization") authorization: String): Call<GithubUser>
    }

    private val api: GithubApi
    private val logger = KotlinLogging.logger {}

    init {

        api = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_github_api_request_timeout_ms", 5000),
            logger
        )
            .baseUrl("https://api.github.com")
            .addJacksonConverter()
            .build()
            .create()
    }

    fun login(token: String): String {
        return api.user("token $token").execute().body()?.login ?: error("no login found for $token")
    }
}
