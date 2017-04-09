/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.shared

import com.fasterxml.jackson.databind.ObjectMapper
import fr.vsct.tock.shared.jackson.mapper
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 *
 */

fun <T : Any> Retrofit.create(service: KClass<T>): T = create(service.java)

fun retrofitBuilderWithTimeout(seconds: Long, vararg interceptors: Interceptor): Retrofit.Builder
        = OkHttpClient.Builder()
        .readTimeout(seconds, TimeUnit.SECONDS)
        .connectTimeout(seconds, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .apply {
            interceptors.forEach { addInterceptor(it) }
        }
        .build()
        .let {
            Retrofit.Builder().client(it)
        }

fun Retrofit.Builder.addJacksonConverter(objectMapper: ObjectMapper = mapper): Retrofit.Builder = run {
    addConverterFactory(JacksonConverterFactory.create(objectMapper))
}