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

import ai.tock.bot.admin.model.evaluation.ActionRefsQuery
import ai.tock.bot.admin.model.evaluation.ChangeStatusRequest
import ai.tock.bot.admin.model.evaluation.CreateEvaluationSampleRequest
import ai.tock.bot.admin.model.evaluation.EvaluateRequest
import ai.tock.bot.admin.service.EvaluationService
import ai.tock.shared.security.TockUserRole.botUser
import ai.tock.shared.vertx.WebVerticle
import io.vertx.ext.web.RoutingContext

/**
 * Verticle handling evaluation sample endpoints.
 */
class EvaluationVerticle {
    companion object {
        private const val PATH_SAMPLES = "/bots/:botId/evaluation-samples"
        private const val PATH_SAMPLE = "/bots/:botId/evaluation-samples/:sampleId"
        private const val PATH_ACTION_REFS = "/bots/:botId/evaluation-samples/:sampleId/action-refs"
        private const val PATH_EVALUATION = "/bots/:botId/evaluation-samples/:sampleId/evaluations/:evaluationId"
        private const val PATH_CHANGE_STATUS = "/bots/:botId/evaluation-samples/:sampleId/change-status"
    }

    fun configure(verticle: WebVerticle) {
        with(verticle) {
            // GET /bots/:botId/evaluation-samples - List all evaluation samples for a bot
            blockingJsonGet(PATH_SAMPLES, botUser) { context ->
                val botId = context.pathParam("botId")
                val namespace = context.organization
                val statusParam = context.queryParam("status").firstOrNull()
                val status =
                    statusParam?.let {
                        ai.tock.bot.admin.evaluation.EvaluationSampleStatus.valueOf(it)
                    }
                EvaluationService.findSamplesByBotId(namespace, botId, status)
            }

            // POST /bots/:botId/evaluation-samples - Create a new evaluation sample
            blockingJsonPost(PATH_SAMPLES, botUser) { context, request: CreateEvaluationSampleRequest ->
                val botId = context.pathParam("botId")
                val namespace = context.organization
                val createdBy = context.userLogin
                EvaluationService.createEvaluationSample(namespace, botId, request, createdBy)
            }

            // GET /bots/:botId/evaluation-samples/:sampleId - Get a specific evaluation sample
            blockingJsonGet(PATH_SAMPLE, botUser) { context ->
                val sampleId = context.pathParam("sampleId")
                EvaluationService.findSampleById(sampleId)
            }

            // DELETE /bots/:botId/evaluation-samples/:sampleId - Delete an evaluation sample
            blockingDelete(PATH_SAMPLE, botUser) { context ->
                val sampleId = context.pathParam("sampleId")
                EvaluationService.deleteSample(sampleId)
            }

            // POST /bots/:botId/evaluation-samples/:sampleId/action-refs - Get paginated action refs (POST with body)
            blockingJsonPost(PATH_ACTION_REFS, botUser) { context, query: ActionRefsQuery ->
                val sampleId = context.pathParam("sampleId")
                EvaluationService.getActionRefs(sampleId, query.start, query.size, query.status)
            }

            // PUT /bots/:botId/evaluation-samples/:sampleId/evaluations/:evaluationId - Evaluate an action
            blockingJsonPut(
                PATH_EVALUATION,
                setOf(botUser),
            ) { context: RoutingContext, request: EvaluateRequest ->
                val sampleId = context.pathParam("sampleId")
                val evaluationId = context.pathParam("evaluationId")
                val evaluatorId = context.userLogin
                EvaluationService.evaluate(sampleId, evaluationId, request.status, request.reason, evaluatorId)
            }

            // POST /bots/:botId/evaluation-samples/:sampleId/change-status - Change sample status
            blockingJsonPost(PATH_CHANGE_STATUS, botUser) { context, request: ChangeStatusRequest ->
                val sampleId = context.pathParam("sampleId")
                val changedBy = context.userLogin
                EvaluationService.changeStatus(sampleId, request.targetStatus, changedBy, request.comment)
            }
        }
    }
}
