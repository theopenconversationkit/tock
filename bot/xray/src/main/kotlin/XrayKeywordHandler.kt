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

package ai.tock.bot.xray

import ai.tock.bot.admin.dialog.ActionReport
import ai.tock.bot.admin.dialog.DialogReport
import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.definition.BotDefinitionBase
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.xray.XrayKeywords.XRAY_KEYWORD
import ai.tock.bot.xray.XrayKeywords.XRAY_UPDATE_KEYWORD
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.provide
import mu.KotlinLogging

class XrayKeywordHandler {
    private val logger = KotlinLogging.logger {}
    private val dialogReportDAO: DialogReportDAO = injector.provide()
    private val jiraKeyProject = property("tock_bot_test_jira_project", "Set a key for the jira project.")

    internal fun createXray(
        keyword: String,
        bus: BotBus,
    ) {
        val params = keyword.replace(XRAY_KEYWORD, "").split(",")

        if (params.size == 1 && params[0].isBlank()) {
            bus.endRawText("Error : Test title is mandatory")
        } else {
            val labelPlanMap = emptyMap<String, String>()
            val xray =
                try {
                    val dialog = dialogReportDAO.getDialog(bus.dialog.id)!!.cleanSurrogates()
                    val linkedJira = params.getOrNull(1)?.trim()
                    val connectorName = ""
                    val testTitle = { labels: List<String> ->
                        val l = labels.filter { labelPlanMap.containsKey(it) }
                        val labelLink = if (l.isEmpty()) "" else "[${l.first()}]"
                        val linkedJiraNumber = if (linkedJira.isNullOrBlank()) "" else linkedJira.replace("$jiraKeyProject-", "") + " - "
                        "$linkedJiraNumber [AUTO]$connectorName$labelLink " +
                            (
                                params.getOrNull(0)?.run {
                                    if (isBlank()) {
                                        null
                                    } else {
                                        trim()
                                    }
                                } ?: "Test"
                            )
                    }
                    XrayService().generateXrayTest(
                        dialog,
                        testTitle,
                        linkedJira,
                        listOfNotNull(""),
                        labelPlanMap,
                    )
                } catch (e: Exception) {
                    logger.error(e)
                    null
                }
            BotDefinitionBase.endTestContextKeywordHandler(bus, false)
            bus.nextUserActionState = null
            if (xray != null) {
                bus.endRawText("Xray issue created : ${xray.key}")
            } else {
                bus.endRawText("Error during issue creation")
            }
        }
    }

    internal fun updateXray(
        keyword: String,
        bus: BotBus,
    ) {
        val params = keyword.replace(XRAY_UPDATE_KEYWORD, "")
        val testKey = params.trim()

        if (testKey.isBlank()) {
            bus.endRawText("Error: test key is mandatory")
        } else if (!testKey.contains(jiraKeyProject)) {
            bus.endRawText("Error: project key (${params.split("-")[0].trim()}) does not match expected one ($jiraKeyProject)")
        } else {
            val xray =
                try {
                    val dialog = dialogReportDAO.getDialog(bus.dialog.id)!!.cleanSurrogates()
                    XrayService().updateXrayTest(dialog, testKey)
                } catch (e: Exception) {
                    logger.error(e)
                    null
                }
            BotDefinitionBase.endTestContextKeywordHandler(bus, false)
            bus.nextUserActionState = null
            if (xray != null) {
                bus.endRawText("Xray issue updated : $testKey")
            } else {
                bus.endRawText("Error during update of issue $testKey")
            }
        }
    }

    private fun DialogReport.cleanSurrogates(): DialogReport {
        val cleanedActions: MutableList<ActionReport> = mutableListOf()

        this.actions.forEachIndexed { index, currentAction ->
            var currentActionMessage = currentAction.message.toPrettyString()
            if (currentAction.message.isSimpleMessage()) {
                currentActionMessage.forEach { c ->
                    if (c.isSurrogate()) {
                        currentActionMessage = currentActionMessage.replace("$c", "")
                    }
                }
                cleanedActions.add(currentAction.copy(message = Sentence(currentActionMessage)))
            } else {
                cleanedActions.add(this.actions[index])
            }
        }
        return DialogReport(cleanedActions, this.userInterface, this.id)
    }
}
