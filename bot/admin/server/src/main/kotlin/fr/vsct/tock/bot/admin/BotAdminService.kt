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

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.admin.dialog.DialogReport
import fr.vsct.tock.bot.admin.dialog.DialogReportDAO
import fr.vsct.tock.bot.admin.model.UserSearchQuery
import fr.vsct.tock.bot.admin.user.UserReportDAO
import fr.vsct.tock.bot.admin.user.UserReportQueryResult
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.shared.injector
import mu.KotlinLogging

/**
 *
 */
object BotAdminService {

    private val logger = KotlinLogging.logger {}

    val front = FrontClient
    val userReportDAO: UserReportDAO  by injector.instance()
    val dialogReportDAO: DialogReportDAO  by injector.instance()

    fun searchUsers(query: UserSearchQuery): UserReportQueryResult {
        return userReportDAO.search(query.toSearchQuery(query.namespace, query.applicationName))
    }

    fun lastDialog(playerId:PlayerId): DialogReport {
        return dialogReportDAO.lastDialog(playerId)
    }
}