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

package ai.tock.iadvize.client

import mu.KLogger
import mu.KotlinLogging
import okhttp3.ConnectionSpec
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.http.promisesBody
import okio.Buffer
import retrofit2.Retrofit
import java.io.EOFException
import java.net.Proxy
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

/**
 * Logging interceptor for Retrofit client.
 */
private class LoggingInterceptor(val logger: KLogger) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val connection = chain.connection()
        val protocol = connection?.protocol() ?: Protocol.HTTP_1_1
        val request = chain.request()

        logger.info("--> ${request.method} ${request.url} $protocol")

        val requestHeaders = request.headers.onEach { logger.debug { "${it.first}: ${it.second}" } }
        val requestBody = request.body
        when {
            requestBody == null -> logger.info("--> END ${request.method}")
            bodyEncoded(requestHeaders) -> logger.info("--> END ${request.method} (encoded body omitted)")
            else -> {
                with(requestBody) {
                    val buffer = Buffer()

                    writeTo(buffer)

                    if (isPlaintext(buffer)) {
                        val charset = contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                        var bodyString = buffer.readString(charset)

                        // Mask sensitive data
                        bodyString =
                            bodyString
                                .replace(Regex("(?i)(password=)[^&]+"), "$1****") // mask password

                        logger.debug(bodyString)
                        logger.info("--> END ${request.method} (${contentLength()}-byte body)")
                    } else {
                        logger.info("--> END ${request.method} (binary ${contentLength()}-byte body omitted)")
                    }
                }
            }
        }

        val response: Response
        val responseBody: ResponseBody?

        try {
            measureTimeMillis {
                response = chain.proceed(request)
            }.let {
                responseBody =
                    with(response) {
                        logger.info("<-- $code $message ${request.url} ($it ms)")
                        body
                    }
            }
        } catch (e: Exception) {
            logger.error("<-- HTTP FAILED: $e")
            throw e
        }

        val responseHeaders = response.headers.onEach { logger.debug { "${it.first} : ${it.second}" } }

        when {
            !response.promisesBody() -> logger.info("<-- END HTTP")
            bodyEncoded(responseHeaders) -> logger.info("<-- END HTTP (encoded body omitted)")
            else -> {
                with(responseBody!!) {
                    val buffer =
                        source()
                            .apply { request(java.lang.Long.MAX_VALUE) }.buffer

                    if (!isPlaintext(buffer)) {
                        logger.info("")
                        logger.info("<-- END HTTP (binary ${buffer.size}-byte body omitted)")
                        return response
                    }

                    val charset = contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                    if (contentLength() != 0L) {
                        logger.info("")

                        // Mask sensitive data
                        var responseString = buffer.clone().readString(charset)
                        responseString =
                            responseString
                                .replace(Regex("(?i)(\"access_token\"\\s*:\\s*\").*?(\")"), "$1****$2")
                                .replace(Regex("(?i)(\"refresh_token\"\\s*:\\s*\").*?(\")"), "$1****$2")

                        logger.debug(responseString)
                    }

                    logger.info("<-- END HTTP (${buffer.size}-byte body)")
                }
            }
        }

        return response
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                if (Character.isISOControl(prefix.readUtf8CodePoint())) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }
}

/**
 * Creates a Retrofit client.
 */
fun retrofitBuilderWithTimeoutAndLogger(
    ms: Long,
    logger: KLogger = KotlinLogging.logger {},
    interceptors: List<Interceptor> = emptyList(),
    proxy: Proxy? = null,
): Retrofit.Builder =
    OkHttpClient.Builder()
        .readTimeout(ms, TimeUnit.MILLISECONDS)
        .connectTimeout(ms, TimeUnit.MILLISECONDS)
        .writeTimeout(ms, TimeUnit.MILLISECONDS)
        .apply { interceptors.forEach { addInterceptor(it) } }
        .addInterceptor(LoggingInterceptor(logger))
        .apply {
            // support compatible tls
            connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
            takeIf { proxy != null }?.proxy(proxy)
        }
        .build()
        .let { Retrofit.Builder().client(it) }

/**
 * Creates authentication interceptor.
 */
fun tokenAuthenticationInterceptor(token: () -> String) =
    Interceptor { chain ->
        chain.request()
            .newBuilder()
            .header(AUTHORIZATION, "$BEARER ${token()}")
            .build()
            .let { chain.proceed(it) }
    }
