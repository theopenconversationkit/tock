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

package ai.tock.bot.admin.model

import ai.tock.bot.admin.annotation.BotAnnotationReasonType
import ai.tock.bot.admin.annotation.BotAnnotationState
import ai.tock.bot.admin.dialog.AnnotationReportQuery
import ai.tock.nlp.admin.model.PaginatedQuery

/**
 * Request for the open-annotations search (`POST /bots/:botId/annotations/search`).
 *
 * Inherits [start] / [size] from [PaginatedQuery]. The namespace and bot are NOT taken from the
 * request body: they are resolved server-side from the session and the path `botId`, so a caller
 * cannot widen the scope by crafting the payload.
 */
data class AnnotationsSearchQuery(
    val annotationStates: Set<BotAnnotationState> =
        setOf(BotAnnotationState.ANOMALY, BotAnnotationState.REVIEW_NEEDED),
    val annotationReasons: Set<BotAnnotationReasonType> = emptySet(),
) : PaginatedQuery() {
    fun toAnnotationReportQuery(
        namespace: String,
        botId: String,
    ): AnnotationReportQuery =
        AnnotationReportQuery(
            namespace = namespace,
            nlpModel = botId,
            annotationStates = annotationStates,
            annotationReasons = annotationReasons,
            start = start,
            // Cap the page size so a crafted request cannot ask for an unbounded $limit.
            size = size.coerceIn(1, 100),
        )
}
