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

package ai.tock.bot.connector.mattermost

import ai.tock.bot.connector.mattermost.model.MattermostMessageOut
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.create
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

/**
 *
 */
internal class MattermostClient(
    mattermostUrl: String,
    private val mattermostToken: String,
) {
    private interface MattermostApi {
        @Headers("Content-Type: application/json")
        @POST("hooks/{token}")
        fun sendMessage(
            @Path("token") token: String,
            @Body message: MattermostMessageOut,
        ): Call<Void>
    }

    private val logger = KotlinLogging.logger {}
    private val mattermostApi: MattermostApi =
        retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_mattermost_request_timeout_ms", 30000),
            logger,
        )
            .baseUrl(mattermostUrl)
            .addJacksonConverter()
            .build()
            .create()

    fun sendMessage(message: MattermostMessageOut) {
        val response = mattermostApi.sendMessage(mattermostToken, message).execute()
        logger.debug { response }
    }
}
