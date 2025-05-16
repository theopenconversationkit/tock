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

package ai.tock.bot.api.retrofit

import ai.tock.shared.jackson.mapper
import ai.tock.shared.longProperty
import okhttp3.OkHttpClient.Builder
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Creates a retrofit instance from the specified base url.
 *
 * @param baseUrl the service base url
 * @param timeout the timeout value - default is 20000ms.
 */
fun retrofit(baseUrl: String, timeout: Long = longProperty("tock_nlp_client_request_timeout_ms", 20000)): Retrofit {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .client(
            Builder()
                .readTimeout(timeout, MILLISECONDS)
                .connectTimeout(timeout, MILLISECONDS)
                .writeTimeout(timeout, MILLISECONDS)
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
                .build()
        )
        .build()
}
