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

package ai.tock.genai.orchestratorclient.retrofit

import ai.tock.genai.orchestratorclient.responses.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.Interceptor
import okhttp3.Response

class GenAIOrchestratorInterceptor(private val jsonObjectMapper: ObjectMapper = jacksonObjectMapper()) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            // Proceed with the request
            val response: Response = chain.proceed(chain.request())

            // Check if the response is not successful
            if (!response.isSuccessful) {
                // Handle error response globally
                handleApiError(response)
            }

            // Return the response
            return response
        } catch (exc: Exception) {
            // Handle network or unexpected errors
            exc.printStackTrace()
            throw exc
        }
    }

    private fun handleApiError(response: Response) {
        val errorBody = response.body?.string()
        when (response.code) {
            400 -> {
                val errorResponse = jsonObjectMapper.readValue(errorBody, ErrorResponse::class.java)
                throw GenAIOrchestratorBusinessError(error = errorResponse)
            }
            422 -> {
                // Unprocessable Entity
                val errorResponse = jsonObjectMapper.readValue(errorBody, HTTPValidationErrorResponse::class.java)
                throw GenAIOrchestratorValidationError(detail = errorResponse.detail)
            }
            else -> {
                throw Exception("Generative AI Orchestrator unknown error")
            }
        }
    }
}
