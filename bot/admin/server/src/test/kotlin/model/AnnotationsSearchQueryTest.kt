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
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AnnotationsSearchQueryTest {
    @Test
    fun `toAnnotationReportQuery takes namespace and bot from arguments, never from the request body`() {
        // A crafted body cannot widen the scope: namespace/nlpModel come from the trusted arguments.
        val query: AnnotationsSearchQuery =
            mapper.readValue("""{"namespace":"evil","applicationName":"evil-bot","annotationStates":["ANOMALY"]}""")

        val reportQuery = query.toAnnotationReportQuery("trusted-namespace", "trusted-bot")

        assertEquals("trusted-namespace", reportQuery.namespace)
        assertEquals("trusted-bot", reportQuery.nlpModel)
    }

    @Test
    fun `toAnnotationReportQuery caps an over-large size to 100`() {
        val query: AnnotationsSearchQuery = mapper.readValue("""{"size":200}""")

        assertEquals(100, query.toAnnotationReportQuery("ns", "bot").size)
    }

    @Test
    fun `toAnnotationReportQuery raises a non-positive size to 1`() {
        val query: AnnotationsSearchQuery = mapper.readValue("""{"size":0}""")

        assertEquals(1, query.toAnnotationReportQuery("ns", "bot").size)
    }

    @Test
    fun `toAnnotationReportQuery passes states, reasons and paging through unchanged within bounds`() {
        val query: AnnotationsSearchQuery =
            mapper.readValue(
                """{"start":5,"size":20,"annotationStates":["REVIEW_NEEDED"],"annotationReasons":["HALLUCINATION"]}""",
            )

        val reportQuery = query.toAnnotationReportQuery("ns", "bot")

        assertEquals(5, reportQuery.start)
        assertEquals(20, reportQuery.size)
        assertEquals(setOf(BotAnnotationState.REVIEW_NEEDED), reportQuery.annotationStates)
        assertEquals(setOf(BotAnnotationReasonType.HALLUCINATION), reportQuery.annotationReasons)
    }

    @Test
    fun `AnnotationsSearchQuery defaults to the two open states and no reason filter`() {
        val query: AnnotationsSearchQuery = mapper.readValue("{}")

        assertEquals(setOf(BotAnnotationState.ANOMALY, BotAnnotationState.REVIEW_NEEDED), query.annotationStates)
        assertEquals(emptySet(), query.annotationReasons)
    }
}
