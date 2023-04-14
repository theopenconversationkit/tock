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

import ai.tock.bot.admin.I18nCsvCodec
import ai.tock.bot.admin.model.BotI18nLabel
import ai.tock.bot.admin.model.BotI18nLabels
import ai.tock.bot.admin.model.CreateI18nLabelRequest
import ai.tock.bot.admin.model.I18LabelQuery
import ai.tock.bot.admin.service.BotAdminService
import ai.tock.nlp.admin.model.TranslateReport
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.provide
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.toRequestHandler
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.Translator
import ai.tock.translator.TranslatorEngine
import org.litote.kmongo.toId


class I18nVerticle(val i18n: I18nDAO) : ChildVerticle<AdminException>{

    override fun configure(parent: WebVerticle<AdminException>) {
        with(parent) {

            blockingJsonGet("/i18n",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context ->
                    val stats = i18n.getLabelStats(context.organization).groupBy { it.labelId }
                    BotI18nLabels(
                        i18n
                            .getLabels(context.organization)
                            .map {
                                BotI18nLabel(
                                    it,
                                    stats[it._id] ?: emptyList()
                                )
                            }
                    )
                })


            blockingJsonPost(
                "/i18n/complete",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                simpleLogger("Complete Responses Labels"),
                handler = toRequestHandler { context, labels: List<I18nLabel> ->
                    if (!injector.provide<TranslatorEngine>().supportAdminTranslation) {
                        WebVerticle.badRequest("Translation is not activated for this account")
                    }
                    TranslateReport(Translator.completeAllLabels(labels.filter { it.namespace == context.organization }))
                })

            blockingJsonPost(
                "/i18n/saveAll",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                simpleLogger("Save Responses Labels"),
                handler = toRequestHandler { context, labels: List<I18nLabel> ->
                    i18n.save(labels.filter { it.namespace == context.organization })
                })

            blockingJsonPost(
                "/i18n/save",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                simpleLogger("Save Response Label"),
                handler = toRequestHandler { context, label: I18nLabel ->
                    if (label.namespace == context.organization) {
                        i18n.save(label)
                    } else {
                        WebVerticle.unauthorized()
                    }
                })

            blockingJsonPost(
                "/i18n/create",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                simpleLogger("Create Response Label"),
                handler = toRequestHandler { context, request: CreateI18nLabelRequest ->
                    BotAdminService.createI18nRequest(context.organization, request)
                })

            blockingDelete(
                "/i18n/:id",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                simpleLogger("Delete Response Label", { it.path("id") }),
                handler = toRequestHandler { context ->
                    i18n.deleteByNamespaceAndId(context.organization, context.pathId("id"))
                })


            blockingUploadPost(
                "/i18n/import/csv",
                setOf(TockUserRole.botUser),
                simpleLogger("CSV Import Response Labels"),
                handler = toRequestHandler { context, content ->
                    measureTimeMillis(logger, context) {
                        I18nCsvCodec.importCsv(context.organization, content)
                    }
                })


            blockingUploadJsonPost(
                "/i18n/import/json",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                simpleLogger("JSON Import Response Labels"),
                handler = toRequestHandler { context, labels: List<I18nLabel> ->
                    measureTimeMillis(logger, context) {
                        labels
                            .filter { it.i18n.any { i18n -> i18n.validated } }
                            .map {
                                it.copy(
                                    _id = it._id.toString().replaceFirst(it.namespace, context.organization).toId(),
                                    namespace = context.organization
                                )
                            }.apply {
                                i18n.save(this)
                            }
                            .size
                    }
                })


            blockingJsonGet(
                "/i18n/export/csv",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context ->
                    I18nCsvCodec.exportCsv(context.organization)
                })

            blockingJsonPost("/i18n/export/csv",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, query: I18LabelQuery ->
                    I18nCsvCodec.exportCsv(context.organization, query)
                })


            blockingJsonGet("/i18n/export/json",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context ->
                    mapper.writeValueAsString(i18n.getLabels(context.organization))
                })

            blockingJsonPost("/i18n/export/json",
                setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, query: I18LabelQuery ->
                    val labels = i18n.getLabels(context.organization, query.toI18nLabelFilter())
                    mapper.writeValueAsString(labels)
                })
        }
    }

}