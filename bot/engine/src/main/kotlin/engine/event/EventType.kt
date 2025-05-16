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

package ai.tock.bot.engine.event

/**
 * Define default event types.
 */
enum class EventType(val action: Boolean = true) {

    /**
     * [SendSentence] action type.
     */
    sentence,
    /**
      * [SentenceWithFootnotes] action type.
     */
    sentenceWithFootnotes,
    /**
     * [SendChoice] action type.
     */
    choice,
    /**
     * [SendAttachment] action type.
     */
    attachment,
    /**
     * [SendLocation] action type.
     */
    location,
    /**
     * [Debug] event type.
     */
    debug,
    /**
     * [SubscribingEvent] event type.
     */
    subscribing(false),
    /**
     * [TypingOnEvent] event type.
     */
    typingOn(false),
    /**
     * [TypingOffEvent] event type.
     */
    typingOff(false),
    /**
     * [MarkSeenEvent] event type.
     */
    markSeen(false),
    /**
     * [StartConversationEvent] event type.
     */
    startConversation(false),
    /**
     * [EndConversationEvent] event type.
     */
    endConversation(false),
    /**
     * [NoInputEvent] event type.
     */
    noInput(false),
    /**
     * [StartSessionEvent] event type.
     */
    startSession(false),
    /**
     * [EndSessionEvent] event type.
     */
    endSession(false),
    /**
     * [GetAppRolesEvent] event type.
     */
    getAppRoles(false),
    /**
     * [PassThreadControlEvent] event type.
     */
    passThreadControl(false),
    /**
     * [RequestThreadControlEvent] event type.
     */
    requestThreadControl(false),
    /**
     * [TakeThreadControlEvent] event type.
     */
    takeThreadControl(false),
    /**
     * [ReferralParamtersEvent] event type.
     */
    referralParamters(false)
}
