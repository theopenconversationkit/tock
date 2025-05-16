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

package ai.tock.bot.connector.twitter.ai.tock.bot.connector.twitter.json

import ai.tock.bot.connector.twitter.model.Application
import ai.tock.bot.connector.twitter.model.DirectMessage
import ai.tock.bot.connector.twitter.model.DirectMessageIndicateTyping
import ai.tock.bot.connector.twitter.model.Entities
import ai.tock.bot.connector.twitter.model.Hashtag
import ai.tock.bot.connector.twitter.model.Mention
import ai.tock.bot.connector.twitter.model.MessageCreate
import ai.tock.bot.connector.twitter.model.MessageData
import ai.tock.bot.connector.twitter.model.Option
import ai.tock.bot.connector.twitter.model.OptionWithoutDescription
import ai.tock.bot.connector.twitter.model.Options
import ai.tock.bot.connector.twitter.model.Recipient
import ai.tock.bot.connector.twitter.model.Symbol
import ai.tock.bot.connector.twitter.model.Tweet
import ai.tock.bot.connector.twitter.model.Url
import ai.tock.bot.connector.twitter.model.User
import ai.tock.bot.connector.twitter.model.incoming.DirectMessageIncomingEvent
import ai.tock.bot.connector.twitter.model.incoming.DirectMessageIndicateTypingIncomingEvent
import ai.tock.bot.connector.twitter.model.incoming.IncomingEvent
import ai.tock.bot.connector.twitter.model.incoming.TweetIncomingEvent
import ai.tock.shared.jackson.mapper
import ai.tock.shared.resourceAsStream
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Date

internal class IncomingEventDeserializationTest {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Test
    fun `serialize deserialize direct message`() {
        val twitterEvent = DirectMessageIncomingEvent(
            forUserId = "forUserId",
            directMessages = listOf(
                DirectMessage(
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
                                symbols = listOf(Symbol(listOf(0, 5), "test")),
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
                    lang = "fr",
                    location = "",
                    name = "name",
                    screenName = "screenName",
                    protected = false,
                    verified = true,
                    followersCount = 100,
                    friendsCount = 100,
                    statusesCount = 100,
                    profileImageUrlHttps = "https://photo.com"
                ),
                "user2" to User(
                    id = "id",
                    lang = "fr",
                    location = "",
                    name = "name",
                    screenName = "screenName",
                    protected = false,
                    verified = true,
                    followersCount = 100,
                    friendsCount = 100,
                    statusesCount = 100,
                    profileImageUrlHttps = "https://photo.com"
                )

            )
        )
        val s = mapper.writeValueAsString(twitterEvent)
        assertThat(mapper.readValue<DirectMessageIncomingEvent>(s)).isEqualTo(twitterEvent)
    }

    @Test
    fun `direct message event sent`() {
        val expected = DirectMessageIncomingEvent(
            forUserId = "14235326",
            directMessages = listOf(
                DirectMessage(
                    id = "1090238550052880394",
                    created = 1548768100642,
                    messageCreated = MessageCreate(
                        target = Recipient(
                            recipientId = "707685546848550916"
                        ),
                        senderId = "14235326",
                        sourceAppId = "258901",
                        messageData = MessageData(
                            text = "Test #test @virginie_buchin https://t.co/N3w1YJ9hVr",
                            entities = Entities(
                                hashtags = listOf(Hashtag("test", listOf(5, 10))),
                                mentions = listOf(Mention("virginie_buchin", "Virginie", "707685546848550900", "707685546848550916", listOf(11, 27))),
                                urls = listOf(Url("https://t.co/N3w1YJ9hVr", "http://www.oui.sncf", "oui.sncf", listOf(28, 51))),
                                symbols = emptyList()
                            )
                        )
                    )
                )
            ),
            apps = mapOf(
                "258901" to Application(
                    id = "258901",
                    name = "Twitter for Android",
                    url = "http://twitter.com/download/android"
                )
            ),
            users = mapOf(
                "707685546848550916" to User(
                    id = "707685546848550916",
                    lang = null,
                    location = null,
                    createdTimestamp = 1457560357426,
                    name = "Virginie",
                    screenName = "virginie_buchin",
                    protected = false,
                    verified = false,
                    followersCount = 7,
                    friendsCount = 26,
                    statusesCount = 3,
                    profileImageUrlHttps = "https://abs.twimg.com/sticky/default_profile_images/default_profile_normal.png"
                ),
                "14235326" to User(
                    id = "14235326",
                    lang = null,
                    location = null,
                    createdTimestamp = 1206626473000,
                    name = "LE BESCOND Erwan",
                    screenName = "bagou",
                    protected = false,
                    verified = false,
                    followersCount = 86,
                    friendsCount = 104,
                    statusesCount = 229,
                    profileImageUrlHttps = "https://pbs.twimg.com/profile_images/378800000245800156/bbe387dd6a6d46f22a228a259b355df5_normal.jpeg"
                )
            )
        )
        val deserializedEvent = mapper.readValue<IncomingEvent>(resourceAsStream("/direct_message_event_sent.json"))
        assertThat(deserializedEvent).isEqualTo(expected)
    }

