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

import ai.tock.bot.admin.indicators.IndicatorError
import ai.tock.bot.admin.indicators.PredefinedIndicators
import ai.tock.bot.admin.indicators.metric.MetricFilter
import ai.tock.bot.admin.model.Valid
import ai.tock.bot.admin.model.ValidationError
import ai.tock.bot.admin.model.indicator.SaveIndicatorRequest
import ai.tock.bot.admin.model.indicator.UpdateIndicatorRequest
import ai.tock.bot.admin.model.indicator.metric.Requests
import ai.tock.bot.admin.service.IndicatorService
import ai.tock.bot.admin.service.MetricService
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.exception.rest.UnauthorizedException
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

/**
 * IndicatorVerticle contains all the routes and actions associated with the indicators analytics
 */
class IndicatorVerticle {

    companion object {
        const val PATH_PARAM_APPLICATION_NAME = "applicationName"
        const val PATH_PARAM_NAME = "name"
        private const val INDICATORS = "indicators"
        private const val METRICS = "metrics"
        const val ALL_INDICATORS = "/$INDICATORS"
        private const val BOT = "bot"
        const val INDICATORS_BY_APPLICATION_NAME_PATH = "/$BOT/:$PATH_PARAM_APPLICATION_NAME/$INDICATORS"
        const val BY_APPLICATION_NAME_AND_BY_NAME_PATH =
            "/$BOT/:$PATH_PARAM_APPLICATION_NAME/$INDICATORS/:$PATH_PARAM_NAME"
        const val METRICS_BY_APPLICATION_NAME_PATH = "/$BOT/:$PATH_PARAM_APPLICATION_NAME/$METRICS"
    }

    private val front = FrontClient

    fun configure(webVerticle: WebVerticle) {
        val authorizedRoles = setOf(TockUserRole.botUser, TockUserRole.admin, TockUserRole.technicalAdmin)

        with(webVerticle) {

            /**
             * lamdba calling database to retrieve application definition from request context
             * @return [ApplicationDefinition]
             */
            val currentContextApp: (RoutingContext) -> ApplicationDefinition? =
                { context ->
                    val appName = context.pathParam(PATH_PARAM_APPLICATION_NAME)
                    val namespace = getNamespace(context)
                    front.getApplicationByNamespaceAndName(
                        namespace,
                        appName
                    ) ?: throw NotFoundException(404, "Could not find $appName in $namespace")
                }

            blockingJsonPost(
                INDICATORS_BY_APPLICATION_NAME_PATH,
                authorizedRoles
            ) { context: RoutingContext, request: SaveIndicatorRequest ->
                checkNamespaceAndExecute(context, currentContextApp) {
                    tryExecute(context) {
                        logger.info { "saving new indicator ${request.name}" }
                        IndicatorService.save(it.namespace, it.name, Valid(request))
                    }
                }
                return@blockingJsonPost request
            }

            blockingJsonPut(
                BY_APPLICATION_NAME_AND_BY_NAME_PATH,
                authorizedRoles
            ) { context: RoutingContext, request: UpdateIndicatorRequest ->
                checkNamespaceAndExecute(context, currentContextApp) {
                    val name = context.path(PATH_PARAM_NAME)
                    tryExecute(context) {
                        logger.info { "updating indicator $name" }
                        IndicatorService.update(it.name, it.namespace, name, Valid(request))
                    }
                }
                return@blockingJsonPut request
            }

            blockingJsonGet(BY_APPLICATION_NAME_AND_BY_NAME_PATH, authorizedRoles) { context ->
                checkNamespaceAndExecute(context, currentContextApp) {
                    val name = context.path(PATH_PARAM_NAME)
                    tryExecute(context) {
                        logger.info { "deleting indicator $name" }
                        IndicatorService.findByNameAndBotId(name, it.namespace, it.name)
                    }
                }
            }

            blockingJsonGet(INDICATORS_BY_APPLICATION_NAME_PATH, authorizedRoles) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) {
                    tryExecute(context) {
                        logger.info { "retrieve indicators from ${it.name}" }
                        IndicatorService.findAllByBotId(it.namespace, it.name) + PredefinedIndicators.indicators
                    }
                }
            }

            blockingJsonGet(ALL_INDICATORS, authorizedRoles) { _: RoutingContext ->
                logger.info { "retrieve all indicators" }
                IndicatorService.findAll()
            }

            blockingJsonDelete(BY_APPLICATION_NAME_AND_BY_NAME_PATH, authorizedRoles) { context: RoutingContext ->
                val indicatorName = context.path(PATH_PARAM_NAME)
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    tryExecute(context) {
                        IndicatorService.deleteByNameAndApplicationName(indicatorName, app.namespace, app.name)
                    }
                } ?: false
            }

            blockingJsonPost(METRICS_BY_APPLICATION_NAME_PATH, authorizedRoles) {
                    context: RoutingContext, request: Requests ->
                checkNamespaceAndExecute(context, currentContextApp) {
                    MetricService.filterAndGroupBy(createFilterMetric(namespace = it.namespace, it.name, request.filter), request.groupBy)
                }
            }
        }
    }

    /**
     * Get the namespace from the context
     * @param context : the vertx routing context
     */
    private fun getNamespace(context: RoutingContext) = (context.user() as TockUser).namespace

    /**
     * Merge namespace and botId on requested [MetricFilter]
     * @param namespace the namespace
     * @param botId the bot id
     * @param filter a given [MetricFilter]
     */
    private fun createFilterMetric(namespace: String, botId: String, filter: MetricFilter?)
            = filter?.copy(namespace = namespace, botId = botId) ?: MetricFilter(namespace, botId)
}

/**
 * Check the app requested is found to execute the request
 * @param context [RoutingContext] the request context
 * @param applicationDefinition the application definition retrieved from [context][RoutingContext]
 * @param block the code block invoke after check is OK
 * @throws [UnauthorizedException] if context check is KO
 *
 */
fun <T> WebVerticle.checkNamespaceAndExecute(
    context: RoutingContext,
    applicationDefinition: (RoutingContext) -> ApplicationDefinition?,
    block: (ApplicationDefinition) -> T
): T? {
    val appFound = applicationDefinition.invoke(context)
    return if (context.organization == appFound?.namespace) {
        block.invoke(appFound)
    } else {
        WebVerticle.unauthorized()
    }
}

data class ErrorMessage(val message: String? = "Unexpected error occurred")

/**
 * try to execute [block] code otherwise throw an exception and set the status code
 * @param context [RoutingContext] request context to be set
 * @param block code block invoked
 */
private fun <T> tryExecute(context: RoutingContext, block: () -> T): T? {
    return try {
        // in case of success the status code is 201 for POST creation method in this Verticle
        if (context.request().method() == HttpMethod.POST) {
            context.response().statusCode = 201
        }
        block.invoke()
    } catch (e: Exception) {

        val statusCode = when (e) {
            is ValidationError -> 400
            is IndicatorError.IndicatorDeletionFailed -> 409
            is IndicatorError.IndicatorAlreadyExists -> 409
            is IndicatorError.IndicatorNotFound -> 404
            else -> 500
        }

        KotlinLogging.logger{}.error { "Error ${e.message}" }

        context.response()
            .setStatusCode(statusCode)
            .end(ObjectMapper().writeValueAsString(ErrorMessage(e.message)))

        null
    }
}
