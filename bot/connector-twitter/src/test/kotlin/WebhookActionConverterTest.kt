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

package ai.tock.bot.connector.twitter

import ai.tock.bot.connector.twitter.model.Application
import ai.tock.bot.connector.twitter.model.DirectMessage
import ai.tock.bot.connector.twitter.model.DirectMessageIndicateTyping
import ai.tock.bot.connector.twitter.model.Entities
import ai.tock.bot.connector.twitter.model.Hashtag
import ai.tock.bot.connector.twitter.model.Mention
import ai.tock.bot.connector.twitter.model.MessageCreate
import ai.tock.bot.connector.twitter.model.MessageData
import ai.tock.bot.connector.twitter.model.Recipient
import ai.tock.bot.connector.twitter.model.Tweet
import ai.tock.bot.connector.twitter.model.Url
import ai.tock.bot.connector.twitter.model.User
import ai.tock.bot.connector.twitter.model.incoming.DirectMessageIncomingEvent
import ai.tock.bot.connector.twitter.model.incoming.DirectMessageIndicateTypingIncomingEvent
import ai.tock.bot.connector.twitter.model.incoming.TweetIncomingEvent
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.ActionQuote
import ai.tock.bot.engine.action.ActionReply
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class WebhookActionConverterTest {
    @Test
    fun `ignore direct message sent from the account listened`() {
        val directMessageSent = DirectMessageIncomingEvent(
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

        val event = directMessageSent.toEvent("appId")
        assertThat(event).isNull()
    }

    @Test
    fun `direct message event received`() {
        val directMessageReceived = DirectMessageIncomingEvent(
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

        val event = directMessageReceived.toEvent("appId") as SendSentence
        val expectedEvent = SendSentence(
            PlayerId("707685546848550916"),
            "appId",
            PlayerId("14235326"),
            "Plouf",
            metadata = ActionMetadata(
                visibility = ActionVisibility.PRIVATE
            )
        )

        assertThat(event).isEqualToIgnoringGivenFields(
            expectedEvent,
            "id", "date"
        )
        assertThat(event.playerId.type).isEqualTo(PlayerType.user)
        assertThat(event.recipientId.type).isEqualTo(PlayerType.bot)
    }

    @Test
    fun `ignoring direct message indicate typing event`() {
        val typingEvent = DirectMessageIndicateTypingIncomingEvent(
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

        val event = typingEvent.toEvent("appId")
        assertThat(event).isNull()
    }

    @Test
    fun `incoming tweet with mention`() {
        val tweetIncomingEvent = TweetIncomingEvent(
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
                    isQuote = true,
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

        val event = tweetIncomingEvent.toEvent("appId") as SendSentence
        val expectedEvent = SendSentence(
            PlayerId("14235326", PlayerType.bot),
            "appId",
            PlayerId("602907365", PlayerType.user),
            "Hello @Delphes99 alors ?",
            metadata = ActionMetadata(
                visibility = ActionVisibility.PUBLIC,
                replyMessage = ActionReply.NOREPLY,
                quoteMessage = ActionQuote.ISQUOTE
            )
        )

        assertThat(event).isEqualToIgnoringGivenFields(
            expectedEvent,
            "id", "date"
        )
        assertThat(event.playerId.type).isEqualTo(PlayerType.user)
        assertThat(event.recipientId.type).isEqualTo(PlayerType.bot)
    }

    @Test
    fun `incoming tweet with mention (reply to status)`() {
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
                    inReplyToStatusId = 6029073651,
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

        val event = expected.toEvent("appId") as SendSentence
        val expectedEvent = SendSentence(
            PlayerId("14235326", PlayerType.bot),
            "appId",
            PlayerId("602907365", PlayerType.user),
            "@Delphes99 alors?",
            metadata = ActionMetadata(
                visibility = ActionVisibility.PUBLIC,
                replyMessage = ActionReply.ISREPLY,
                quoteMessage = ActionQuote.NOQUOTE
            )
        )

        assertThat(event).isEqualToIgnoringGivenFields(
            expectedEvent,
            "id", "date"
        )
        assertThat(event.playerId.type).isEqualTo(PlayerType.user)
        assertThat(event.recipientId.type).isEqualTo(PlayerType.bot)
    }

    @Test
    fun `ignore tweet sent from the account listened`() {
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

        val event = expected.toEvent("appId")
        assertThat(event).isNull()
    }
}
