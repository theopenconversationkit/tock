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

import fr.vsct.tock.shared.jackson.mapper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 *
 */

fun <T : Any> Retrofit.create(service: KClass<T>): T = create(service.java)

fun retrofitBuilderWithTimeout(seconds: Int): Retrofit.Builder
        = OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).connectTimeout(30, TimeUnit.SECONDS).build()
        .let {
            Retrofit.Builder().client(it)
        }

fun Retrofit.Builder.addJacksonConverter(): Retrofit.Builder = run {
    addConverterFactory(JacksonConverterFactory.create(mapper))
}