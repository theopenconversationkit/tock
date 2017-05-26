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

package fr.vsct.tock.bot.engine

import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.nlp.api.client.model.NlpQuery
import fr.vsct.tock.nlp.api.client.model.NlpResult
import fr.vsct.tock.nlp.api.client.model.dump.ApplicationDump
import fr.vsct.tock.nlp.api.client.model.merge.ValuesMergeQuery
import fr.vsct.tock.nlp.api.client.model.merge.ValuesMergeResult
import java.io.InputStream

/**
 * Send NLP requests.
 */
interface NlpController {

    /**
     * Parse a sentence and set intent and entities in context.
     */
    fun parseSentence(sentence: SendSentence,
                      userTimeline: UserTimeline,
                      dialog: Dialog,
                      connector: ConnectorController,
                      botDefinition: BotDefinition)

    /**
     * Import a NLP dump (configuration and sentences of NLP model).
     * @return true if NLP model is modified, false either
     */
    fun importNlpDump(stream: InputStream): Boolean

    /**
     * Import a NLP dump (configuration and sentences of NLP model).
     * @return true if NLP model is modified, false either
     */
    fun importNlpPlainDump(dump: ApplicationDump): Boolean
}