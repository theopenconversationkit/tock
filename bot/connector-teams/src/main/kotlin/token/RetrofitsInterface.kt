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

package ai.tock.bot.connector.teams.token

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginMicrosoftOnline {
    @Headers("Host:login.microsoftonline.com", "Content-Type:application/x-www-form-urlencoded")
    @POST("/botframework.com/oauth2/v2.0/token")
    @FormUrlEncoded
    fun login(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("scope") scope: String = "https://api.botframework.com/.default",
    ): Call<LoginResponse>
}

data class LoginResponse(
    val tokenType: String,
    val expiresIn: Long,
    val extExpiresIn: Long,
    val accessToken: String,
)
