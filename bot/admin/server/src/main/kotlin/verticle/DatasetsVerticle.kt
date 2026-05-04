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

import ai.tock.bot.admin.model.Valid
import ai.tock.bot.admin.model.ValidationError
import ai.tock.bot.admin.model.dataset.DatasetCreateRequest
import ai.tock.bot.admin.model.dataset.DatasetRunCancelRequest
import ai.tock.bot.admin.model.dataset.DatasetRunCreateRequest
import ai.tock.bot.admin.model.dataset.DatasetUpdateRequest
import ai.tock.bot.admin.model.evaluation.CreateEvaluationSampleFromRunRequest
import ai.tock.bot.admin.service.DatasetError
import ai.tock.bot.admin.service.DatasetService
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.exception.rest.UnprocessableEntityException
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

class DatasetsVerticle {
    companion object {
        private const val PATH_PARAM_BOT_ID = "botId"
        private const val PATH_PARAM_DATASET_ID = "datasetId"
        private const val PATH_PARAM_RUN_ID = "runId"
        private const val PATH_DATASETS = "/bots/:$PATH_PARAM_BOT_ID/datasets"
        private const val PATH_DATASET = "$PATH_DATASETS/:$PATH_PARAM_DATASET_ID"
        private const val PATH_RUNS = "$PATH_DATASET/runs"
        private const val PATH_RUN = "$PATH_RUNS/:$PATH_PARAM_RUN_ID"
        private const val PATH_RUN_ACTIONS = "$PATH_RUN/actions"
        private const val PATH_RUN_CANCEL = "$PATH_RUN/cancel"
        private const val PATH_RUN_EVALUATION_SAMPLES = "$PATH_RUN/evaluation-samples"
    }

    private val front = FrontClient

    fun configure(webVerticle: WebVerticle) {
        val authorizedRoles =
            setOf(
                TockUserRole.botUser,
                TockUserRole.admin,
                TockUserRole.technicalAdmin,
            )

        with(webVerticle) {
            val currentContextApp: (RoutingContext) -> ApplicationDefinition? = { context ->
                val botId = context.pathParam(PATH_PARAM_BOT_ID)
                val namespace = context.organization
                front.getApplicationByNamespaceAndName(namespace, botId)
                    ?: throw NotFoundException(404, "Could not find $botId in namespace $namespace")
            }

            blockingJsonGet(PATH_DATASETS, authorizedRoles) { context ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    tryExecuteDataset(context) {
                        DatasetService.listDatasets(app.namespace, app.name)
                    }
                }
            }

            blockingJsonGet(PATH_DATASET, authorizedRoles) { context ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    tryExecuteDataset(context) {
                        DatasetService.getDataset(
                            namespace = app.namespace,
                            botId = app.name,
                            datasetId = context.pathParam(PATH_PARAM_DATASET_ID),
                        )
                    }
                }
            }

