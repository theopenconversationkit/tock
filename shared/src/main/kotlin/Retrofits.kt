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

package ai.tock.shared

import ai.tock.shared.Level.BODY
import ai.tock.shared.Level.NONE
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.retrofit.CircuitBreakerCallAdapter
import mu.KLogger
import mu.KotlinLogging
import okhttp3.ConnectionSpec
import okhttp3.Credentials
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.BufferedSink
import okio.GzipSink
import okio.buffer
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.EOFException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.Proxy
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Best attempt to guess local ip.
 */
fun tryToFindLocalIp(): String {
    return NetworkInterface.getNetworkInterfaces()
        .toList()
        .run {
            find { it.name.contains("eno") }
                ?.inetAddresses?.toList()?.filterIsInstance<Inet4Address>()?.firstOrNull()?.hostName
                ?: flatMap { it.inetAddresses.toList().filterIsInstance<Inet4Address>() }
                    .find { it.hostName.startsWith("192.168.0") }
                    ?.hostName
                ?: "localhost"
        }
}

/**
 * Create a new Retrofit service.
 */
inline fun <reified T : Any> Retrofit.create(): T = create(T::class.java)

/**
 * Retrofit log level environment variable
 */
val retrofitDefaultLogLevel = propertyOrNull("tock_retrofit_log_level")

/**
 * Adapt retrofit log level to [logLevel] following the [Level] (inspired from [HttpLoggingInterceptor.Level])
 * Default is [BODY] when using dev environment, [NONE] in Production
 * @param logLevel the [Level] wanted, easy to use the environment variable [logLevel]
 * @return [Level]
 */
fun retrofitLogLevel(logLevel: String?): Level =
    if (devEnvironment && logLevel.isNullOrEmpty()) BODY
    else {
        logLevel?.let { Level.valueOf(logLevel) } ?: NONE
    }

/**
 * Init a [Retrofit.Builder] with specified timeout, logger and interceptors.
 */
fun retrofitBuilderWithTimeoutAndLogger(
    ms: Long,
    logger: KLogger = KotlinLogging.logger {},
    level: Level = retrofitLogLevel(retrofitDefaultLogLevel),
    interceptors: List<Interceptor> = emptyList(),
    /**
     * Gzip the request for servers that support it.
     */
    requestGZipEncoding: Boolean = false,
    /**
     * Add a circuit breaker facility.
     */
    circuitBreaker: Boolean = false,
    proxy: Proxy? = null
): Retrofit.Builder = OkHttpClient.Builder()
    .readTimeout(ms, MILLISECONDS)
    .connectTimeout(ms, MILLISECONDS)
    .writeTimeout(ms, MILLISECONDS)
    .apply {
        interceptors.forEach { addInterceptor(it) }
    }
    .apply {
        takeIf { requestGZipEncoding }
            ?.addInterceptor(GzipRequestInterceptor())
    }
    .addInterceptor(LoggingInterceptor(logger, level))
    .apply {
        // support compatible tls
        connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
        takeIf { proxy != null }
            ?.proxy(proxy)
    }
    .apply(TockProxyAuthenticator::install)

    .build()
    .let {
        Retrofit.Builder().client(it)
            .apply {

                takeIf { circuitBreaker && booleanProperty("tock_circuit_breaker", false) }
                    ?.addCallAdapterFactory(CircuitBreakerCallAdapter.of(CircuitBreaker.ofDefaults(logger.name)))
            }
    }

/**
 * Create a basic auth interceptor.
 */
fun basicAuthInterceptor(login: String, password: String): Interceptor {
    val credential = basicCredentialsHeader(login, password)
    return Interceptor { chain ->
        val original = chain.request()

        val requestBuilder = original.newBuilder()
            .header("Authorization", credential)

        val request = requestBuilder.build()
        chain.proceed(request)
    }
}

/**
 * Create a token authentication interceptor.
 */
fun tokenAuthenticationInterceptor(token: String): Interceptor {
    return Interceptor { chain ->
        val original = chain.request()

        val requestBuilder = original.newBuilder()
            .header("Authorization", "Bearer $token")

        val request = requestBuilder.build()
        chain.proceed(request)
    }
}

