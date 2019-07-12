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

package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import fr.vsct.tock.bot.admin.user.UserReport
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendLocation
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.TimeBoxedFlag
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.engine.user.UserState
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.defaultZoneId
import fr.vsct.tock.shared.security.TockObfuscatorService.obfuscate
import fr.vsct.tock.shared.security.decrypt
import fr.vsct.tock.shared.security.encrypt
import fr.vsct.tock.shared.security.encryptionEnabled
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.toId
import java.time.Instant
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
    var lastUserActionDate: Instant = lastUpdateDate
) {

    constructor(newTimeline: UserTimeline, oldTimeline: UserTimelineCol?) : this(
        newTimeline.playerId.id.toId(),
        newTimeline.playerId,
        UserPreferencesWrapper(newTimeline.userPreferences),
        UserStateWrapper(newTimeline.userState),
        newTimeline.temporaryIds
    ) {
        //register last action
        newTimeline.dialogs.lastOrNull()?.currentStory?.actions?.lastOrNull { it.playerId.type == PlayerType.user }
            ?.let {
                lastUserActionDate = it.date
                lastActionText = when (it) {
                    is SendSentence -> obfuscate(it.stringText)
                    is SendChoice -> "(click) ${it.intentName}"
                    is SendAttachment -> "(send) ${it.url}"
                    is SendLocation -> "(send user location)"
                    else -> null
                }
            }
        //register application id
        oldTimeline?.let {
            applicationIds.addAll(it.applicationIds)
        }
        newTimeline.dialogs.lastOrNull()?.currentStory?.actions?.forEach {
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

    @Data(internal = true)
    @JacksonData(internal = true)
    data class UserPreferencesWrapper(
        var firstName: String? = null,
        var lastName: String? = null,
        var email: String? = null,
        var timezone: ZoneId = defaultZoneId,
        var locale: Locale = defaultLocale,
        var picture: String? = null,
        var gender: String? = null,
        var initialLocale: Locale = locale,
        /**
         * Is it a test user?
         */
        var test: Boolean = false,
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
                //consume this stream
                mapper.readTree<TreeNode>(jp)
                emptyMap()
            }
        }
    }


}