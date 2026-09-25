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

package ai.tock.bot.admin.verticle

import ai.tock.bot.admin.knowledgebase.KnowledgeBaseJobType
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseBulkStatus
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseEntryPayload
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseImportPreview
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseImportRequest
import ai.tock.bot.admin.model.knowledgebase.KnowledgeBaseRetrievalRequest
import ai.tock.bot.admin.service.KnowledgeBaseImportService
import ai.tock.bot.admin.service.KnowledgeBaseService
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import io.vertx.ext.web.RoutingContext

class KnowledgeBaseVerticle {
    companion object {
        private const val ROOT = "/bots/:botId/knowledge-base"
    }

    private val service get() = KnowledgeBaseService.default
    private val imports get() = KnowledgeBaseImportService(service)

    fun configure(webVerticle: WebVerticle) {
        val roles = setOf(TockUserRole.botUser, TockUserRole.admin, TockUserRole.technicalAdmin)
        with(webVerticle) {
            val app: (RoutingContext) -> ApplicationDefinition? = { context ->
                FrontClient.getApplicationByNamespaceAndName(context.organization, context.pathParam("botId"))
                    ?: throw NotFoundException(404, "Bot not found")
            }
            blockingJsonGet("$ROOT/entries", roles) { c ->
                checkNamespaceAndExecute(c, app) { a ->
                    kb(c) {
                        service.search(a.namespace, a.name, c.queryParams().entries().associate { it.key to it.value })
                    }
                }
            }
            blockingJsonGet("$ROOT/entries/:entryId", roles) { c ->
                checkNamespaceAndExecute(c, app) { a ->
                    kb(c) {
                        service.get(a.namespace, a.name, c.pathParam("entryId"))
                    }
                }
            }
            blockingJsonPost("$ROOT/entries", roles) { c, p: KnowledgeBaseEntryPayload ->
                checkNamespaceAndExecute(c, app) { a ->
                    kb(c) {
                        service.save(a.namespace, a.name, p, c.userLogin ?: "unknown")
                    }
                }
            }
            blockingJsonPut("$ROOT/entries/:entryId", roles) { c, p: KnowledgeBaseEntryPayload ->
                checkNamespaceAndExecute(c, app) { a ->
                    kb(c) {
                        service.save(a.namespace, a.name, p, c.userLogin ?: "unknown", c.pathParam("entryId"))
                    }
                }
            }
            blockingJsonPost("$ROOT/entries/:entryId/delete", roles) { c, _: Map<String, Any?> ->
                checkNamespaceAndExecute(c, app) { a ->
                    kb(c) {
                        service.delete(a.namespace, a.name, c.pathParam("entryId"), c.userLogin ?: "unknown")
                    }
                }
            }
            blockingJsonGet("$ROOT/tags", roles) { c -> checkNamespaceAndExecute(c, app) { a -> kb(c) { service.tags(a.namespace, a.name) } } }
            blockingJsonGet("$ROOT/sync", roles) { c -> checkNamespaceAndExecute(c, app) { a -> kb(c) { service.sync(a.namespace, a.name) } } }
            mapOf("sync" to KnowledgeBaseJobType.REPAIR_INDEX, "verify" to KnowledgeBaseJobType.VERIFY_INDEX, "index" to KnowledgeBaseJobType.CREATE_INDEX).forEach { (path, type) ->
                blockingJsonPost("$ROOT/$path", roles) { c, _: Map<String, Any?> -> checkNamespaceAndExecute(c, app) { a -> kb(c) { service.enqueue(a.namespace, a.name, type) } } }
            }
            blockingJsonPost("$ROOT/bulk-status", roles) { c, p: KnowledgeBaseBulkStatus ->
                checkNamespaceAndExecute(c, app) { a ->
                    kb(c) {
                        service.bulk(
                            a.namespace,
                            a.name,
                            p,
                            c.userLogin ?: "unknown",
                        )
                    }
                }
            }
            // Static route must precede the job-id route.
            blockingJsonGet("$ROOT/jobs/active", roles) { c -> checkNamespaceAndExecute(c, app) { a -> kb(c) { service.activeJob(a.namespace, a.name) } } }
            blockingJsonGet("$ROOT/jobs/:jobId", roles) { c -> checkNamespaceAndExecute(c, app) { a -> kb(c) { service.job(a.namespace, a.name, c.pathParam("jobId")) } } }
            blockingJsonPost("$ROOT/retrieval-test", roles) { c, p: KnowledgeBaseRetrievalRequest -> checkNamespaceAndExecute(c, app) { a -> kb(c) { service.retrieval(a.namespace, a.name, p) } } }
            blockingJsonPost("$ROOT/import/preview", roles) { c, p: KnowledgeBaseImportPreview -> checkNamespaceAndExecute(c, app) { a -> kb(c) { imports.preview(a.namespace, a.name, p) } } }
            blockingJsonPost(
                "$ROOT/import",
                roles,
            ) { c, p: KnowledgeBaseImportRequest -> checkNamespaceAndExecute(c, app) { a -> kb(c) { imports.apply(a.namespace, a.name, p, c.userLogin ?: "unknown") } } }
            blockingJsonGet("$ROOT/export", roles) { c -> checkNamespaceAndExecute(c, app) { a -> kb(c) { imports.export(a.namespace, a.name) } } }
        }
    }
}

private fun <T> kb(
    context: RoutingContext,
    action: () -> T,
): T? =
    try {
        action()
    } catch (e: Exception) {
        val code =
            when (e) {
                is NotFoundException -> 404
                is IllegalArgumentException -> 400
                else -> 500
            }
        // Never serialize provider exceptions or connection strings to the Studio.
        val message = if (code == 500) "knowledge-base.job.projection_failed" else e.message.orEmpty()
        context.response().setStatusCode(code).end(mapper.writeValueAsString(mapOf("message" to message)))
        null
    }
