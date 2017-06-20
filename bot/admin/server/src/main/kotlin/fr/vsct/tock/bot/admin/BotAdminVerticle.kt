/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.admin

import fr.vsct.tock.bot.admin.model.DialogReportRequest
import fr.vsct.tock.bot.admin.model.BotDialogRequest
import fr.vsct.tock.bot.admin.model.UserSearchQuery
import fr.vsct.tock.nlp.admin.AdminVerticle
import fr.vsct.tock.nlp.admin.model.ApplicationScopedQuery
import mu.KotlinLogging

/**
 *
 */
class BotAdminVerticle : AdminVerticle(KotlinLogging.logger {}) {

    override fun configure() {
        super.configure()

        blockingJsonPost("/users/search") { context, query: UserSearchQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.searchUsers(query)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/dialogs/user") { context, query: DialogReportRequest ->
            if (context.organization == query.namespace) {
                BotAdminService.lastDialog(query.playerId)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/configuration/bots") { context, query: ApplicationScopedQuery ->
            if (context.organization == query.namespace) {
                BotAdminService.getRestApplicationConfigurations(query.namespace)
            } else {
                unauthorized()
            }
        }

        blockingJsonPost("/test/talk") { context, query: BotDialogRequest ->
            if (context.organization == query.namespace) {
                BotAdminService.talk(query)
            } else {
                unauthorized()
            }
        }
    }
}