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

import ai.tock.bot.admin.dashboard.BotContact
import ai.tock.bot.admin.service.BotDashboardService
import ai.tock.bot.admin.service.BotIdentityRequest
import ai.tock.bot.admin.service.BotNoteRequest
import ai.tock.shared.security.TockUserRole.admin
import ai.tock.shared.security.TockUserRole.botUser
import ai.tock.shared.vertx.WebVerticle
import io.vertx.ext.web.RoutingContext

class DashboardVerticle : AbstractNamespaceRetriever() {
    fun configure(verticle: WebVerticle) {
        with(verticle) {
            blockingJsonGet("/bots/:botId/identity", setOf(botUser, admin)) { context ->
                checkNamespaceAndExecute(context, ::currentContextApp) { app -> BotDashboardService.identity(app.namespace, app.name) }
            }
            blockingJsonPut("/bots/:botId/identity", setOf(admin)) { context: RoutingContext, request: BotIdentityRequest ->
                checkNamespaceAndExecute(context, ::currentContextApp) { app -> BotDashboardService.saveIdentity(app.namespace, app.name, request, context.userLogin) }
            }
            blockingJsonGet("/bots/:botId/contacts", setOf(botUser, admin)) { context ->
                checkNamespaceAndExecute(context, ::currentContextApp) { app -> BotDashboardService.contacts(app.namespace, app.name) }
            }
            blockingJsonPut("/bots/:botId/contacts", setOf(admin)) { context: RoutingContext, request: List<BotContact> ->
                checkNamespaceAndExecute(context, ::currentContextApp) { app -> BotDashboardService.saveContacts(app.namespace, app.name, request) }
            }
            blockingJsonGet("/bots/:botId/index-sessions/:sessionId/note", setOf(botUser, admin)) { context ->
                checkNamespaceAndExecute(context, ::currentContextApp) { app -> BotDashboardService.note(app.namespace, app.name, context.pathParam("sessionId")) }
            }
            blockingJsonPut("/bots/:botId/index-sessions/:sessionId/note", setOf(admin)) { context: RoutingContext, request: BotNoteRequest ->
                checkNamespaceAndExecute(context, ::currentContextApp) { app -> BotDashboardService.saveNote(app.namespace, app.name, context.pathParam("sessionId"), request, context.userLogin) }
            }
            blockingJsonGet("/bots/:botId/history", setOf(botUser, admin)) { context ->
                checkNamespaceAndExecute(
                    context,
                    ::currentContextApp,
                ) { app -> BotDashboardService.history(app.namespace, app.name, context.request().getParam("before"), context.request().getParam("limit")) }
            }
        }
    }
}
