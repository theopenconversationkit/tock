/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.annotation.BotAnnotationDTO
import ai.tock.bot.admin.annotation.BotAnnotationEventDTO
import ai.tock.bot.admin.model.DialogsSearchQuery
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.WebVerticle.Companion.unauthorized
import io.vertx.ext.web.RoutingContext

/**
 * Verticle handling dialog and annotation related endpoints.
 */
class DialogVerticle {

    companion object {
        private const val PATH_ANNOTATION = "/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation"
        private const val PATH_ANNOTATION_EVENTS = "$PATH_ANNOTATION/:annotationId/events"
        private const val PATH_ANNOTATION_EVENT = "/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation/events/:eventId"
        private const val PATH_ANNOTATION_EVENT_DELETE = "/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation/:annotationId/events/:eventId"
    }

    private val front = FrontClient

    fun configure(webVerticle: WebVerticle) {
        with(webVerticle) {

            val currentContextApp: (RoutingContext) -> ApplicationDefinition? = { context ->
                val botId = context.pathParam("botId")
                val namespace = getNamespace(context)
                front.getApplicationByNamespaceAndName(
                    namespace, botId
                ) ?: throw NotFoundException(404, "Could not find $botId in $namespace")
            }

            // --------------------------------- Annotation Routes ----------------------------------

            // CREATE ANNO
            blockingJsonPost(PATH_ANNOTATION, setOf(TockUserRole.botUser)) { context, annotationDTO: BotAnnotationDTO ->
                val dialogId = context.path("dialogId")
                val actionId = context.path("actionId")
                val user = context.userLogin

                try {
                    logger.info { "Creating Annotation..." }
                    BotAdminService.createAnnotation(dialogId, actionId, annotationDTO, user)
                } catch (e: IllegalStateException) {
                    context.fail(400, e)
                }
            }

            // ADD EVENT
            blockingJsonPost(
                PATH_ANNOTATION_EVENTS,
                setOf(TockUserRole.botUser)
            ) { context, eventDTO: BotAnnotationEventDTO ->
                val dialogId = context.path("dialogId")
                val actionId = context.path("actionId")
                val annotationId = context.path("annotationId")
                val user = context.userLogin

                logger.info { "Adding an event..." }
                BotAdminService.addEventToAnnotation(dialogId, actionId, eventDTO, user)
            }

            // MODIFY COMMENT
            blockingJsonPut(
                PATH_ANNOTATION_EVENT,
                setOf(TockUserRole.botUser)
            ) { context, eventDTO: BotAnnotationEventDTO ->
                val dialogId = context.path("dialogId")
                val actionId = context.path("actionId")
                val eventId = context.path("eventId")
                val user = context.userLogin

                logger.info { "Modifying a comment..." }
                BotAdminService.updateAnnotationEvent(dialogId, actionId, eventId, eventDTO, user)
            }

            // DELETE COMMENT
            blockingDelete(PATH_ANNOTATION_EVENT_DELETE, setOf(TockUserRole.botUser)) { context ->
                val dialogId = context.path("dialogId")
                val actionId = context.path("actionId")
                val annotationId = context.path("annotationId")
                val eventId = context.path("eventId")
                val user = context.userLogin

                logger.info { "Deleting a comment..." }
                BotAdminService.deleteAnnotationEvent(dialogId, actionId, annotationId, eventId, user)
            }


        }
        }

    /**
     * Get the namespace from the context
     * @param context : the vertx routing context
     */
    private fun getNamespace(context: RoutingContext) = (context.user() as TockUser).namespace

}
