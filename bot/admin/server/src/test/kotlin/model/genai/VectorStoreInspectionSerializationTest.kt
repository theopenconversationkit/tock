/*
 * Copyright (C) 2017/2026 SNCF Connect & Tech
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

package ai.tock.bot.admin.model.genai

import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionChannelRanks
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionChannelScores
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionFunnelStage
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionSearchFunnel
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionSearchResponse
import ai.tock.genai.orchestratorclient.responses.VectorStoreInspectionSearchResultChunk
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VectorStoreInspectionSerializationTest {
    @Test
    fun `orchestrator top k cut stage is deserialized`() {
        val response =
            mapper.copy().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).readValue(
                """
                {
                  "funnel": {
                    "vector": {"status": "applied", "count": 5},
                    "fts": {"status": "skipped", "count": null},
                    "rrf": {"status": "skipped", "count": null},
                    "top_k_cut": {"status": "applied", "count": 3},
                    "compression": {"status": "disabled", "count": null}
                  },
                  "compression_stage": "before_cut",
                  "results": [],
                  "duration": 0.1
                }
                """.trimIndent(),
                VectorStoreInspectionSearchResponse::class.java,
            )

        assertEquals("applied", response.funnel.topKCut.status)
        assertEquals(3, response.funnel.topKCut.count)
    }

    @Test
    fun `null channel data is explicit in the Studio response`() {
        val skipped = VectorStoreInspectionFunnelStage(status = "skipped", count = null)
        val response =
            VectorStoreInspectionSearchResponseDTO(
                funnel =
                    VectorStoreInspectionSearchFunnel(
                        vector = skipped,
                        fts = skipped,
                        rrf = skipped,
                        topKCut = VectorStoreInspectionFunnelStage(status = "applied", count = 1),
                        compression = VectorStoreInspectionFunnelStage(status = "disabled", count = null),
                    ),
                compressionStage = "beforeCut",
                results =
                    listOf(
                        VectorStoreInspectionSearchResultChunk(
                            chunkId = "document:1/1",
                            documentId = "document",
                            title = "Title",
                            chunk = "1/1",
                            content = "Content",
                            ranks = VectorStoreInspectionChannelRanks(),
                            scores = VectorStoreInspectionChannelScores(),
                            outcome = "not_retrieved",
                            pinned = true,
                        ),
                    ),
                duration = 0.1,
            )

        val json = mapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(response)
        val result = json["results"][0]

        assertTrue(json["funnel"]["vector"]["count"].isNull)
        assertTrue(result["ranks"]["vector"].isNull)
        assertTrue(result["ranks"]["fts"].isNull)
        assertTrue(result["ranks"]["rrf"].isNull)
        assertTrue(result["scores"]["vector"].isNull)
        assertTrue(result["scores"]["fts"].isNull)
        assertTrue(result["scores"]["rrf"].isNull)
        assertTrue(result["scores"]["compressor"].isNull)
    }
}
