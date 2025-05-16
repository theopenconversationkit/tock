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

package ai.tock.bot.mongo

import ai.tock.bot.admin.user.UserAnalytics
import ai.tock.bot.admin.user.UserReport
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendLocation
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.TimeBoxedFlag
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.engine.user.UserState
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.shared.defaultLocale
import ai.tock.shared.defaultZoneId
import ai.tock.shared.security.TockObfuscatorService.obfuscate
import ai.tock.shared.security.decrypt
import ai.tock.shared.security.encrypt
import ai.tock.shared.security.encryptionEnabled
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

@Data(internal = true)
@JacksonData(internal = true)
internal data class UserTimelineCol(
    val _id: Id<UserTimelineCol>,
    val playerId: PlayerId,
    val userPreferences: UserPreferencesWrapper,
    val userState: UserStateWrapper,
    val temporaryIds: MutableSet<String> = mutableSetOf(),
    val applicationIds: MutableSet<String> = mutableSetOf(),
    var lastActionText: String? = null,
    val lastUpdateDate: Instant = Instant.now(),
    var lastUserActionDate: Instant = lastUpdateDate,
    val namespace: String? = null,
    val creationDate: Instant = Instant.now()
) {

    constructor(timelineId: String, namespace: String, newTimeline: UserTimeline, oldTimeline: UserTimelineCol?) : this(
        timelineId.toId(),
        newTimeline.playerId,
        UserPreferencesWrapper(newTimeline.userPreferences),
        UserStateWrapper(newTimeline.userState),
        newTimeline.temporaryIds,
        namespace = namespace
    ) {
        // register last action
        newTimeline.lastUserAction
            ?.let {
                lastUserActionDate = it.date
                lastActionText = when (it) {
                    is SendSentence -> obfuscate(it.stringText, it.nlpStats?.obfuscatedRanges() ?: emptyList())
                    is SendChoice -> "(click) ${it.intentName}"
                    is SendAttachment -> "(send) ${it.url}"
                    is SendLocation -> "(send user location)"
                    else -> null
                }
            }
        // register application id
        oldTimeline?.let {
            applicationIds.addAll(it.applicationIds)
        }
        newTimeline.lastAction?.also {
            applicationIds.add(it.applicationId)
        }
    }

    fun toUserTimeline(): UserTimeline {
        return UserTimeline(
            playerId,
            userPreferences.toUserPreferences(),
            userState.toUserState(),
            temporaryIds = temporaryIds
        )
    }

    fun toUserReport(): UserReport {
        return UserReport(
            playerId,
            applicationIds,
            userPreferences.toUserPreferences(),
            userState.toUserState(),
            lastUpdateDate,
            lastActionText,
            lastUserActionDate
        )
    }

    fun toUserAnalytics(): UserAnalytics {
        val zoneId = defaultZoneId
        return UserAnalytics(
            playerId,
            applicationIds,
            LocalDateTime.ofInstant(lastUserActionDate, zoneId).toLocalDate(),
            LocalDateTime.ofInstant(lastUserActionDate, zoneId)
        )
    }

    @Data(internal = true)
    @JacksonData(internal = true)
    data class UserPreferencesWrapper(
        val firstName: String? = null,
        val lastName: String? = null,
        val email: String? = null,
        val timezone: ZoneId = defaultZoneId,
        val locale: Locale = defaultLocale,
        val picture: String? = null,
        val gender: String? = null,
        val initialLocale: Locale = locale,
        /**
         * Is it a test user?
         */
        val test: Boolean = false,
        val encrypted: Boolean = false
    ) {

        constructor(pref: UserPreferences) : this(
            pref.firstName?.let { if (encryptionEnabled) encrypt(it) else it },
            pref.lastName?.let { if (encryptionEnabled) encrypt(it) else it },
            pref.email?.let { if (encryptionEnabled) encrypt(it) else it },
            pref.timezone,
            pref.locale,
            pref.picture?.let { if (encryptionEnabled) encrypt(it) else it },
            pref.gender?.let { if (encryptionEnabled) encrypt(it) else it },
            pref.initialLocale,
            pref.test,
            encryptionEnabled
        )

        fun toUserPreferences(): UserPreferences {
            return UserPreferences(
                firstName?.let { if (encrypted) decrypt(it) else it },
                lastName?.let { if (encrypted) decrypt(it) else it },
                email?.let { if (encrypted) decrypt(it) else it },
                timezone,
                locale,
                picture?.let { if (encrypted) decrypt(it) else it },
                gender?.let { if (encrypted) decrypt(it) else it },
                test,
                initialLocale
            )
        }
    }

    @Data(internal = true)
    class UserStateWrapper(
        val creationDate: Instant = Instant.now(),
        val lastUpdateDate: Instant = creationDate,
        @JsonDeserialize(using = FlagsDeserializer::class)
        val flags: Map<String, TimeBoxedFlagWrapper>
    ) {
        constructor(state: UserState) :
            this(
                state.creationDate,
                Instant.now(),
                state.flags.mapValues {
                    TimeBoxedFlagWrapper(
                        it.value,
                        MongoBotConfiguration.hasToEncryptFlag(it.key)
                    )
                }
            )

        fun toUserState(): UserState {
            return UserState(
                creationDate,
                flags.mapValues { it.value.toTimeBoxedFlag() }.toMutableMap()
            )
        }
    }

    data class TimeBoxedFlagWrapper(
        val encrypted: Boolean = false,
        val value: String,
        val expirationDate: Instant? = Instant.now()
    ) {

        constructor(flag: TimeBoxedFlag, doEncrypt: Boolean) : this(
            doEncrypt,
            if (doEncrypt) encrypt(flag.value) else flag.value,
            flag.expirationDate
        )

        fun toTimeBoxedFlag(): TimeBoxedFlag = TimeBoxedFlag(
            decryptValue(),
            expirationDate
        )

        fun decryptValue(): String {
            return if (encrypted) {
                decrypt(value)
            } else {
                value
            }
        }
    }

    class FlagsDeserializer : JsonDeserializer<Map<String, TimeBoxedFlagWrapper>>() {

        companion object {
            val reference = object : TypeReference<Map<String, TimeBoxedFlagWrapper>>() {}
        }

        override fun deserialize(jp: JsonParser, context: DeserializationContext): Map<String, TimeBoxedFlagWrapper> {
            val mapper = jp.getCodec()
            return if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                mapper.readValue(jp, reference)
            } else {
                // consume this stream
                mapper.readTree<TreeNode>(jp)
                emptyMap()
            }
        }
    }
}
