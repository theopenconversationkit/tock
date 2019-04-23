/*
 * Copyright (C) 2019 VSCT
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

package fr.vsct.tock.bot.connector.twitter.fr.vsct.tock.bot.connector.twitter.json

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.connector.twitter.model.MessageCreate
import fr.vsct.tock.bot.connector.twitter.model.MessageData
import fr.vsct.tock.bot.connector.twitter.model.Recipient
import fr.vsct.tock.bot.connector.twitter.model.outcoming.DirectMessageOutcomingEvent
import fr.vsct.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.resourceAsStream
import org.junit.jupiter.api.Test

internal class OutcomingEventSerializationTest {
    @Test
    fun testOutcomingEventDeserialization() {
        val twitterEvent = mapper.readValue<OutcomingEvent>(resourceAsStream("/outcoming_direct_message.json"))
        kotlin.test.assertNotNull(twitterEvent)
    }

    @Test
    fun testOutcomingEventSerialization() {
        val outcomingEvent = OutcomingEvent(
            DirectMessageOutcomingEvent(
                MessageCreate(Recipient("recipientId"), "senderId", "sourceAppId", MessageData("text"))
            )
        )
        val s = mapper.writeValueAsString(outcomingEvent)
        kotlin.test.assertEquals(outcomingEvent, mapper.readValue(s))
    }
}