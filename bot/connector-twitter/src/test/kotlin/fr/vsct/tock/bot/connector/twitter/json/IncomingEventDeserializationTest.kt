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
import fr.vsct.tock.bot.connector.twitter.model.Application
import fr.vsct.tock.bot.connector.twitter.model.DirectMessage
import fr.vsct.tock.bot.connector.twitter.model.Entities
import fr.vsct.tock.bot.connector.twitter.model.Hashtag
import fr.vsct.tock.bot.connector.twitter.model.Mention
import fr.vsct.tock.bot.connector.twitter.model.MessageCreate
import fr.vsct.tock.bot.connector.twitter.model.MessageData
import fr.vsct.tock.bot.connector.twitter.model.Recipient
import fr.vsct.tock.bot.connector.twitter.model.Symbol
import fr.vsct.tock.bot.connector.twitter.model.Url
import fr.vsct.tock.bot.connector.twitter.model.User
import fr.vsct.tock.bot.connector.twitter.model.incoming.DirectMessageIncomingEvent
import fr.vsct.tock.bot.connector.twitter.model.incoming.IncomingEvent
import fr.vsct.tock.bot.connector.twitter.model.outcoming.DirectMessageOutcomingEvent
import fr.vsct.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.resourceAsStream
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class IncomingEventDeserializationTest {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Test
    fun testTwitterEventDeserialization() {

        val twitterEvent = DirectMessageIncomingEvent(
            forUserId = "forUserId",
            directMessages = listOf(
                DirectMessage(
                    //type = "type",
                    id = "id",
                    created = Date().time,
                    messageCreated = MessageCreate(
                        target = Recipient(
                            recipientId = "recipientId"
                        ),
                        senderId = "senderId",
                        sourceAppId = "sourceAppId",
                        messageData = MessageData(
                            text = "message",
                            entities = Entities(
                                hashtags = listOf(Hashtag("tag1", listOf(1, 5)), Hashtag("tag2", listOf(1, 5))),
                                symbols = listOf(Symbol(listOf(0, 5))),
                                mentions = listOf(Mention("screenName", "name", "id", "idStr", listOf(1, 5))),
                                urls = listOf(Url("url", "expandedUrl", "displayUrl", listOf(1, 5)))
                            )
                        )
                    )
                )
            ),
            apps = mapOf(
                "appId" to Application(
                    id = "appId",
                    name = "applicationName",
                    url = "url"
                )
            ),
            users = mapOf(
                "user1" to User(
                    id = "id",
                    created = Date().time,
                    name = "name",
                    screenName = "screenName",
                    protected = false,
                    verified = true,
                    followersCount = 100,
                    friendsCount = 100,
                    statusesCount = 100,
                    profileImageUrl = "profileImageUrl",
                    profileImageUrlHttps = "profileImageUrlHttps"
                ),
                "user2" to User(
                    id = "id",
                    created = Date().time,
                    name = "name",
                    screenName = "screenName",
                    protected = false,
                    verified = true,
                    followersCount = 100,
                    friendsCount = 100,
                    statusesCount = 100,
                    profileImageUrl = "profileImageUrl",
                    profileImageUrlHttps = "profileImageUrlHttps"
                )

            )
        )
        val s = mapper.writeValueAsString(twitterEvent)
        assertEquals(twitterEvent, mapper.readValue(s))
    }

    @Test
    fun testDirectMessageEventSentDeserialization() {
        val twitterEvent = mapper.readValue<IncomingEvent>(resourceAsStream("/direct_message_event_sent.json"))
        assertNotNull(twitterEvent)
    }

    @Test
    fun testDirectMessageEventReceivedDeserialization() {
        val twitterEvent = mapper.readValue<IncomingEvent>(resourceAsStream("/direct_message_event_received.json"))
        assertNotNull(twitterEvent)
    }

    @Test
    fun testTypingEventDeserialization() {
        val twitterEvent = mapper.readValue<IncomingEvent>(resourceAsStream("/direct_message_indicate_typing_event.json"))
        assertNotNull(twitterEvent)
    }

    @Test
    fun testOutcomingEventDeserialization() {
        val twitterEvent = mapper.readValue<OutcomingEvent>(resourceAsStream("/outcoming_direct_message.json"))
        assertNotNull(twitterEvent)
    }

    @Test
    fun testOutcomingEventSerialization() {
        val outcomingEvent = OutcomingEvent(
            DirectMessageOutcomingEvent(
                MessageCreate(Recipient("recipientId"), "senderId", "sourceAppId", MessageData("text"))
            )
        )
        val s = mapper.writeValueAsString(outcomingEvent)
        assertEquals(outcomingEvent, mapper.readValue(s))
    }

}