/**
 * Encode basic credential header.
 */
fun basicCredentialsHeader(login: String, password: String): String =
    Credentials.basic(login, password)

/**
 * Add jackson converter factory.
 */
fun Retrofit.Builder.addJacksonConverter(objectMapper: ObjectMapper = mapper): Retrofit.Builder = run {
    addConverterFactory(JacksonConverterFactory.create(objectMapper))
}

/** This interceptor compresses the HTTP request body. Many webservers can't handle this!  */
private class GzipRequestInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val body = originalRequest.body
        if (body == null ||
            originalRequest.header("Content-Encoding") != null ||
            body.contentLength() < 512
        ) {
            return chain.proceed(originalRequest)
        }

        val compressedRequest = originalRequest.newBuilder()
            .header("Content-Encoding", "gzip")
            .method(originalRequest.method, gzip(body))
            .build()
        return chain.proceed(compressedRequest)
    }

    private fun gzip(body: RequestBody): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return body.contentType()
            }

            override fun contentLength(): Long {
                return -1 // We don't know the compressed length in advance!
            }

            override fun writeTo(sink: BufferedSink) {
                val gzipSink = GzipSink(sink).buffer()
                body.writeTo(gzipSink)
                gzipSink.close()
            }
        }
    }
}

// copied from okhttp3.logging.HttpLogginginterceptor

/**
 * Http requests/response log level.
 */
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

        val requestBody = request.body
        val hasRequestBody = requestBody != null

        val connection = chain.connection()
        val protocol = if (connection != null) connection.protocol() else Protocol.HTTP_1_1
        var requestStartMessage = "--> " + request.method + ' ' + request.url + ' ' + protocol
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

            val headers = request.headers
            var i = 0
            val count = headers.size
            while (i < count) {
                val name = headers.name(i)
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(name, ignoreCase = true) && !"Content-Length".equals(
                        name,
                        ignoreCase = true
                    )
                ) {
                    logger.info(name + ": " + headers.value(i))
                }
                i++
            }

            if (!logBody || !hasRequestBody) {
                logger.info("--> END " + request.method)
            } else if (bodyEncoded(request.headers)) {
                logger.info("--> END " + request.method + " (encoded body omitted)")
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
                    logger.info(
                        "--> END " + request.method +
                                " (" + requestBody.contentLength() + "-byte body)"
                    )
                } else {
                    logger.info(
                        "--> END " + request.method + " (binary " +
                                requestBody.contentLength() + "-byte body omitted)"
                    )
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

        val responseBody = response.body
        val contentLength = responseBody?.contentLength() ?: -1
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        logger.info(
            "<-- " + response.code + ' ' + response.message + ' ' +
                    response.request.url + " (" + tookMs + "ms" + (
                    if (!logHeaders)
                        ", " +
                                bodySize + " body"
                    else
                        ""
                    ) + ')'
        )

        if (logHeaders) {
            val headers = response.headers
            var i = 0
            val count = headers.size
            while (i < count) {
                logger.info(headers.name(i) + ": " + headers.value(i))
                i++
            }

            if (!logBody || !response.promisesBody()) {
                logger.info("<-- END HTTP")
            } else if (bodyEncoded(response.headers)) {
                logger.info("<-- END HTTP (encoded body omitted)")
            } else {
                val source = responseBody?.source()
                if(source != null) {
                    source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
                    val buffer = source.buffer

                    var charset = UTF_8
                    val contentType = responseBody.contentType()
                    if (contentType != null) {
                        charset = contentType.charset(UTF_8)
                    }

                    if (!isPlaintext(buffer)) {
                        logger.info("")
                        logger.info("<-- END HTTP (binary " + buffer.size + "-byte body omitted)")
                        return response
                    }

                    if (contentLength != 0L && buffer != null) {
                        logger.info("")
                        logger.info(buffer.clone().readString(charset))
                    }

                    logger.info("<-- END HTTP (" + buffer?.size + "-byte body)")
                }
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
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }
}
