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
import ai.tock.bot.admin.BotAdminService.dialogReportDAO
import ai.tock.bot.admin.annotation.BotAnnotationDTO
import ai.tock.bot.admin.annotation.BotAnnotationEventDTO
import ai.tock.bot.admin.annotation.BotAnnotationEventType
import ai.tock.bot.admin.annotation.BotAnnotationUpdateDTO
import ai.tock.bot.admin.model.DialogsSearchQuery
import ai.tock.bot.engine.message.Sentence
import ai.tock.nlp.admin.CsvCodec
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.WebVerticle.Companion.unauthorized
import io.vertx.ext.web.RoutingContext
import org.litote.kmongo.Id
import org.litote.kmongo.toId

/**
 * Verticle handling dialog and annotation related endpoints.
 */
class DialogVerticle {

    companion object {
        // DIALOGS ENDPOINTS
        private const val PATH_RATINGS_EXPORT = "/dialogs/ratings/export"
        private const val PATH_INTENTS_EXPORT = "/dialogs/ratings/intents/export"
        private const val PATH_DIALOG = "/dialog/:applicationId/:dialogId"
        private const val PATH_DIALOG_SATISFACTION = "/dialog/:applicationId/:dialogId/satisfaction"
        private const val PATH_DIALOGS_SEARCH = "/dialogs/search"
        private const val PATH_DIALOGS_INTENTS = "/dialogs/intents/:applicationId"

        // ANNOTATION ENDPOINTS
        private const val PATH_ANNOTATION = "/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation"
        private const val PATH_ANNOTATION_EVENTS = "$PATH_ANNOTATION/:annotationId/events"
        private const val PATH_ANNOTATION_EVENT = "/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation/events/:eventId"
        private const val PATH_ANNOTATION_UPDATE = "/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation/:annotationId"
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

            // --------------------------------- Dialog Routes --------------------------------------

            blockingJsonPost(PATH_RATINGS_EXPORT, setOf(TockUserRole.botUser)) { context, query: DialogsSearchQuery ->
                if (context.organization == query.namespace) {
                    val sb = StringBuilder()
                    val printer = CsvCodec.newPrinter(sb)
                    printer.printRecord(listOf("Timestamp", "Dialog ID", "Note", "Commentaire"))
                    BotAdminService.search(query)
                        .dialogs
                        .forEach { label ->
                            printer.printRecord(
                                listOf(
                                    label.actions.first().date,
                                    label.id,
                                    label.rating,
                                    label.review,
                                )
                            )
                        }
                    sb.toString()
                } else {
                    unauthorized()
                }
            }

            blockingJsonPost(PATH_INTENTS_EXPORT, setOf(TockUserRole.botUser)) { context, query: DialogsSearchQuery ->
                if (context.organization == query.namespace) {
                    val sb = StringBuilder()
                    val printer = CsvCodec.newPrinter(sb)
                    printer.printRecord(
                        listOf(
                            "Timestamp",
                            "Intent",
                            "Dialog ID",
                            "Player Type",
                            "Application ID",
                            "Message"
                        )
                    )
                    BotAdminService.search(query)
                        .dialogs
                        .forEach { dialog ->
                            dialog.actions.forEach {
                                printer.printRecord(
                                    listOf(
                                        it.date,
                                        it.intent,
                                        dialog.id,
                                        it.playerId.type,
                                        it.applicationId,
                                        if (it.message.isSimpleMessage()) it.message.toPrettyString().replace(
                                            "\n",
                                            " "
                                        ) else (it.message as Sentence).messages.joinToString { it.texts.values.joinToString() }
                                            .replace("\n", " ")
                                    )
                                )
                            }
                        }
                    sb.toString()

                } else {
                    unauthorized()
                }
            }

            blockingJsonGet(PATH_DIALOG, setOf(TockUserRole.botUser)) { context ->
                val app = FrontClient.getApplicationById(context.pathId("applicationId"))
                if (context.organization == app?.namespace) {
                    dialogReportDAO.getDialog(context.path("dialogId").toId())
                } else {
                    unauthorized()
                }
            }

            blockingJsonPost(PATH_DIALOG_SATISFACTION, setOf(TockUserRole.botUser)) { context, query: Set<String> ->
                val app = FrontClient.getApplicationById(context.pathId("applicationId"))
                if (context.organization == app?.namespace) {
                    BotAdminService.getDialogObfuscatedById(context.pathId("dialogId"), query)
                } else {
                    unauthorized()
                }
            }

            blockingJsonPost(PATH_DIALOGS_SEARCH, setOf(TockUserRole.botUser)) { context, query: DialogsSearchQuery ->
                if (context.organization == query.namespace) {
                    BotAdminService.search(query)
                } else {
                    unauthorized()
                }
            }

            blockingJsonGet(PATH_DIALOGS_INTENTS, setOf(TockUserRole.botUser)) { context ->
                val app = FrontClient.getApplicationById(context.path("applicationId").toId())
                app?.let { BotAdminService.getIntentsInDialogs(app.namespace, app.name) }
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

            // MODIFY ANNOTATION
            blockingJsonPut(
                PATH_ANNOTATION_UPDATE,
                setOf(TockUserRole.botUser)
            ) { context, updatedAnnotationDTO: BotAnnotationUpdateDTO ->
                val botId = context.path("botId")
                val dialogId = context.path("dialogId")
                val actionId = context.path("actionId")
                val annotationId = context.path("annotationId")
                val user = context.userLogin

                try {
                    logger.info { "Updating annotation for bot $botId, dialog $dialogId, action $actionId..." }
                    val updatedAnnotation = BotAdminService.updateAnnotation(
                        dialogId = dialogId,
                        actionId = actionId,
                        annotationId = annotationId,
                        updatedAnnotationDTO = updatedAnnotationDTO,
                        user = user
                    )
                    updatedAnnotation
                } catch (e: IllegalArgumentException) {
                    context.fail(400, e)
                } catch (e: IllegalStateException) {
                    context.fail(404, e)
                }
            }

            // ADD COMMENT
            blockingJsonPost(
                PATH_ANNOTATION_EVENTS,
                setOf(TockUserRole.botUser)
            ) { context, eventDTO: BotAnnotationEventDTO ->
                val dialogId = context.path("dialogId")
                val actionId = context.path("actionId")
                val annotationId = context.path("annotationId")
                val user = context.userLogin

                if (eventDTO.type != BotAnnotationEventType.COMMENT) {
                    throw IllegalArgumentException("Only COMMENT events are allowed")
                }

                logger.info { "Adding a COMMENT event to annotation $annotationId..." }
                BotAdminService.addCommentToAnnotation(dialogId, actionId, eventDTO, user)
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
