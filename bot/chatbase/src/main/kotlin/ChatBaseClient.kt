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

package ai.tock.analytics.chatbase

import ai.tock.analytics.chatbase.model.Message
import ai.tock.analytics.chatbase.model.Response
import ai.tock.analytics.chatbase.model.Status
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.create
import ai.tock.shared.error
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal class ChatBaseClient {
    private val baseUrl = "https://www.chatbase.com/"

    interface GenericMessageApi {
        @POST("/api/message")
        fun message(
            @Body message: Message,
        ): Call<Response>
    }

    private val logger = KotlinLogging.logger {}

    private val genericMessageApi: GenericMessageApi

    init {
        genericMessageApi =
            retrofitBuilderWithTimeoutAndLogger(
                longProperty("tock_chatbase_request_timeout_ms", 3000),
                logger,
            )
                .baseUrl(baseUrl)
                .addJacksonConverter()
                .build()
                .create()
    }

    private fun retrofit2.Response<*>.logError() {
        val error = message()
        val errorCode = code()
        logger.error { "Chatbase Error : $errorCode $error" }
        val errorBody = errorBody()?.string()
        logger.error { "Chatbase Error body : $errorBody" }
    }

    fun message(message: Message): Boolean {
        return try {
            val response = genericMessageApi.message(message).execute()
            if (response.isSuccessful) {
                response.body()?.status == Status.OK
            } else {
                response.logError()
                false
            }
        } catch (e: Exception) {
            // log and ignore
            logger.error(e)
            false
        }
    }
}
