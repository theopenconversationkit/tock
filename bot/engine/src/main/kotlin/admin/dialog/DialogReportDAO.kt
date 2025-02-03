/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.admin.dialog

import ai.tock.bot.admin.annotation.BotAnnotation
import ai.tock.bot.admin.annotation.BotAnnotationEvent
import ai.tock.bot.admin.annotation.BotAnnotationEventDTO
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.nlp.NlpCallStats
import ai.tock.bot.engine.nlp.NlpStats
import org.litote.kmongo.Id

/**
 *
 */
interface DialogReportDAO {

    fun search(query: DialogReportQuery): DialogReportQueryResult
    fun intents(namespace: String,nlpModel : String): Set<String>

    fun findBotDialogStats(query: DialogReportQuery): RatingReportQueryResult?

    fun getDialog(id: Id<Dialog>): DialogReport?

    fun getNlpCallStats(actionId: Id<Action>, namespace: String): NlpCallStats?

    // ANNOTATION FUNCTIONS
    fun insertAnnotation(dialogId: String, actionId: String, annotation: BotAnnotation)
    fun getNlpStats(dialogIds: List<Id<Dialog>>, namespace: String): List<NlpStats>

    fun updateAnnotation(dialogId: String, actionId: String, annotation: BotAnnotation)
    fun addAnnotationEvent(dialogId: String, actionId: String, event: BotAnnotationEvent)
    fun getAnnotationEvent(dialogId: String, actionId: String, eventId: String): BotAnnotationEvent?
    fun updateAnnotationEvent(dialogId: String, actionId: String, eventId: String, updatedEvent: BotAnnotationEvent)
    fun deleteAnnotationEvent(dialogId: String, actionId: String, eventId: String)
    fun annotationExists(dialogId: String, actionId: String): Boolean
    fun findAnnotation(dialogId: String, actionId: String): BotAnnotation?
    fun findAnnotationById(dialogId: String, actionId: String, annotationId: String): BotAnnotation?
}