    @Test
    fun `direct message event sent with options`() {
        val expected = DirectMessageIncomingEvent(
            forUserId = "1090334520887386112",
            directMessages = listOf(
                DirectMessage(
                    id = "1167008736986226692",
                    created = 1567071539528,
                    messageCreated = MessageCreate(
                        target = Recipient(
                            recipientId = "1100452418326806530"
                        ),
                        senderId = "1090334520887386112",
                        sourceAppId = "16132880",
                        messageData = MessageData(
                            text = "Bonjour bag0u1, je suis l'Agent virtuel SNCF !",
                            entities = Entities(
                                hashtags = listOf(),
                                mentions = listOf(),
                                urls = listOf(),
                                symbols = emptyList()
                            ),
                            quickReply = Options(
                                listOf(
                                    Option.of(
                                        "\uD83D\uDC64 Parler à un agent",
                                        "description",
                                        "speak_agent?_previous_intent=greetings"
                                    ),
                                    Option.of("🚦 Info trafic", "description", "train_is_running?_previous_intent=greetings")
                                )
                            )
                        )
                    )
                )
            ),
            apps = mapOf(
                "16132880" to Application(
                    id = "16132880",
                    name = "aiv-local",
                    url = "https://www.sncf.com"
                )
            ),
            users = mapOf(
                "1090334520887386112" to User(
                    id = "1090334520887386112",
                    lang = null,
                    location = "Paris, France",
                    createdTimestamp = 1548790982035,
                    name = "bag0u",
                    screenName = "bag0u",
                    protected = false,
                    verified = false,
                    followersCount = 2,
                    friendsCount = 2,
                    statusesCount = 58,
                    profileImageUrlHttps = "https://abs.twimg.com/sticky/default_profile_images/default_profile_normal.png"
                ),
                "1100452418326806530" to User(
                    id = "1100452418326806530",
                    lang = null,
                    location = null,
                    createdTimestamp = 1551203276725,
                    name = "bag0u1",
                    screenName = "bag0u1",
                    protected = false,
                    verified = false,
                    followersCount = 1,
                    friendsCount = 2,
                    statusesCount = 83,
                    profileImageUrlHttps = "https://abs.twimg.com/sticky/default_profile_images/default_profile_normal.png"
                )
            )
        )
        val deserializedEvent = mapper.readValue<IncomingEvent>(resourceAsStream("/direct_message_event_with_options_sent.json"))
        assertThat(deserializedEvent).isEqualTo(expected)
    }

