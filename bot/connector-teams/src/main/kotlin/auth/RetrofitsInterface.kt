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

package ai.tock.bot.connector.teams.auth

import com.google.gson.Gson
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.Serializable

data class MicrosoftOpenidMetadata(
    val issuer: String,
    val authorizationEndpoint: String,
    val jwksUri: String,
    val idTokenSigningAlgValuesSupported: ArrayList<String>,
    val tokenEndpointAuthMethodsSupported: List<String>,
)

internal interface MicrosoftOpenIdMetadataApi {
    @GET(".well-known/openidconfiguration/")
    fun getMicrosoftOpenIdMetadata(): Call<MicrosoftOpenidMetadata>

    @GET(".well-known/openid-configuration/")
    fun getMicrosoftOpenIdMetadataForBotFwkEmulator(): Call<MicrosoftOpenidMetadata>
}

data class MicrosoftValidSigningKeys(
    val keys: List<MicrosoftValidSigningKey>,
) : Serializable

data class MicrosoftValidSigningKey(
    val kty: String,
    val use: String,
    val kid: String,
    val x5t: String?,
    val n: String,
    val e: String,
    val x5c: List<String>?,
    val endorsements: List<String>?,
) : Serializable {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

internal interface MicrosoftJwksApi {
    @GET
    fun getJwk(
        @Url url: String,
    ): Call<MicrosoftValidSigningKeys>
}
