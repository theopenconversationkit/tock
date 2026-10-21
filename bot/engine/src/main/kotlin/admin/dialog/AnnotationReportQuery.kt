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

import ai.tock.bot.admin.annotation.BotAnnotationReasonType
import ai.tock.bot.admin.annotation.BotAnnotationState

/**
 * Query for a flat, cross-dialog annotation search (see [AnnotationReport]).
 *
 * Deliberately slim: it carries only what the dedicated annotation aggregation needs, rather than
 * reusing the much larger [DialogReportQuery].
 */
data class AnnotationReportQuery(
    val namespace: String,
    val nlpModel: String,
    val annotationStates: Set<BotAnnotationState>,
    val annotationReasons: Set<BotAnnotationReasonType> = emptySet(),
    val start: Long = 0,
    val size: Int = 5,
)