    @Test
    fun `direct message event sent with options without description`() {
        val expected = DirectMessageIncomingEvent(
            forUserId = "1090334520887386112",
            directMessages = listOf(
                DirectMessage(
                    id = "1167008736986226692",
                    created = 1567071539528,
                    messageCreated = MessageCreate(
                        target = Recipient(
                            recipientId = "1100452418326806530"
                        ),
                        senderId = "1090334520887386112",
                        sourceAppId = "16132880",
                        messageData = MessageData(
                            text = "Bonjour bag0u1, je suis l'Agent virtuel SNCF !",
                            entities = Entities(
                                hashtags = listOf(),
                                mentions = listOf(),
                                urls = listOf(),
                                symbols = emptyList()
                            ),
                            quickReply = Options(
                                listOf(
                                    OptionWithoutDescription.of("\uD83D\uDC64 Parler à un agent", "speak_agent?_previous_intent=greetings"),
                                    OptionWithoutDescription.of("🚦 Info trafic", "train_is_running?_previous_intent=greetings")
                                )
                            )
                        )
                    )
                )
            ),
            apps = mapOf(
                "16132880" to Application(
                    id = "16132880",
                    name = "aiv-local",
                    url = "https://www.sncf.com"
                )
            ),
            users = mapOf(
                "1090334520887386112" to User(
                    id = "1090334520887386112",
                    lang = null,
                    location = "Paris, France",
                    createdTimestamp = 1548790982035,
                    name = "bag0u",
                    screenName = "bag0u",
                    protected = false,
                    verified = false,
                    followersCount = 2,
                    friendsCount = 2,
                    statusesCount = 58,
                    profileImageUrlHttps = "https://abs.twimg.com/sticky/default_profile_images/default_profile_normal.png"
                ),
                "1100452418326806530" to User(
                    id = "1100452418326806530",
                    lang = null,
                    location = null,
                    createdTimestamp = 1551203276725,
                    name = "bag0u1",
                    screenName = "bag0u1",
                    protected = false,
                    verified = false,
                    followersCount = 1,
                    friendsCount = 2,
                    statusesCount = 83,
                    profileImageUrlHttps = "https://abs.twimg.com/sticky/default_profile_images/default_profile_normal.png"
                )
            )
        )
        val deserializedEvent = mapper.readValue<IncomingEvent>(resourceAsStream("/direct_message_event_with_options_without_description_sent.json"))
        assertThat(deserializedEvent).isEqualTo(expected)
    }

    @Test
    fun `direct message event received`() {
        val expected = DirectMessageIncomingEvent(
            forUserId = "14235326",
            directMessages = listOf(
                DirectMessage(
                    id = "1090001495532191748",
                    created = 1548711582440,
                    messageCreated = MessageCreate(
                        target = Recipient(
                            recipientId = "14235326"
                        ),
                        senderId = "707685546848550916",
                        messageData = MessageData(
                            text = "Plouf",
                            entities = Entities(
                                hashtags = emptyList(),
                                mentions = emptyList(),
                                urls = emptyList(),
                                symbols = emptyList()
                            )
                        )
                    )
                )
            ),
            apps = null,
            users = mapOf(
                "707685546848550916" to User(
                    id = "707685546848550916",
                    lang = null,
                    location = null,
                    createdTimestamp = 1457560357426,
                    name = "Virginie",
                    screenName = "virginie_buchin",
                    protected = false,
                    verified = false,
                    followersCount = 7,
                    friendsCount = 26,
                    statusesCount = 3,
                    profileImageUrlHttps = "https://abs.twimg.com/sticky/default_profile_images/default_profile_normal.png"
                ),
                "14235326" to User(
                    id = "14235326",
                    lang = null,
                    location = null,
                    createdTimestamp = 1206626473000,
                    name = "LE BESCOND Erwan",
                    screenName = "bagou",
                    protected = false,
                    verified = false,
                    followersCount = 86,
                    friendsCount = 104,
                    statusesCount = 229,
                    profileImageUrlHttps = "https://pbs.twimg.com/profile_images/378800000245800156/bbe387dd6a6d46f22a228a259b355df5_normal.jpeg"
                )
            )
        )
        val deserializedEvent = mapper.readValue<IncomingEvent>(resourceAsStream("/direct_message_event_received.json"))
        assertThat(deserializedEvent).isEqualTo(expected)
    }

