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
import java.time.Instant

internal class UserTimelineCol(
        val _id: String,
        val playerId: PlayerId,
        val userPreferences: UserPreferences,
        val userState: UserStateWrapper,
        val applicationIds: MutableSet<String> = mutableSetOf(),
        var lastActionText: String? = null,
        val lastUpdateDate: Instant = Instant.now(),
        var lastUserActionDate: Instant = lastUpdateDate) {

    constructor(newTimeline: UserTimeline, oldTimeline: UserTimelineCol?) : this(
            newTimeline.playerId.id,
            newTimeline.playerId,
            newTimeline.userPreferences,
            UserStateWrapper(newTimeline.userState)
    ) {
        //register last action
        newTimeline.dialogs.lastOrNull()?.currentStory()?.actions?.lastOrNull { it.playerId.type == PlayerType.user }?.let {
            lastUserActionDate = it.date
            lastActionText = when (it) {
                is SendSentence -> it.text
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
        newTimeline.dialogs.lastOrNull()?.currentStory()?.actions?.forEach {
            applicationIds.add(it.applicationId)
        }
    }

    fun toUserTimeline(): UserTimeline {
        return UserTimeline(
                playerId,
                userPreferences,
                userState.toUserState()
        )
    }

    fun toUserReport(): UserReport {
        return UserReport(
                playerId,
                applicatigonIds,
                userPreferences,
                userState.toUserState(),
                lastUpdateDate,
                lastActionText,
                lastUserActionDate
        )
    }

    class UserStateWrapper(val creationDate: Instant = Instant.now(),
                           val lastUpdateDate: Instant = creationDate,
                           @JsonDeserialize(using = FlagsDeserializer::class)
                           val flags: Map<String, TimeBoxedFlagWrapper>) {
        constructor(state: UserState) :
                this(
                        state.creationDate,
                        Instant.now(),
                        state.flags.mapValues { TimeBoxedFlagWrapper(it.value) }
                )

        fun toUserState(): UserState {
            return UserState(
                    creationDate,
                    flags.mapValues { it.value.toTimeBoxedFlag() }.toMutableMap()
            )
        }
    }

    data class TimeBoxedFlagWrapper(val value: String,
                                    val expirationDate: Instant? = Instant.now()) {

        constructor(flag: TimeBoxedFlag) : this(flag.value, flag.expirationDate)

        fun toTimeBoxedFlag(): TimeBoxedFlag = TimeBoxedFlag(value, expirationDate)
    }

    class FlagsDeserializer : JsonDeserializer<Map<String, TimeBoxedFlagWrapper>>() {

        override fun deserialize(jp: JsonParser, context: DeserializationContext): Map<String, TimeBoxedFlagWrapper> {
            val mapper = jp.getCodec()
            return if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                mapper.readValue(jp, object : TypeReference<Map<String, TimeBoxedFlagWrapper>>() {})
            } else {
                //consume this stream
                mapper.readTree<TreeNode>(jp)
                emptyMap()
            }
        }
    }


}