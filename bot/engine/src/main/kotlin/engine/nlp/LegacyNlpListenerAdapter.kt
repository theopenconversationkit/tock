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

@OptIn(ExperimentalTockCoroutines::class)
internal class LegacyNlpListenerAdapter(private val wrapped: NlpListener) : AsyncNlpListener {
    override suspend fun detectKeyword(sentence: String): Intent? {
        return wrapped.handleKeyword(sentence)
    }

    override suspend fun precompute(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        dialog: Dialog,
        botDefinition: BotDefinition
    ): NlpResult? {
        return wrapped.precompute(sentence, userTimeline, dialog, botDefinition)
    }

    override suspend fun updateQuery(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        dialog: Dialog,
        botDefinition: BotDefinition,
        nlpQuery: NlpQuery
    ): NlpQuery {
        return wrapped.updateQuery(sentence, userTimeline, dialog, botDefinition, nlpQuery)
    }

    override suspend fun findIntent(
        userTimeline: UserTimeline,
        dialog: Dialog,
        event: Event,
        nlpResult: NlpResult
    ): IntentAware? {
        return wrapped.findIntent(userTimeline, dialog, event, nlpResult)
    }

    override suspend fun evaluateEntities(
        userTimeline: UserTimeline,
        dialog: Dialog,
        event: Event,
        nlpResult: NlpResult
    ): List<EntityValue> {
        return wrapped.evaluateEntities(userTimeline, dialog, event, nlpResult)
    }

    override suspend fun sortEntitiesBeforeMerge(entities: List<NlpEntityMergeContext>): List<NlpEntityMergeContext> {
        return wrapped.sortEntitiesToMerge(entities)
    }

    override suspend fun mergeEntityValues(
        dialogState: DialogState,
        action: Action,
        entityToMerge: NlpEntityMergeContext
    ): NlpEntityMergeContext {
        return wrapped.mergeEntityValues(dialogState, action, entityToMerge)
    }

    override suspend fun onSuccess(
        query: NlpQuery,
        result: NlpResult
    ) {
        wrapped.success(query, result)
    }

    override suspend fun onError(
        query: NlpQuery,
        dialog: Dialog,
        throwable: Throwable?
    ) {
        wrapped.error(query, dialog, throwable)
    }
}