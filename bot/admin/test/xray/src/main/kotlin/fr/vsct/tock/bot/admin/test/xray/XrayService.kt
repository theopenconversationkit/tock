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

package fr.vsct.tock.bot.admin.test.xray

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer
import fr.vsct.tock.bot.admin.dialog.ActionReport
import fr.vsct.tock.bot.admin.dialog.DialogReport
import fr.vsct.tock.bot.admin.test.TestPlan
import fr.vsct.tock.bot.admin.test.xray.model.XrayExecutionConfiguration
import fr.vsct.tock.bot.admin.test.xray.model.XrayStatus
import fr.vsct.tock.bot.admin.test.xray.model.XrayTest
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestExecution
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestExecutionInfo
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestExecutionReport
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestExecutionStepReport
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.message.Sentence
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.jackson.BotEngineJacksonConfiguration
import fr.vsct.tock.shared.defaultZoneId
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.jackson.addSerializer
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.property
import mu.KotlinLogging
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 *
 */
object XrayService {

    private val logger = KotlinLogging.logger {}

    private val testedBotId: String = property("tock_bot_test_botId", "please set a bot id to test")
    private val startSentence: String = property("tock_bot_test_start_sentence", "")
    private val userId = PlayerId("testUser", PlayerType.user)
    private val botId = PlayerId("testBot", PlayerType.bot)
    private val instant = Instant.now()

    init {
        BotEngineJacksonConfiguration.init()
        mapper.registerModule(
                SimpleModule()
                        .addSerializer(
                                OffsetDateTime::class,
                                object : OffsetDateTimeSerializer(
                                        OffsetDateTimeSerializer.INSTANCE,
                                        true,
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZ")) {
                                }
                        )
        )
    }

    fun executePlans() {
        try {
            TockTestClient.getBotConfigurations(testedBotId)
                    .filter { it.connectorType == ConnectorType.rest }
                    .forEach {
                        exec(XrayExecutionConfiguration(it))
                    }
        } catch(t: Throwable) {
            logger.error(t)
        }
    }

    private fun exec(configuration: XrayExecutionConfiguration) {
        configuration.testPlanKeys.forEach { planKey ->
            logger.info { "start plan $planKey execution" }
            val testPlan = getTestPlan(configuration, planKey)
            executePlan(configuration, testPlan)
            logger.info { "plan $planKey executed" }
        }
    }

    private fun executePlan(
            configuration: XrayExecutionConfiguration,
            testPlan: TestPlan) {
        val now = OffsetDateTime.now()
        val execution = TockTestClient.executeTestPlan(testPlan)
        val end = OffsetDateTime.now()

        val xrayExecution = XrayTestExecution(
                XrayTestExecutionInfo(
                        "${testPlan.name} - ${configuration.botConfiguration.name}",
                        "Automatized Test Execution",
                        now,
                        end,
                        testPlan.name,
                        listOf(configuration.environment)

                ),
                execution.dialogs.map { dialogReport ->
                    val dialog = testPlan.dialogs.firstOrNull {
                        it.id == dialogReport.dialogReportId
                    }
                    var stepViewed = false
                    XrayTestExecutionReport(
                            dialogReport.dialogReportId,
                            OffsetDateTime.ofInstant(dialogReport.date, defaultZoneId),
                            OffsetDateTime.ofInstant(dialogReport.date, defaultZoneId).plus(dialogReport.duration),
                            if (dialogReport.error) "Failed execution" else "Successful execution",
                            if (dialogReport.error) XrayStatus.FAIL else XrayStatus.PASS,
                            if (dialog == null) {
                                emptyList()
                            } else {
                                dialog.actions.filter {
                                    it.playerId.type == PlayerType.bot
                                }.map {
                                    val status = if (!dialogReport.error) {
                                        XrayStatus.PASS
                                    } else if (stepViewed) {
                                        XrayStatus.TODO
                                    } else if (dialogReport.errorActionId == it.id) {
                                        stepViewed = true
                                        XrayStatus.FAIL
                                    } else {
                                        XrayStatus.PASS
                                    }
                                    XrayTestExecutionStepReport(
                                            when (status) {
                                                XrayStatus.PASS -> "Test successful"
                                                XrayStatus.TODO -> "Skipped"
                                                XrayStatus.FAIL ->
                                                    if (dialogReport.returnedMessage != null) {
                                                        "TestFailed : ${dialogReport.returnedMessage?.toStringDisplay()}"
                                                    } else if (dialogReport.errorMessage != null) {
                                                        "TestFailed : ${dialogReport.errorMessage}"
                                                    } else {
                                                        "Test failed"
                                                    }
                                            },
                                            status
                                    )
                                }
                            }
                    )
                }
        )
        XrayClient.sendTestExecution(xrayExecution)
    }

    private fun getTestPlan(configuration: XrayExecutionConfiguration, planKey: String): TestPlan {
        val tests = XrayClient.getTestPlanTests(planKey)
        return with(configuration) {
            TestPlan(
                    tests.map { getDialogReport(it) },
                    planKey,
                    botConfiguration.applicationId,
                    botConfiguration.namespace,
                    botConfiguration.nlpModel,
                    botConfiguration._id!!,
                    if (startSentence.isBlank()) null else Sentence(startSentence),
                    "planKey_${configuration.botConfiguration.applicationId}"
            )
        }
    }

    private fun getDialogReport(test: XrayTest): DialogReport {
        val steps = XrayClient.getTestSteps(test.key)
        return DialogReport(
                steps.flatMap {
                    listOf(
                            ActionReport(userId, instant, Sentence(it.data.raw), "u${it.id}"),
                            ActionReport(botId, instant, Sentence(it.result.raw), "b${it.id}")
                    )
                }, test.key)
    }

}