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
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.DialogState
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.nlp.api.client.model.NlpQuery
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.shared.coroutines.ExperimentalTockCoroutines

/**
 * Used to customize behaviour of NLP parsing - and also to monitor NLP requests - on bot side.
 * Has to be registered using [ai.tock.bot.engine.BotRepository.registerNlpListener].
 */
@ExperimentalTockCoroutines
interface AsyncNlpListener {

    /**
     * Used to handle "secret" keywords.
     *
     * @return null if no keyword is detected. If not null the nlp call is not started and the returned intent is used.
     */
    suspend fun detectKeyword(sentence: String): Intent? = null

    /**
     * Precomputes NLP result - if this method returns null, the [ai.tock.nlp.api.client.model.NlpResult] is used and no NLP call is sent.
     *
     * Default returns null.
     */
    suspend fun precompute(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        dialog: Dialog,
        botDefinition: BotDefinition
    ): NlpResult? = null

    /**
     * This method is automatically called by the bot before a NLP request is sent in order to update the NLP query parameters.
     * Overrides it if you need more control on NLP request.
     *
     *
     * Default returns [nlpQuery] without change.
     */
    suspend fun updateQuery(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        dialog: Dialog,
        botDefinition: BotDefinition,
        nlpQuery: NlpQuery
    ): NlpQuery = nlpQuery

    /**
     * This method is automatically called by the bot after a NLP request in order to select an intent.
     * Overrides it if you need more control on intent choice.
     *
     * If it returns null, [BotDefinition.findIntent] is called.
     *
     * Default returns null.
     */
    suspend fun findIntent(
        userTimeline: UserTimeline,
        dialog: Dialog,
        event: Event,
        nlpResult: NlpResult
    ): IntentAware? = null

    /**
     * Allows custom entity evaluation - default returns empty list.
     */
    suspend fun evaluateEntities(
        userTimeline: UserTimeline,
        dialog: Dialog,
        event: Event,
        nlpResult: NlpResult
    ): List<EntityValue> = emptyList()

    /**
     * Defines custom sort new entity values.
     * This is useful when you want to evaluate an entity role only after another entity role has been evaluated
     * (entity dependence use case).
     * Default does nothing.
     */
    suspend fun sortEntitiesBeforeMerge(entities: List<NlpEntityMergeContext>): List<NlpEntityMergeContext> = entities

    /**
     * Allows to override [NlpEntityMergeContext] before trying to merge the entity context.
     */
    suspend fun mergeEntityValues(
        dialogState: DialogState,
        action: Action,
        entityToMerge: NlpEntityMergeContext
    ): NlpEntityMergeContext = entityToMerge

    /**
     * Called when nlp request is successful.
     */
    suspend fun onSuccess(query: NlpQuery, result: NlpResult) = Unit

    /**
     * Called when nlp request is throwing an error.
     *
     * Using this method, you can for example redirect to a custom story which will handle the error.
     *
     * ```
     * object MyNlpListener : AsyncNlpListener {
     *  override suspend fun onError(query: NlpQuery, dialog: Dialog, throwable: Throwable?) {
     *   super.onError(query, dialog, throwable)
     *   dialog.state.currentIntent = bot.myCustomErrorStory.mainIntent()
     *  }
     * }
     * ```
     */
    suspend fun onError(query: NlpQuery, dialog: Dialog, throwable: Throwable?) = Unit
}
