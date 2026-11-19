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
import ai.tock.bot.connector.mattermost.model.MattermostPost
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.create
import ai.tock.shared.error
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

internal class MattermostClient(
    mattermostUrl: String,
    private val mattermostToken: String,
    private val apiToken: String? = null,
) {
    private interface MattermostApi {
        @Headers("Content-Type: application/json")
        @POST("hooks/{token}")
        fun sendMessage(
            @Path("token") token: String,
            @Body message: MattermostMessageOut,
        ): Call<Void>

        @GET("api/v4/posts/{postId}")
        fun getPost(
            @Header("Authorization") authorization: String,
            @Path("postId") postId: String,
        ): Call<MattermostPost>
    }

    private val logger = KotlinLogging.logger {}
    private val mattermostApi: MattermostApi =
        retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_mattermost_request_timeout_ms", 30000),
            logger,
        ).baseUrl(mattermostUrl)
            .addJacksonConverter()
            .build()
            .create()

    /**
     * Sends the message through the incoming webhook.
     * @return true if Mattermost accepted the message
     */
    fun sendMessage(message: MattermostMessageOut): Boolean =
        try {
            val response = mattermostApi.sendMessage(mattermostToken, message).execute()
            if (response.isSuccessful) {
                logger.debug { response }
                true
            } else {
                logger.error { "Mattermost rejected the message: ${response.code()} ${response.errorBody()?.string()}" }
                false
            }
        } catch (e: Throwable) {
            logger.error(e)
            false
        }

    /**
     * Returns the id of the root post of the thread containing [postId] (that is [postId] itself if it is not a reply),
     * or null if no api token is configured or if the post cannot be read.
     */
    fun threadRootOf(postId: String): String? {
        if (apiToken == null) {
            return null
        }
        return try {
            val response = mattermostApi.getPost("Bearer $apiToken", postId).execute()
            val post = response.body()
            if (response.isSuccessful && post != null) {
                post.rootId?.takeUnless { it.isBlank() } ?: post.id
            } else {
                logger.warn { "Unable to read Mattermost post $postId: ${response.code()} ${response.errorBody()?.string()}" }
                null
            }
        } catch (e: Throwable) {
            logger.error(e)
            null
        }
    }
}
