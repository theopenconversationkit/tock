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

package ai.tock.bot.connector.slack

import ai.tock.bot.connector.slack.model.SlackConnectorMessage
import ai.tock.shared.jackson.mapper
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

object SlackClient {
    private val logger = KotlinLogging.logger { }

    interface SlackApi {
        @POST("/services/{outToken1}/{outToken2}/{outToken3}")
        fun sendMessage(
            @Path("outToken1") outToken1: String,
            @Path("outToken2") outToken2: String,
            @Path("outToken3") outToken3: String,
            @Body message: RequestBody,
        ): Call<Void>
    }

    private val slackApi: SlackApi =
        retrofitBuilderWithTimeoutAndLogger(
            30000,
            logger,
        )
            .baseUrl("https://hooks.slack.com")
            .build()
            .create(SlackApi::class.java)

    fun sendMessage(
        outToken1: String,
        outToken2: String,
        outToken3: String,
        message: SlackConnectorMessage,
    ) {
        val body = RequestBody.create("application/json".toMediaType(), mapper.writeValueAsBytes(message))
        val response = slackApi.sendMessage(outToken1, outToken2, outToken3, body).execute()
        logger.debug { response }
    }
}
