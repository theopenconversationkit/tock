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
        private const val PATH_ANNOTATIONS = "/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation"
        private const val PATH_ANNOTATION = "$PATH_ANNOTATIONS/:annotationId"
        private const val PATH_ANNOTATION_EVENTS = "$PATH_ANNOTATION/events"
        private const val PATH_ANNOTATION_EVENT = "$PATH_ANNOTATION_EVENTS/:eventId"
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
                    BotAdminService.searchWithCommentRights(query, context.userLogin)
                } else {
                    unauthorized()
                }
            }

            blockingJsonGet(PATH_DIALOGS_INTENTS, setOf(TockUserRole.botUser)) { context ->
                val app = FrontClient.getApplicationById(context.path("applicationId").toId())
                app?.let { BotAdminService.getIntentsInDialogs(app.namespace, app.name) }
            }

            // --------------------------------- Annotation Routes ----------------------------------

            // CREATE ANNOTATION
            blockingJsonPost(PATH_ANNOTATIONS, setOf(TockUserRole.botUser)) { context, annotationDTO: BotAnnotationDTO ->
                BotAdminService.createAnnotation(
                    context.path("dialogId"),
                    context.path("actionId"),
                    annotationDTO,
                    context.userLogin
                )
            }

            // MODIFY ANNOTATION
            blockingJsonPut(PATH_ANNOTATION, setOf(TockUserRole.botUser)) { context, annotationDTO: BotAnnotationDTO ->
                BotAdminService.updateAnnotation(
                    context.path("dialogId"),
                    context.path("actionId"),
                    context.path("annotationId"),
                    annotationDTO,
                    context.userLogin
                )
            }

            // ADD COMMENT
            blockingJsonPost(PATH_ANNOTATION_EVENTS, setOf(TockUserRole.botUser)) { context, eventDTO: BotAnnotationEventDTO ->
                BotAdminService.addCommentToAnnotation(
                    context.path("dialogId"),
                    context.path("actionId"),
                    eventDTO,
                    context.userLogin
                )
            }

            // MODIFY COMMENT
            blockingJsonPut(PATH_ANNOTATION_EVENT, setOf(TockUserRole.botUser)) { context, eventDTO: BotAnnotationEventDTO ->
                BotAdminService.updateAnnotationEvent(
                    context.path("dialogId"),
                    context.path("actionId"),
                    context.path("eventId"),
                    eventDTO,
                    context.userLogin
                )
            }

            // DELETE COMMENT
            blockingDelete(PATH_ANNOTATION_EVENT, setOf(TockUserRole.botUser)) { context ->
                BotAdminService.deleteAnnotationEvent(
                    context.path("dialogId"),
                    context.path("actionId"),
                    context.path("annotationId"),
                    context.path("eventId"),
                    context.userLogin
                )
            }
        }
    }

    /**
     * Get the namespace from the context
     * @param context : the vertx routing context
     */
    private fun getNamespace(context: RoutingContext) = (context.user() as TockUser).namespace

}
