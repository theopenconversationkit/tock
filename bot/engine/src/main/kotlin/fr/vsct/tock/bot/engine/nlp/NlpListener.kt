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

package fr.vsct.tock.bot.engine.nlp

import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.EntityValue
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.nlp.api.client.model.NlpQuery
import fr.vsct.tock.nlp.api.client.model.NlpResult

/**
 * Used to customize behaviour of NLP parsing - and also to monitor NLP requests - on bot side.
 * Need to be registered using [fr.vsct.tock.bot.engine.BotRepository.registerNlpListener].
 */
interface NlpListener {

    /**
     * Used to handle "secret" keywords.
     *
     * @return null if no keyword is detected. If not null the nlp call is not started and the returned intent is used.
     */
    fun handleKeyword(sentence: String): Intent? = null

    /**
     * This is the method called by the bot after a NLP request to choose an intent.
     * Overrides it if you need more control on intent choice.
     *
     * If it returns null, [BotDefinition.findIntent] is called.
     *
     * Default returns null.
     */
    fun findIntent(userTimeline: UserTimeline, dialog: Dialog, nlpResult: NlpResult): IntentAware? = null

    /**
     * Allow custom entity evaluation - default returns empty list.
     */
    fun evaluateEntities(userTimeline: UserTimeline, dialog: Dialog, nlpResult: NlpResult): List<EntityValue> =
        emptyList()

    /**
     * Called when nlp request is successful.
     */
    fun success(query: NlpQuery, result: NlpResult) = Unit

    /**
     * Called when nlp request is throwing an error.
     */
    fun error(query: NlpQuery, throwable: Throwable?) = Unit
}