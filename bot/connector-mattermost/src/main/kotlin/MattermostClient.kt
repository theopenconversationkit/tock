/*
 * Copyright (C) 2017/2024 e-voyageurs technologies
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

package ai.tock.bot.connector.mattermost

import ai.tock.bot.connector.mattermost.model.MattermostConnectorMessage
import ai.tock.shared.create
import ai.tock.shared.jackson.mapper
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import java.time.OffsetDateTime

/**
 *
 */
internal class MattermostClient(
    mattermostUrl: String
) {
    private interface MattermostApi {
        @Headers("Content-Type: application/json")
        @POST("hooks/{token}")
        fun sendMessage(@Path("token") token: String, @Body message: RequestBody): Call<Void>
    }

    private val logger = KotlinLogging.logger {}
    private val mattermostApi: MattermostApi = retrofitBuilderWithTimeoutAndLogger(
        longProperty("tock_mattermost_request_timeout_ms", 30000),
        logger
    )
        .baseUrl(mattermostUrl)
        .build()
        .create()

    @Volatile
    private var tokenExpiration: OffsetDateTime? = null

    fun sendMessage(token: String, message: MattermostConnectorMessage) {
        val body = RequestBody.create("application/json".toMediaType(), mapper.writeValueAsBytes(message))
        val response = mattermostApi.sendMessage(token, body).execute()
        logger.debug { response }
    }
}