            blockingJsonPost(PATH_DATASETS, authorizedRoles) { context, request: DatasetCreateRequest ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    tryExecuteDataset(context, created = true) {
                        Valid(request)
                        DatasetService.createDataset(
                            namespace = app.namespace,
                            botId = app.name,
                            request = request,
                            userLogin = context.userLogin,
                        )
                    }
                }
            }

            blockingJsonPost(PATH_RUNS, authorizedRoles) { context, request: DatasetRunCreateRequest ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    tryExecuteDataset(context, created = true) {
                        Valid(request)
                        DatasetService.createRun(
                            namespace = app.namespace,
                            botId = app.name,
                            datasetId = context.pathParam(PATH_PARAM_DATASET_ID),
                            request = request,
                            userLogin = context.userLogin,
                        )
                    }
                }
            }

            blockingJsonPut(PATH_DATASET, authorizedRoles) { context, request: DatasetUpdateRequest ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    tryExecuteDataset(context) {
                        Valid(request)
                        DatasetService.updateDataset(
                            namespace = app.namespace,
                            botId = app.name,
                            datasetId = context.pathParam(PATH_PARAM_DATASET_ID),
                            request = request,
                            userLogin = context.userLogin,
                        )
                    }
                }
            }

            blockingJsonGet(PATH_RUN, authorizedRoles) { context ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    tryExecuteDataset(context) {
                        DatasetService.getRun(
                            namespace = app.namespace,
                            botId = app.name,
                            datasetId = context.pathParam(PATH_PARAM_DATASET_ID),
                            runId = context.pathParam(PATH_PARAM_RUN_ID),
                        )
                    }
                }
            }

            blockingJsonGet(PATH_RUN_ACTIONS, authorizedRoles) { context ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    tryExecuteDataset(context) {
                        DatasetService.getRunActions(
                            namespace = app.namespace,
                            botId = app.name,
                            datasetId = context.pathParam(PATH_PARAM_DATASET_ID),
                            runId = context.pathParam(PATH_PARAM_RUN_ID),
                        )
                    }
                }
            }

            blockingJsonPost(PATH_RUN_CANCEL, authorizedRoles) { context, _: DatasetRunCancelRequest ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    tryExecuteDataset(context) {
                        DatasetService.cancelRun(
                            namespace = app.namespace,
                            botId = app.name,
                            datasetId = context.pathParam(PATH_PARAM_DATASET_ID),
                            runId = context.pathParam(PATH_PARAM_RUN_ID),
                        )
                    }
                }
            }

            blockingJsonPost(
                PATH_RUN_EVALUATION_SAMPLES,
                authorizedRoles,
            ) { context, request: CreateEvaluationSampleFromRunRequest ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    tryExecuteDataset(context, created = true) {
                        Valid(request)
                        DatasetService.createEvaluationSampleFromRun(
                            namespace = app.namespace,
                            botId = app.name,
                            datasetId = context.pathParam(PATH_PARAM_DATASET_ID),
                            runId = context.pathParam(PATH_PARAM_RUN_ID),
                            request = request,
                            userLogin = context.userLogin,
                        )
                    }
                }
            }

            blockingDeleteEmptyResponse(PATH_RUN, authorizedRoles) { context ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    tryExecuteDataset(context) {
                        DatasetService.deleteRun(
                            namespace = app.namespace,
                            botId = app.name,
                            datasetId = context.pathParam(PATH_PARAM_DATASET_ID),
                            runId = context.pathParam(PATH_PARAM_RUN_ID),
                        )
                    }
                }
            }

            blockingDeleteEmptyResponse(PATH_DATASET, authorizedRoles) { context ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    tryExecuteDataset(context) {
                        DatasetService.deleteDataset(
                            namespace = app.namespace,
                            botId = app.name,
                            datasetId = context.pathParam(PATH_PARAM_DATASET_ID),
                        )
                    }
                }
            }
        }
    }
}

private fun <T> tryExecuteDataset(
    context: RoutingContext,
    created: Boolean = false,
    block: () -> T,
): T? =
    try {
        if (created && context.request().method() == HttpMethod.POST) {
            context.response().statusCode = 201
        }
        block()
    } catch (e: Exception) {
        val statusCode =
            when (e) {
                is ValidationError -> 400
                is IllegalArgumentException -> 400
                is DatasetError.InvalidRequest -> 400
                is DatasetError.ActiveRunConflict -> 409
                is DatasetError.DatasetNotFound -> 404
                is DatasetError.RunNotFound -> 404
                is DatasetError.RunStateConflict -> 409
                is DatasetError.RunNotFinished -> 409
                is NotFoundException -> 404
                is UnprocessableEntityException -> 422
                else -> 500
            }

        KotlinLogging.logger {}.error(e) { "Datasets API error: ${e.message}" }

        context.response()
            .setStatusCode(statusCode)
            .end(ObjectMapper().writeValueAsString(ErrorMessage(e.message)))

        null
    }
