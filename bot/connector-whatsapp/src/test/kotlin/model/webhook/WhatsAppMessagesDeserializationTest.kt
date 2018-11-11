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
package fr.vsct.tock.bot.connector.whatsapp.model.webhook

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.connector.whatsapp.model.common.WhatsAppTextBody
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.resource
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 */
class WhatsAppMessagesDeserializationTest {

    @Test
    fun `text message is deserialized`() {
        val json = resource("/model/webhook/texto.json")
        val messages: WhatsAppMessages = mapper.readValue(json)
        assertEquals(
            WhatsAppMessages(
                listOf(
                    WhatsAppTextMessage(
                        WhatsAppTextBody("Hello this is an answer"),
                        "ABGGFlA5FpafAgo6tHcNmNjXmuSf",
                        "16315555555",
                        "1518694235",
                        WhatsAppProfile("Kerry Fisher")
                    )
                )
            ),
            messages
        )
    }

    @Test
    fun `location message is deserialized`() {
        val json = resource("/model/webhook/location.json")
        val messages: WhatsAppMessages = mapper.readValue(json)
        assertEquals(
            WhatsAppMessages(
                listOf(
                    WhatsAppLocationMessage(
                        WhatsAppLocation(
                            38.9806263495,
                            -131.9428612257,
                            "Main Street Beach, Santa Cruz, CA",
                            "Main Street Beach",
                            "https://foursquare.com/v/4d7031d35b5df7744"
                        ),
                        "ABGGFlA5FpafAgo6tHcNmNjXmuSf",
                        "16315555555",
                        "1521497875"
                    )
                )
            ),
            messages
        )
    }
}