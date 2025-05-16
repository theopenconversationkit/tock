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

package ai.tock.bot.connector.businesschat

import ai.tock.bot.connector.businesschat.model.common.ReceivedModel
import ai.tock.bot.connector.businesschat.model.csp.BusinessChatCommonModel
import ai.tock.bot.engine.event.Event
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.jackson.mapper
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import ai.tock.shared.trace
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KLogger
import okhttp3.Interceptor
import okhttp3.RequestBody
import okio.Buffer
import java.io.IOException
import kotlin.reflect.KClass

/**
 * In order to manage CSP BusinessChat integration.
 */
interface BusinessChatIntegrationService {

    val baseUrl: String

    fun parseThreadControl(message: ReceivedModel, connectorId: String): Event?

    fun authInterceptor(): Interceptor?

    fun passControl(sourceId: String, recipient: String)

    fun takeControl(sourceId: String, recipient: String)

    fun <T : Any> createClient(clazz: KClass<T>, logger: KLogger): T {
        fun bodyToString(request: RequestBody?): String? {
            return try {
                val buffer = Buffer()
                request?.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: IOException) {
                logger.debug("bodyToString error")
                logger.trace(e)
                null
            }
        }

        val headerInterceptor = Interceptor { chain ->
            val original = chain.request()
            val request = try {
                val bodyString = bodyToString(original.body)
                if (bodyString != null) {
                    val message = mapper.readValue<BusinessChatCommonModel>(bodyString)
                    original.newBuilder()
                        .addHeader("id", message.id)
                        .addHeader("Source-Id", message.sourceId)
                        .addHeader("Destination-Id", message.destinationId)
                        .method(original.method, original.body)
                        .build()
                } else null
            } catch (e: Exception) {
                logger.debug("error in deserialising json object")
                original
            }
            chain.proceed(request ?: original)
        }
        return retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_business_chat_request_timeout_ms", 30000),
            logger,
            interceptors = listOfNotNull(authInterceptor(), headerInterceptor)
        )
            .baseUrl(baseUrl)
            .addJacksonConverter()
            .build()
            .create(clazz.java)
    }
}
