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
import ai.tock.bot.admin.annotation.BotAnnotationReasonType
import ai.tock.bot.admin.annotation.BotAnnotationState
import ai.tock.bot.admin.dialog.AnnotationReport
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import com.fasterxml.jackson.annotation.JsonProperty
import com.mongodb.client.model.Aggregates.count
import com.mongodb.client.model.Aggregates.limit
import com.mongodb.client.model.Aggregates.match
import com.mongodb.client.model.Aggregates.project
import com.mongodb.client.model.Aggregates.skip
import com.mongodb.client.model.Aggregates.sort
import com.mongodb.client.model.Aggregates.unwind
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.`in`
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.Id
import java.time.Instant

/**
 * Dedicated aggregation turning the per-action annotations embedded in the `dialog` collection
 * into a flat, paginated, cross-dialog list (one row per annotation). See [AnnotationReport].
 *
 * Pipeline: scope + array-level state/reason match (index-friendly prune) → unwind stories →
 * unwind actions → re-match the unwound action → project the flat row → sort by last update desc.
 * The page and the total count share the same base, split into two pipelines (no `$facet`).
 */
internal object AnnotationReportAggregation {
    /**
     * Flat row deserialized from the projected document. `annotation` keeps its events here (they
     * are present in the stored sub-document); they are emptied when mapping to [AnnotationReport].
     */
    internal data class AnnotationReportCol(
        @field:JsonProperty("_id") val dialogId: Id<Dialog>,
        val actionId: Id<Action>,
        val applicationId: String,
        val actionDate: Instant,
        val annotation: BotAnnotation,
        val question: String? = null,
    ) {
        fun toReport(): AnnotationReport =
            AnnotationReport(
                dialogId = dialogId,
                actionId = actionId,
                applicationId = applicationId,
                actionDate = actionDate,
                // Events are not exposed by this read-only listing.
                annotation = annotation.copy(events = mutableListOf()),
                question = question,
            )
    }

    private const val STATE_PATH = "stories.actions.annotation.state"
    private const val REASON_PATH = "stories.actions.annotation.reason"

    /** Enums are stored by name — match on their names. */
    private fun annotationFilters(
        states: Set<BotAnnotationState>,
        reasons: Set<BotAnnotationReasonType>,
    ): List<Bson> =
        buildList {
            add(`in`(STATE_PATH, states.map { it.name }))
            if (reasons.isNotEmpty()) add(`in`(REASON_PATH, reasons.map { it.name }))
        }

    private fun basePipeline(
        namespace: String,
        applicationIds: Set<String>,
        states: Set<BotAnnotationState>,
        reasons: Set<BotAnnotationReasonType>,
    ): List<Bson> {
        val annotationFilters = annotationFilters(states, reasons)
        return listOf(
            // Array-level match first so Mongo can use indexes and prune whole dialogs early.
            match(and(listOf(eq("namespace", namespace), `in`("applicationIds", applicationIds)) + annotationFilters)),
            unwind("\$stories"),
            unwind("\$stories.actions"),
            // Re-match the unwound action: the array-level match kept dialogs, not single actions.
            match(and(annotationFilters)),
        )
    }

    /** Page pipeline: base + sort (most recently updated first) + skip/limit + flat projection. */
    fun pagePipeline(
        namespace: String,
        applicationIds: Set<String>,
        states: Set<BotAnnotationState>,
        reasons: Set<BotAnnotationReasonType>,
        start: Long,
        size: Int,
    ): List<Bson> =
        basePipeline(namespace, applicationIds, states, reasons) +
            listOf(
                // Tie-breakers after the update date keep pagination deterministic when two
                // annotations share the same lastUpdateDate.
                sort(
                    Document("stories.actions.annotation.lastUpdateDate", -1)
                        .append("_id", -1)
                        .append("stories.actions.id", -1),
                ),
                skip(start.toInt()),
                limit(size),
                project(
                    Document("actionId", "\$stories.actions.id")
                        .append("applicationId", "\$stories.actions.applicationId")
                        .append("actionDate", "\$stories.actions.date")
                        .append("annotation", "\$stories.actions.annotation")
                        // Best effort: the RAG user question, else the answer text.
                        .append(
                            "question",
                            Document("\$ifNull", listOf("\$stories.actions.ragDebug.user_question", "\$stories.actions.text")),
                        ),
                ),
            )

    /** Count pipeline: same base, collapsed to a single `{ total }` document. */
    fun countPipeline(
        namespace: String,
        applicationIds: Set<String>,
        states: Set<BotAnnotationState>,
        reasons: Set<BotAnnotationReasonType>,
    ): List<Bson> = basePipeline(namespace, applicationIds, states, reasons) + count("total")
}
