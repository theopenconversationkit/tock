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

package fr.vsct.tock.bot.engine.action

import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.dialog.ActionState
import fr.vsct.tock.bot.engine.dialog.BotMetadata
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.shared.Dice
import java.net.URLDecoder.decode
import java.net.URLEncoder.encode
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant

/**
 * A user choice (click on a button or direct action).
 */
class SendChoice(playerId: PlayerId,
                 applicationId: String,
                 recipientId: PlayerId,
                 val intentName: String,
                 val parameters: Map<String, String> = emptyMap(),
                 id: String = Dice.newId(),
                 date: Instant = Instant.now(),
                 state: ActionState = ActionState(),
                 botMetadata: BotMetadata = BotMetadata()) : Action(playerId, recipientId, applicationId, id, date, state, botMetadata) {

    companion object {

        fun encodeChoiceId(storyDefinition: StoryDefinition, parameters: Map<String, String> = emptyMap()): String {
            return encodeChoiceId(storyDefinition.starterIntents.first(), parameters)
        }

        fun encodeChoiceId(intent: Intent, parameters: Map<String, String> = emptyMap()): String {
            return StringBuilder().apply {
                append(intent.name)
                if (parameters.isNotEmpty()) {
                    parameters.map { e ->
                        "${encode(e.key, UTF_8.name())}=${encode(e.value, UTF_8.name())}"
                    }.joinTo(this, "&", "?")
                }
            }.toString()
        }

        fun decodeChoiceId(id: String): Pair<String, Map<String, String>> {
            val questionMarkIndex = id.indexOf("?")
            return if (questionMarkIndex == -1) {
                id to emptyMap()
            } else {
                id.substring(0, questionMarkIndex) to id.substring(questionMarkIndex + 1)
                        .split("&")
                        .map {
                            it.split("=")
                                    .let { decode(it[0], UTF_8.name()) to decode(it[1], UTF_8.name()) }
                        }.toMap()
            }
        }

    }

    override fun toMessage(): Message {
        return Choice(intentName, parameters)
    }
}