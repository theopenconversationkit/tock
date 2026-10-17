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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.knowledgebase.KnowledgeBaseEntryStatus
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJob
import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobType
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseDuplicatePolicy
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseExportEnvelope
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseExportedEntry
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseImportCandidate
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseImportPreview
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseImportRequest
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseImportResult

class KnowledgeBaseImportService(
    private val service: KnowledgeBaseService = KnowledgeBaseService.default,
) {
    fun preview(
        namespace: String,
        botId: String,
        request: KnowledgeBaseImportPreview,
    ): List<KnowledgeBaseImportCandidate> {
        require(request.rows.size <= 1000) { "Import is limited to 1000 entries" }
        val entries = service.dao.entries(namespace, botId).filterNot { it.deleted }
        val seen = mutableSetOf<String>()
        return request.rows.map { row ->
            val payload = row.payload.copy(status = KnowledgeBaseEntryStatus.DRAFT)
            val validated = runCatching { KnowledgeBaseService.validated(payload) }
            val key = KnowledgeBaseService.normalizeTitle(payload.title)
            val existing =
                entries.firstOrNull { KnowledgeBaseService.normalizeTitle(it.title) == key }
                    ?: row.sourceId?.let { source -> entries.firstOrNull { it.sourceId == source || it._id == source } }
            val repeated = !seen.add(key)
            val rejected = row.rejected || validated.isFailure
            KnowledgeBaseImportCandidate(
                validated.getOrDefault(payload),
                row.sourceId,
                if (rejected) {
                    "REJECTED"
                } else if (existing != null || repeated) {
                    "DUPLICATE"
                } else {
                    "NEW"
                },
                existing?._id,
                row.issues.filter { it.startsWith("knowledge-base.import.") } +
                    if (validated.isFailure) listOf(validated.exceptionOrNull()?.message ?: "knowledge-base.import.issue_invalid") else emptyList(),
                row.faqEnabled,
            )
        }
    }

    fun apply(
        namespace: String,
        botId: String,
        request: KnowledgeBaseImportRequest,
        author: String,
    ): KnowledgeBaseImportResult =
        service.mutate(namespace, botId) {
            require(request.candidates.size <= 1000) { "Import is limited to 1000 entries" }
            var created = 0
            var updated = 0
            var skipped = 0
            var failed = 0
            val ids = mutableListOf<String>()
            val working =
                service.dao
                    .entries(namespace, botId)
                    .filterNot { it.deleted }
                    .toMutableList()
            val job = KnowledgeBaseJob(namespace, botId, KnowledgeBaseJobType.UNPUBLISH)
            request.candidates.forEach { candidate ->
                if (candidate.state == "REJECTED") {
                    skipped++
                    return@forEach
                }
                val payload = runCatching { KnowledgeBaseService.validated(candidate.payload.copy(status = KnowledgeBaseEntryStatus.DRAFT)) }.getOrNull()
                if (payload == null) {
                    failed++
                    return@forEach
                }
                val existing =
                    working.firstOrNull { KnowledgeBaseService.normalizeTitle(it.title) == KnowledgeBaseService.normalizeTitle(payload.title) }
                        ?: candidate.sourceId?.let { source -> working.firstOrNull { it.sourceId == source || it._id == source } }
                // The preview may be stale or forged: never trust its state or existingEntryId.
                if (existing != null && request.duplicatePolicy == KnowledgeBaseDuplicatePolicy.SKIP) {
                    skipped++
                    return@forEach
                }
                val previous = existing?.takeIf { request.duplicatePolicy == KnowledgeBaseDuplicatePolicy.UPDATE }
                val saved = service.write(namespace, botId, payload, author, job._id, previous, candidate.sourceId?.take(200))
                if (previous == null) {
                    created++
                } else {
                    updated++
                    working.removeAll { it._id == previous._id }
                }
                working.add(saved)
                ids.add(saved._id)
            }
            val resultJob = if (ids.isNotEmpty()) job.copy(entryIds = ids.distinct()).also { service.dao.saveJob(it) } else null
            KnowledgeBaseImportResult(created, updated, skipped, failed, ids.distinct(), resultJob?.let { service.jobDTO(it) })
        }

    fun export(
        namespace: String,
        botId: String,
    ): KnowledgeBaseExportEnvelope =
        KnowledgeBaseExportEnvelope(
            namespace,
            botId,
            service.dao
                .entries(namespace, botId)
                .filterNot { it.deleted }
                .map { KnowledgeBaseExportedEntry(it.title, it.searchHints, it.content, it.sourceUrl, it.tags, it.status, it._id) },
        )
}
