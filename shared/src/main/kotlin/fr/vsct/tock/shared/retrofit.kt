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
import mu.KLogger
import mu.KotlinLogging
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.internal.http.HttpEngine
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.EOFException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.charset.UnsupportedCharsetException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS

inline fun <reified T : Any> Retrofit.create(): T = create(T::class.java)

@Deprecated("use retrofitBuilderWithTimeoutAndLogger instead")
fun retrofitBuilderWithTimeout(
        ms: Long,
        vararg interceptors: Interceptor): Retrofit.Builder
        = OkHttpClient.Builder()
        .readTimeout(ms, MILLISECONDS)
        .connectTimeout(ms, MILLISECONDS)
        .apply {
            interceptors.forEach { addInterceptor(it) }
        }
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()
        .let {
            Retrofit.Builder().client(it)
        }

fun retrofitBuilderWithTimeoutAndLogger(
        ms: Long,
        logger: KLogger = KotlinLogging.logger {},
        level: Level = Level.BODY,
        interceptors: List<Interceptor> = emptyList()
): Retrofit.Builder
        = OkHttpClient.Builder()
        .readTimeout(ms, MILLISECONDS)
        .connectTimeout(ms, MILLISECONDS)
        .apply {
            interceptors.forEach { addInterceptor(it) }
        }
        .addInterceptor(LoggingInterceptor(logger, level))
        .build()
        .let {
            Retrofit.Builder().client(it)
        }

fun Retrofit.Builder.addJacksonConverter(objectMapper: ObjectMapper = mapper): Retrofit.Builder = run {
    addConverterFactory(JacksonConverterFactory.create(objectMapper))
}

//copied from okhttp3.logging.HttpLogginginterceptor

enum class Level {
    /** No logs.  */
    NONE,
    /**
     * Logs request and response lines.

     *
     * Example:
     * <pre>`--> POST /greeting http/1.1 (3-byte body)

     * <-- 200 OK (22ms, 6-byte body)
    `</pre> *
     */
    BASIC,
    /**
     * Logs request and response lines and their respective headers.

     *
     * Example:
     * <pre>`--> POST /greeting http/1.1
     * Host: example.com
     * Content-Type: plain/text
     * Content-Length: 3
     * --> END POST

     * <-- 200 OK (22ms)
     * Content-Type: plain/text
     * Content-Length: 6
     * <-- END HTTP
    `</pre> *
     */
    HEADERS,
    /**
     * Logs request and response lines and their respective headers and bodies (if present).

     *
     * Example:
     * <pre>`--> POST /greeting http/1.1
     * Host: example.com
     * Content-Type: plain/text
     * Content-Length: 3

     * Hi?
     * --> END GET

     * <-- 200 OK (22ms)
     * Content-Type: plain/text
     * Content-Length: 6

     * Hello!
     * <-- END HTTP
    `</pre> *
     */
    BODY
}

private class LoggingInterceptor(val logger: KLogger, val level: Level) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val level = this.level

        val request = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(request)
        }

        val logBody = level == Level.BODY
        val logHeaders = logBody || level == Level.HEADERS

        val requestBody = request.body()
        val hasRequestBody = requestBody != null

        val connection = chain.connection()
        val protocol = if (connection != null) connection.protocol() else Protocol.HTTP_1_1
        var requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody!!.contentLength() + "-byte body)"
        }
        logger.info(requestStartMessage)

        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody!!.contentType() != null) {
                    logger.info("Content-Type: " + requestBody.contentType())
                }
                if (requestBody.contentLength() != -1L) {
                    logger.info("Content-Length: " + requestBody.contentLength())
                }
            }

            val headers = request.headers()
            var i = 0
            val count = headers.size()
            while (i < count) {
                val name = headers.name(i)
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(name, ignoreCase = true) && !"Content-Length".equals(name, ignoreCase = true)) {
                    logger.info(name + ": " + headers.value(i))
                }
                i++
            }

            if (!logBody || !hasRequestBody) {
                logger.info("--> END " + request.method())
            } else if (bodyEncoded(request.headers())) {
                logger.info("--> END " + request.method() + " (encoded body omitted)")
            } else {
                val buffer = Buffer()
                requestBody!!.writeTo(buffer)

                var charset = UTF_8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF_8)
                }

                logger.info("")
                if (isPlaintext(buffer)) {
                    logger.info(buffer.readString(charset))
                    logger.info("--> END " + request.method()
                            + " (" + requestBody.contentLength() + "-byte body)")
                } else {
                    logger.info("--> END " + request.method() + " (binary "
                            + requestBody.contentLength() + "-byte body omitted)")
                }
            }
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.error("<-- HTTP FAILED: " + e)
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body()
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) contentLength.toString() + "-byte" else "unknown-length"
        logger.info("<-- " + response.code() + ' ' + response.message() + ' '
                + response.request().url() + " (" + tookMs + "ms" + (if (!logHeaders)
            ", "
                    + bodySize + " body"
        else
            "") + ')')

        if (logHeaders) {
            val headers = response.headers()
            var i = 0
            val count = headers.size()
            while (i < count) {
                logger.info(headers.name(i) + ": " + headers.value(i))
                i++
            }

            if (!logBody || !HttpEngine.hasBody(response)) {
                logger.info("<-- END HTTP")
            } else if (bodyEncoded(response.headers())) {
                logger.info("<-- END HTTP (encoded body omitted)")
            } else {
                val source = responseBody.source()
                source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
                val buffer = source.buffer()

                var charset = UTF_8
                val contentType = responseBody.contentType()
                if (contentType != null) {
                    try {
                        charset = contentType.charset(UTF_8)
                    } catch (e: UnsupportedCharsetException) {
                        logger.info("")
                        logger.info("Couldn't decode the response body; charset is likely malformed.")
                        logger.info("<-- END HTTP")

                        return response
                    }

                }

                if (!isPlaintext(buffer)) {
                    logger.info("")
                    logger.info("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)")
                    return response
                }

                if (contentLength != 0L) {
                    logger.info("")
                    logger.info(buffer.clone().readString(charset))
                }

                logger.info("<-- END HTTP (" + buffer.size() + "-byte body)")
            }
        }

        return response
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    internal fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size() < 64) buffer.size() else 64
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
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }
}

