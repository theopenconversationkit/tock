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

import ai.tock.bot.admin.annotation.BotAnnotation
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.nlp.NlpCallStats
import ai.tock.bot.engine.user.PlayerId
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

/**
 *
 */
internal class SendSentenceNotYetLoaded(
    val dialogId: Id<Dialog>,
    playerId: PlayerId,
    applicationId: String,
    recipientId: PlayerId,
    text: CharSequence?,
    id: Id<Action> = newId(),
    date: Instant = Instant.now(),
    state: EventState = EventState(),
    metadata: ActionMetadata = ActionMetadata(),
    val hasCustomMessage: Boolean = true,
    val hasNlpStats: Boolean = false,
    override var annotation: BotAnnotation? = null
) : SendSentence(playerId, applicationId, recipientId, text, mutableListOf(), id, date, state, metadata, null) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    internal var messageLoaded = !hasCustomMessage
    private val loadedMessages: MutableList<ConnectorMessage> = mutableListOf()

    internal fun setLoadedMessages(messages: List<ConnectorMessage>) {
        loadedMessages.clear()
        loadedMessages.addAll(messages)
        messageLoaded = true
    }

    override val messages: MutableList<ConnectorMessage>
        get() {
            if (!messageLoaded) {
                logger.debug { "load message for $id" }
                messageLoaded = true
                loadedMessages.addAll(runBlocking { UserTimelineMongoDAO.loadConnectorMessage(toActionId(), dialogId) } )
            }
            return loadedMessages
        }

    internal var nlpStatsLoaded = !hasNlpStats
    private var loadedNlpStats: NlpCallStats? = null

    override var nlpStats: NlpCallStats?
        get() {
            if (!nlpStatsLoaded) {
                logger.debug { "load nlpStats for $id" }
                nlpStatsLoaded = true
                loadedNlpStats = runBlocking { UserTimelineMongoDAO.loadNlpStats(toActionId(), dialogId) }
            }
            return loadedNlpStats
        }
        set(value) {
            this.loadedNlpStats = value
            nlpStatsLoaded = true
        }

    override fun toString(): String {
        return if (messageLoaded) super.toString() else "SendSentenceWithNotLoadedMessage(dialogId=$dialogId, messageLoaded=$messageLoaded, nlpStatsLoaded=$nlpStatsLoaded)"
    }
}
