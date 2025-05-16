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

package ai.tock.bot.engine.nlp

import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.nlp.api.client.model.dump.ApplicationDump
import ai.tock.nlp.api.client.model.dump.IntentDefinition
import ai.tock.nlp.api.client.model.dump.SentencesDump
import ai.tock.shared.longProperty
import java.io.InputStream

/**
 * Sends NLP requests.
 */
interface NlpController {

    /**
     * Parses a sentence and set intent and entities in context.
     */
    fun parseSentence(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        dialog: Dialog,
        connector: ConnectorController,
        botDefinition: BotDefinition
    )

    /**
     * Marks the sentence as not understood in the nlp model.
     */
    fun markAsUnknown(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        botDefinition: BotDefinition
    )

    /**
     * Exports list of IntentDefinition
     *
     * @namespace Application Namespace
     * @name Application Name
     *
     * @return List of IntentDefinition
     */
    fun getIntentsByNamespaceAndName(namespace: String, name: String): List<IntentDefinition>?

    /**
     * Imports a NLP dump (configuration and sentences of NLP model).
     *
     * @return true if NLP model is modified, false either
     */
    fun importNlpDump(stream: InputStream): Boolean

    /**
     * Imports a NLP dump (configuration and sentences of NLP model).
     *
     * @param dump the dump to import
     * @return true if NLP model is modified, false either
     */
    fun importNlpPlainDump(dump: ApplicationDump): Boolean

    /**
     * Imports a NLP sentences dump (only validated sentences) - format is simpler than [ApplicationDump].
     *
     * @param dump the dump to import
     * @return true if NLP model is modified, false either
     */
    fun importNlpPlainSentencesDump(dump: SentencesDump): Boolean

    /**
     * Imports a NLP sentences dump (only validated sentences) - format is simpler than [ApplicationDump].
     *
     * @return true if NLP model is modified, false either
     */
    fun importNlpSentencesDump(stream: InputStream): Boolean

    /**
     * Tries to check nlp server, waiting 200 response or [timeToWaitInMs] before returning.
     */
    fun waitAvailability(timeToWaitInMs: Long = longProperty("tock_bot_wait_nlp_availability_in_ms", 5000L))
}
