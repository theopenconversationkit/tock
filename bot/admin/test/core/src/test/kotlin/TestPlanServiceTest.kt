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

import ai.tock.bot.admin.test.TestPlanService.checkEquality
import ai.tock.bot.connector.rest.client.model.ClientChoice
import ai.tock.bot.connector.rest.client.model.ClientConnectorType
import ai.tock.bot.connector.rest.client.model.ClientGenericMessage
import ai.tock.bot.connector.rest.client.model.ClientSentence
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.expect

internal class TestPlanServiceTest {
    @ParameterizedTest
    @CsvSource("Hello,Hello", "Hello ,Hello", "Hello, Hello ")
    fun `should be deepEquals if texts are the same`(
        text: String,
        comparedText: String,
    ) {
        expect(null) {
            ClientSentence(text).checkEquality(ClientSentence(comparedText))
        }
    }

    @Test
    fun `should be deepEquals if texts are the same removing smileys`() {
        expect(null) {
            ClientSentence("Hello").checkEquality(ClientSentence("Hello \uD83E\uDD70"))
        }
    }

    @Test
    fun `should not be deepEquals if texts are not the same`() {
        expect("Text differs : \"Hello\" / expected \"Hello you\"") {
            ClientSentence("Hello").checkEquality(ClientSentence("Hello you"))
        }
    }

    @Test
    fun `should be deepEquals for simple text with suggestion message`() {
        expect(null) {
            ClientSentence(
                null,
                mutableListOf(
                    ClientGenericMessage(
                        ClientConnectorType.none,
                        texts = mapOf("text" to "Hello"),
                        choices = listOf(ClientChoice("cancel")),
                    ),
                ),
            ).checkEquality(
                ClientSentence(
                    null,
                    mutableListOf(
                        ClientGenericMessage(
                            ClientConnectorType.none,
                            texts = mapOf("text" to "Hello"),
                            choices = listOf(ClientChoice("cancel")),
                        ),
                    ),
                ),
            )
        }
    }

    @Test
    fun `should not be deepEquals if simple text with suggestion message differs`() {
        expect("Message text differs : \"{text=Hello you}\" / expected \"{text=Hello}\"") {
            ClientSentence(
                null,
                mutableListOf(
                    ClientGenericMessage(
                        ClientConnectorType.none,
                        texts = mapOf("text" to "Hello you"),
                        choices = listOf(ClientChoice("cancel")),
                    ),
                ),
            ).checkEquality(
                ClientSentence(
                    null,
                    mutableListOf(
                        ClientGenericMessage(
                            ClientConnectorType.none,
                            texts = mapOf("text" to "Hello"),
                            choices = listOf(ClientChoice("cancel")),
                        ),
                    ),
                ),
            )
        }
    }
}
