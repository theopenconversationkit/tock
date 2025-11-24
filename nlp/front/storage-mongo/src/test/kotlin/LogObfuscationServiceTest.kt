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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.entity.EmailValue
import ai.tock.nlp.front.shared.monitoring.ParseRequestLog
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.nlp.front.shared.parser.ParseResult
import ai.tock.nlp.front.shared.parser.ParsedEntityValue
import ai.tock.nlp.front.shared.parser.QueryContext
import ai.tock.shared.defaultLocale
import ai.tock.shared.security.SimpleObfuscator
import ai.tock.shared.security.TockObfuscatorService
import org.litote.kmongo.toId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogObfuscationServiceTest {
    @BeforeTest
    fun beforeTest() {
        TockObfuscatorService.registerStringObfuscator(
            SimpleObfuscator(
                "test@test.com".toRegex(),
                "****@********",
            ),
        )
    }

    @AfterTest
    fun afterTest() {
        TockObfuscatorService.deregisterObfuscators()
    }

    @Test
    fun `entities have to be also obfuscated`() {
        val log =
            ParseRequestLog(
                "appId".toId(),
                ParseQuery(listOf("test@test.com"), "test", "test", QueryContext(defaultLocale), configuration = null),
                ParseResult(
                    "intent",
                    "namespace",
                    defaultLocale,
                    listOf(
                        ParsedEntityValue(
                            0,
                            "test@test.com".length,
                            Entity(EntityType("entity"), "role"),
                            EmailValue("test@test.com"),
                        ),
                    ),
                    retainedQuery = "test@test.com",
                ),
                100L,
            )

        val service = LogObfuscationService()
        val obfuscated = service.obfuscate(log)

        assertEquals(listOf("****@********"), obfuscated.query.queries)
        assertEquals("****@********", obfuscated.result?.retainedQuery)
        assertEquals(EmailValue("****@********"), obfuscated.result?.entities?.first()?.value)
    }
}
