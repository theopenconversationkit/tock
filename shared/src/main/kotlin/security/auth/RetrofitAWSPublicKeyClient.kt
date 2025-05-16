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
import ai.tock.shared.property
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal object RetrofitAWSPublicKeyClient {

    private interface AWSPublicKeyApi {

        @GET("/{keyID}")
        fun getPublicKey(@Path("keyID") keyID: String): Call<ResponseBody>
    }

    private val api: AWSPublicKeyApi
    private val logger = KotlinLogging.logger {}
    private val awsRegion = property("aws_region", "eu-west-1")

    init {

        api = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_aws_public_key_request_timeout_ms", 30000),
            logger
        )
            .baseUrl("https://public-keys.auth.elb.$awsRegion.amazonaws.com")
            .addJacksonConverter()
            .build()
            .create()
    }

    fun getPublicKey(keyID: String): String? {
        return api.getPublicKey(keyID).execute().body()?.string()
    }
}