    @Test
    fun `direct message indicate typing event`() {
        val expected = DirectMessageIndicateTypingIncomingEvent(
            forUserId = "14235326",
            directMessagesIndicateTyping = listOf(
                DirectMessageIndicateTyping(
                    created = 1548711576930,
                    senderId = "707685546848550916",
                    target = Recipient(
                        recipientId = "14235326"
                    )
                )
            ),
            users = mapOf(
                "707685546848550916" to User(
                    id = "707685546848550916",
                    lang = null,
                    location = null,
                    createdTimestamp = 1457560357426,
                    name = "Virginie",
                    screenName = "virginie_buchin",
                    protected = false,
                    verified = false,
                    followersCount = 7,
                    friendsCount = 26,
                    statusesCount = 3,
                    profileImageUrlHttps = "https://abs.twimg.com/sticky/default_profile_images/default_profile_normal.png"
                ),
                "14235326" to User(
                    id = "14235326",
                    lang = null,
                    location = null,
                    createdTimestamp = 1206626473000,
                    name = "LE BESCOND Erwan",
                    screenName = "bagou",
                    protected = false,
                    verified = false,
                    followersCount = 86,
                    friendsCount = 104,
                    statusesCount = 229,
                    profileImageUrlHttps = "https://pbs.twimg.com/profile_images/378800000245800156/bbe387dd6a6d46f22a228a259b355df5_normal.jpeg"
                )
            )
        )
        val deserializedEvent = mapper.readValue<IncomingEvent>(resourceAsStream("/direct_message_indicate_typing_event.json"))
        assertThat(deserializedEvent).isEqualTo(expected)
    }

    @Test
    fun `serialize deserialize tweet`() {
        val twitterEvent = TweetIncomingEvent(
            forUserId = "toto",
            tweets = listOf(
                Tweet(
                    created = "Fri Apr 26 12:53:39 +0000 2019",
                    id = 110L,
                    text = "Mon Tweet",
                    lang = "fr",
                    truncated = false,
                    user = User(
                        id = "id",
                        lang = "fr",
                        location = "",
                        name = "name",
                        screenName = "screenName",
                        protected = false,
                        verified = true,
                        followersCount = 100,
                        friendsCount = 100,
                        statusesCount = 100,
                        profileImageUrlHttps = "https://photo.com"
                    ),
                    isQuote = false,
                    entities = Entities(
                        hashtags = listOf(Hashtag("tag1", listOf(1, 5)), Hashtag("tag2", listOf(1, 5))),
                        symbols = listOf(Symbol(listOf(0, 5), "test")),
                        mentions = listOf(Mention("screenName", "name", "id", "idStr", listOf(1, 5))),
                        urls = listOf(Url("url", "expandedUrl", "displayUrl", listOf(1, 5)))
                    ),
                    extendedEntities = Entities(
                        hashtags = listOf(Hashtag("tag1", listOf(1, 5)), Hashtag("tag2", listOf(1, 5))),
                        symbols = listOf(Symbol(listOf(0, 5), "test")),
                        mentions = listOf(Mention("screenName", "name", "id", "idStr", listOf(1, 5))),
                        urls = listOf(Url("url", "expandedUrl", "displayUrl", listOf(1, 5)))
                    ),
                    inReplyToStatusId = 10L
                )
            )
        )
        val s = mapper.writeValueAsString(twitterEvent)

        assertThat(mapper.readValue<TweetIncomingEvent>(s)).isEqualTo(twitterEvent)
    }

    @Test
    fun `incoming tweet with mention`() {
        val expected = TweetIncomingEvent(
            forUserId = "602907365",
            tweets = listOf(
                Tweet(
                    created = "Fri May 03 12:20:30 +0000 2019",
                    id = 1124287614780047400,
                    text = "Hello @Delphes99 alors ?",
                    lang = "fr",
                    truncated = false,
                    user = User(
                        id = "14235326",
                        lang = "fr",
                        location = null,
                        created = "Thu Mar 27 14:01:13 +0000 2008",
                        name = "LE BESCOND Erwan",
                        screenName = "bagou",
                        protected = false,
                        verified = false,
                        followersCount = 89,
                        friendsCount = 123,
                        statusesCount = 239,
                        profileImageUrlHttps = "https://pbs.twimg.com/profile_images/378800000245800156/bbe387dd6a6d46f22a228a259b355df5_normal.jpeg"
                    ),
                    isQuote = false,
                    entities = Entities(
                        hashtags = emptyList(),
                        mentions = listOf(
                            Mention(
                                screenName = "Delphes99",
                                name = "Laurent Gautho-lapeyre aka Delphes",
                                id = "602907365",
                                idStr = "602907365",
                                indices = listOf(6, 16)
                            )
                        ),
                        urls = emptyList(),
                        symbols = emptyList()
                    )
                )
            )
        )
        val deserializedEvent = mapper.readValue<IncomingEvent>(resourceAsStream("/incoming_tweet_with_mention.json"))
        assertThat(deserializedEvent).isEqualTo(expected)
    }

