/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

import ai.tock.bot.admin.model.FaqDefinitionRequest
import ai.tock.bot.admin.model.FaqSearchRequest
import ai.tock.bot.admin.service.FaqAdminService
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.FaqSettingsQuery
import ai.tock.shared.error
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.security.NoEncryptionPassException
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.toRequestHandler
import org.litote.kmongo.toId


class FaqVerticle : ChildVerticle<AdminException>{

    override fun configure(parent: WebVerticle<AdminException>) {
        with(parent) {

            blockingJsonPost(
                "/faq/tags",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, applicationId: String ->
                    val applicationDefinition = FrontClient.getApplicationById(applicationId.toId())
                    if (context.organization == applicationDefinition?.namespace) {
                        try {
                            FaqAdminService.searchTags(applicationDefinition._id.toString())
                        } catch (t: Exception) {
                            logger.error(t)
                            WebVerticle.badRequest("Error searching faq tags: ${t.message}")
                        }
                    } else {
                        WebVerticle.unauthorized()
                    }
                })

            blockingJsonGet(
                "/faq/settings/:applicationId",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context ->
                    val applicationDefinition = FrontClient.getApplicationById(context.pathId("applicationId"))
                    if (context.organization == applicationDefinition?.namespace) {
                        FaqAdminService.getSettings(applicationDefinition)
                    } else {
                        WebVerticle.unauthorized()
                    }
                })

            blockingJsonPost(
                "/faq/settings/:applicationId",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, faqSettingsQuery: FaqSettingsQuery ->
                    val applicationDefinition = FrontClient.getApplicationById(context.pathId("applicationId"))
                    if (context.organization == applicationDefinition?.namespace) {
                        FaqAdminService.saveSettings(applicationDefinition, faqSettingsQuery)
                    } else {
                        WebVerticle.unauthorized()
                    }
                })

            blockingJsonPost(
                "/faq",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                requestLogger<FaqDefinitionRequest, AdminException>("Save FAQ"),
                handler = toRequestHandler { context, query: FaqDefinitionRequest ->
                    if (query.utterances.isEmpty() && query.title.isBlank() && query.answer.isBlank()) {
                        WebVerticle.badRequest("Missing argument or trouble in query: $query")
                    } else {
                        val applicationDefinition = FrontClient.getApplicationByNamespaceAndName(
                            namespace = context.organization,
                            name = query.applicationName
                        )
                        if (context.organization == applicationDefinition?.namespace) {
                            return@toRequestHandler FaqAdminService.saveFAQ(query, context.userLogin, applicationDefinition)
                        } else {
                            WebVerticle.unauthorized()
                        }
                    }
                })

            blockingJsonDelete(
                "/faq/:faqId",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                simpleLogger("Delete Story", { it.path("faqId") }),
                handler = toRequestHandler { context ->
                    FaqAdminService.deleteFaqDefinition(context.organization, context.path("faqId"))
                })

            blockingJsonPost(
                "/faq/search",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                requestLogger<FaqSearchRequest, AdminException>("Search FAQ"),
                handler = toRequestHandler { context, request: FaqSearchRequest ->
                    val applicationDefinition =
                        FrontClient.getApplicationByNamespaceAndName(request.namespace, request.applicationName)
                    if (context.organization == applicationDefinition?.namespace) {
                        try {
                            measureTimeMillis(logger, context) {
                                FaqAdminService.searchFAQ(request, applicationDefinition)
                            }
                        } catch (t: NoEncryptionPassException) {
                            logger.error(t)
                            WebVerticle.badRequest("Error obfuscating faq: ${t.message}")
                        } catch (t: Exception) {
                            logger.error(t)
                            WebVerticle.badRequest("Error searching faq: ${t.message}")
                        }
                    } else {
                        WebVerticle.unauthorized()
                    }
                })
        }
    }

}