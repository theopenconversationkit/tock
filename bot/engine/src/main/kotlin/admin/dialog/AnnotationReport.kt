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

package ai.tock.bot.admin.dialog

import ai.tock.bot.admin.annotation.BotAnnotation
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import org.litote.kmongo.Id
import java.time.Instant

/**
 * A flat view of a single annotation, extracted from the dialog action that carries it.
 *
 * Unlike [DialogReport] (one entry per dialog), there is one [AnnotationReport] per annotation,
 * so a cross-dialog list of annotations can be paginated and sorted on its own.
 */
data class AnnotationReport(
    val dialogId: Id<Dialog>,
    val actionId: Id<Action>,
    val applicationId: String,
    val actionDate: Instant,
    /**
     * The annotation itself. Its [BotAnnotation.events] are intentionally emptied: this flat view
     * is used by read-only listings (e.g. the dashboard widget) that never render the event thread.
     */
    val annotation: BotAnnotation,
    /**
     * Best-effort text identifying the annotated exchange: the user question that triggered the
     * annotated bot answer (from `ragDebug.user_question`), falling back to the answer text.
     */
    val question: String? = null,
)

/**
 * Paginated result of an annotation search, mirroring [DialogReportQueryResult].
 */
data class AnnotationReportQueryResult(
    val total: Long,
    val start: Long = 0,
    val end: Long = 0,
    val annotations: List<AnnotationReport> = emptyList(),
)
