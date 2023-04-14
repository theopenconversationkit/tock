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

import ai.tock.bot.admin.dialog.DialogReportQuery
import ai.tock.bot.admin.model.DialogsSearchQuery
import ai.tock.bot.admin.service.BotAdminService
import ai.tock.nlp.front.client.FrontClient
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.toRequestHandler


class DialogVerticle : ChildVerticle<AdminException>{

    override fun configure(parent: WebVerticle<AdminException>) {
        with(parent) {
            blockingJsonGet("/dialog/:applicationId/:dialogId", setOf(TockUserRole.botUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context ->
                    val app = FrontClient.getApplicationById(context.pathId("applicationId"))
                    if (context.organization == app?.namespace) {
                        BotAdminService.dialogReportDAO
                            .search(
                                DialogReportQuery(
                                    context.organization,
                                    app.name,
                                    dialogId = context.path("dialogId")
                                )
                            )
                            .run {
                                dialogs.firstOrNull()
                            }
                    } else {
                        WebVerticle.unauthorized()
                    }
                })

            blockingJsonPost(
                "/dialogs/search",
                setOf(TockUserRole.botUser, TockUserRole.faqNlpUser, TockUserRole.faqBotUser),
                handler = toRequestHandler { context, query: DialogsSearchQuery ->
                    if (context.organization == query.namespace) {
                        BotAdminService.search(query)
                    } else {
                        WebVerticle.unauthorized()
                    }
                })
        }
    }

}