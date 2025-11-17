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

package ai.tock.bot.engine.nlp.engine.nlp

import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.DialogState
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.nlp.NlpEntityMergeContext
import ai.tock.bot.engine.nlp.NlpListener
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.nlp.api.client.model.NlpQuery
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import kotlinx.coroutines.runBlocking

/**
 * Used to customize behaviour of NLP parsing - and also to monitor NLP requests - on bot side.
 * Has to be registered using [ai.tock.bot.engine.BotRepository.registerNlpListener].
 */
@ExperimentalTockCoroutines
interface AsyncNlpListener : NlpListener {

    @Deprecated("Use coroutines to call this interface", replaceWith = ReplaceWith("detectKeyword(sentence)"))
    override fun handleKeyword(sentence: String): Intent? = runBlocking {
        detectKeyword(sentence)
    }

    /**
     * Used to handle "secret" keywords.
     *
     * @return null if no keyword is detected. If not null the nlp call is not started and the returned intent is used.
     */
    suspend fun detectKeyword(sentence: String): Intent? = null

    @Deprecated("Use coroutines to call this interface", replaceWith = ReplaceWith("precomputeNlp(sentence, userTimeline, dialog, botDefinition)"))
    override fun precompute(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        dialog: Dialog,
        botDefinition: BotDefinition
    ): NlpResult? = runBlocking {
        precomputeNlp(sentence, userTimeline, dialog, botDefinition)
    }

    /**
     * Precomputes NLP result - if this method returns null, the [NlpResult] is used and no NLP call is sent.
     *
     * Default returns null.
     */
    suspend fun precomputeNlp(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        dialog: Dialog,
        botDefinition: BotDefinition
    ): NlpResult? = null

    @Deprecated("Use coroutines to call this interface", replaceWith = ReplaceWith("updateNlpQuery(sentence, userTimeline, dialog, botDefinition, nlpQuery)"))
    override fun updateQuery(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        dialog: Dialog,
        botDefinition: BotDefinition,
        nlpQuery: NlpQuery
    ): NlpQuery = runBlocking {
        updateNlpQuery(sentence, userTimeline, dialog, botDefinition, nlpQuery)
    }

    /**
     * This method is automatically called by the bot before a NLP request is sent in order to update the NLP query parameters.
     * Overrides it if you need more control on NLP request.
     *
     *
     * Default returns [nlpQuery] without change.
     */
    suspend fun updateNlpQuery(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        dialog: Dialog,
        botDefinition: BotDefinition,
        nlpQuery: NlpQuery
    ): NlpQuery = nlpQuery

    @Deprecated("Use coroutines to call this interface", replaceWith = ReplaceWith("selectIntent(userTimeline, dialog, event, nlpResult)"))
    override fun findIntent(
        userTimeline: UserTimeline,
        dialog: Dialog,
        event: Event,
        nlpResult: NlpResult
    ): IntentAware? = runBlocking {
        selectIntent(userTimeline, dialog, event, nlpResult)
    }

    /**
     * This method is automatically called by the bot after a NLP request in order to select an intent.
     * Overrides it if you need more control on intent choice.
     *
     * If it returns null, [ai.tock.bot.definition.BotDefinition.findIntent] is called.
     *
     * Default returns null.
     */
    suspend fun selectIntent(userTimeline: UserTimeline, dialog: Dialog, event: Event, nlpResult: NlpResult): IntentAware? = null

    @Deprecated("Use coroutines to call this interface", replaceWith = ReplaceWith("postProcessEntities(userTimeline, dialog, event, nlpResult)"))
    override fun evaluateEntities(
        userTimeline: UserTimeline,
        dialog: Dialog,
        event: Event,
        nlpResult: NlpResult
    ): List<EntityValue> = runBlocking {
        processEntities(userTimeline, dialog, event, nlpResult)
    }

    /**
     * Allows custom entity evaluation - default returns empty list.
     */
    suspend fun processEntities(
        userTimeline: UserTimeline,
        dialog: Dialog,
        event: Event,
        nlpResult: NlpResult
    ): List<EntityValue> = emptyList()

    @Deprecated("Use coroutines to call this interface", replaceWith = ReplaceWith("sortEntitiesBeforeMerge(entities)"))
    override fun sortEntitiesToMerge(entities: List<NlpEntityMergeContext>): List<NlpEntityMergeContext> = runBlocking {
        sortEntitiesBeforeMerge(entities)
    }

    /**
     * Defines custom sort new entity values.
     * This is useful when you want to evaluate an entity role only after another entity role has been evaluated
     * (entity dependence use case).
     * Default does nothing.
     */
    suspend fun sortEntitiesBeforeMerge(entities: List<NlpEntityMergeContext>): List<NlpEntityMergeContext> = entities

    @Deprecated("Use coroutines to call this interface", replaceWith = ReplaceWith("tryMergeEntityValues(dialogState, action, entityToMerge)"))
    override fun mergeEntityValues(
        dialogState: DialogState,
        action: Action,
        entityToMerge: NlpEntityMergeContext
    ): NlpEntityMergeContext = runBlocking {
        tryMergeEntityValues(dialogState, action, entityToMerge)
    }

    /**
     * Allows to override [NlpEntityMergeContext] before trying to merge the entity context.
     */
    suspend fun tryMergeEntityValues(
        dialogState: DialogState,
        action: Action,
        entityToMerge: NlpEntityMergeContext
    ): NlpEntityMergeContext = entityToMerge

    @Deprecated("Use coroutines to call this interface", replaceWith = ReplaceWith("success(query, result)"))
    override fun success(query: NlpQuery, result: NlpResult) = runBlocking {
        onSuccess(query, result)
    }

    /**
     * Called when nlp request is successful.
     */
    suspend fun onSuccess(query: NlpQuery, result: NlpResult) = Unit

    @Deprecated("Use coroutines to call this interface", replaceWith = ReplaceWith("onError(query, dialog, throwable)"))
    override fun error(
        query: NlpQuery,
        dialog: Dialog,
        throwable: Throwable?
    ) = runBlocking {
        onError(query, dialog, throwable)
    }

    /**
     * Called when nlp request is throwing an error.
     *
     * Using this method, you can for example redirect to a custom story which will handle the error.
     *
     * ```
     * object MyNlpListener : NlpListener {
     *  override suspend fun onError(query: NlpQuery, dialog: Dialog, throwable: Throwable?) {
     *   super.error(query, dialog, throwable)
     *   dialog.state.currentIntent = bot.myCustomErrorStory.mainIntent()
     *  }
     * }
     * ```
     */
    suspend fun onError(query: NlpQuery, dialog: Dialog, throwable: Throwable?) = Unit
}