    @Test
    fun `incoming tweet with mention (reply)`() {
        val expected = TweetIncomingEvent(
            forUserId = "602907365",
            tweets = listOf(
                Tweet(
                    created = "Fri May 03 09:54:52 +0000 2019",
                    id = 1124250964289044500,
                    text = "@Delphes99 alors?",
                    lang = "fr",
                    truncated = false,
                    user = User(
                        id = "14235326",
                        lang = "fr",
                        location = null,
                        created = "Thu Mar 27 14:01:13 +0000 2008",
                        name = "LE BESCOND Erwan",
                        screenName = "bagou",
                        protected = false,
                        verified = false,
                        followersCount = 89,
                        friendsCount = 123,
                        statusesCount = 239,
                        profileImageUrlHttps = "https://pbs.twimg.com/profile_images/378800000245800156/bbe387dd6a6d46f22a228a259b355df5_normal.jpeg"
                    ),
                    isQuote = false,
                    inReplyToUserId = 602907365,
                    contributors = "Delphes99",
                    entities = Entities(
                        hashtags = emptyList(),
                        mentions = listOf(
                            Mention(
                                screenName = "Delphes99",
                                name = "Laurent Gautho-lapeyre aka Delphes",
                                id = "602907365",
                                idStr = "602907365",
                                indices = listOf(0, 10)
                            )
                        ),
                        urls = emptyList(),
                        symbols = emptyList()
                    )
                )
            )
        )
        val deserializedEvent = mapper.readValue<IncomingEvent>(resourceAsStream("/incoming_tweet_with_mention_reply.json"))
        assertThat(deserializedEvent).isEqualTo(expected)
    }

    @Test
    fun `incoming tweet sent`() {
        val expected = TweetIncomingEvent(
            forUserId = "602907365",
            tweets = listOf(
                Tweet(
                    created = "Fri May 03 14:16:13 +0000 2019",
                    id = 1124316737317548000,
                    text = "hello @chabott4",
                    lang = "en",
                    truncated = false,
                    user = User(
                        id = "602907365",
                        lang = "fr",
                        location = "Paris, France",
                        created = "Fri Jun 08 17:01:14 +0000 2012",
                        url = "http://www.delphes-at-home.net",
                        description = "Software developer-crafter @ArollaFr / player-gamer / AFOL / collector...",
                        name = "Laurent Gautho-lapeyre aka Delphes",
                        screenName = "Delphes99",
                        protected = false,
                        verified = false,
                        followersCount = 79,
                        friendsCount = 162,
                        statusesCount = 267,
                        profileImageUrlHttps = "https://pbs.twimg.com/profile_images/572053989202472960/55VgnNvn_normal.jpeg"
                    ),
                    isQuote = false,
                    entities = Entities(
                        hashtags = emptyList(),
                        mentions = listOf(
                            Mention(
                                screenName = "chabott4",
                                name = "chabotté",
                                id = "1121407864646656000",
                                idStr = "1121407864646656000",
                                indices = listOf(6, 15)
                            )
                        ),
                        urls = emptyList(),
                        symbols = emptyList()
                    )
                )
            )
        )
        val deserializedEvent = mapper.readValue<IncomingEvent>(resourceAsStream("/incoming_tweet_sent.json"))
        assertThat(deserializedEvent).isEqualTo(expected)
    }
}
