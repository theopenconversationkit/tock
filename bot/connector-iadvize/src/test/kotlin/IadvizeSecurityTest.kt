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

package ai.tock.bot.connector.iadvize

import ai.tock.shared.exception.rest.BadRequestException
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.mockk
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.slf4j.LoggerFactory
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IadvizeSecurityTest {
    private val secret = "my-iadvize-secret"
    private val body = """{"idOperator":"op"}"""
    private val logger = LoggerFactory.getLogger(IadvizeSecurity::class.java) as Logger
    private val logs = ListAppender<ILoggingEvent>()

    @BeforeEach
    fun captureLogs() {
        logs.start()
        logger.addAppender(logs)
    }

    @AfterEach
    fun releaseLogs() {
        logger.detachAppender(logs)
    }

    private fun sign(payload: String): String {
        val mac = Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(secret.toByteArray(), "HmacSHA256")) }
        return mac.doFinal(payload.toByteArray()).joinToString("", prefix = "sha256=") { "%02x".format(it) }
    }

    private fun context(signature: String?): RoutingContext =
        mockk(relaxed = true) {
            every { request().method() } returns HttpMethod.POST
            every { body().asString() } returns body
            every { request().getHeader(IadvizeSecurity.HEADER_NAME) } returns signature
        }

    @Test
    fun `GIVEN a valid signature WHEN the payload is validated THEN it is accepted`() {
        IadvizeSecurity(secret).validatePayloads(context(sign(body)))
    }

    @Test
    fun `GIVEN an invalid or missing signature WHEN the payload is validated THEN it is refused without logging the secret nor the expected signature`() {
        assertFailsWith<BadRequestException> { IadvizeSecurity(secret).validatePayloads(context("sha256=forged")) }
        assertFailsWith<BadRequestException> { IadvizeSecurity(secret).validatePayloads(context(null)) }

        val logged = logs.list.joinToString("\n") { it.formattedMessage }
        assertTrue(logged.contains("sha256=forged"), logged)
        assertFalse(logged.contains(secret), logged)
        assertFalse(logged.contains(sign(body)), logged)
    }
}
