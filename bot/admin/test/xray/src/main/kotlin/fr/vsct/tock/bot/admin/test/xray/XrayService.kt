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
import fr.vsct.tock.bot.admin.dialog.DialogReport
import fr.vsct.tock.bot.admin.test.TestActionReport
import fr.vsct.tock.bot.admin.test.TestDialogReport
import fr.vsct.tock.bot.admin.test.TestPlan
import fr.vsct.tock.bot.admin.test.xray.model.JiraTest
import fr.vsct.tock.bot.admin.test.xray.model.XrayAttachment
import fr.vsct.tock.bot.admin.test.xray.model.XrayBuildStepAttachment
import fr.vsct.tock.bot.admin.test.xray.model.XrayBuildTestStep
import fr.vsct.tock.bot.admin.test.xray.model.XrayExecutionConfiguration
import fr.vsct.tock.bot.admin.test.xray.model.XrayPrecondition
import fr.vsct.tock.bot.admin.test.xray.model.XrayStatus.FAIL
import fr.vsct.tock.bot.admin.test.xray.model.XrayStatus.PASS
import fr.vsct.tock.bot.admin.test.xray.model.XrayStatus.TODO
import fr.vsct.tock.bot.admin.test.xray.model.XrayTest
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestExecution
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestExecutionInfo
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestExecutionReport
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestExecutionStepReport
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.message.parser.MessageParser
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType.bot
import fr.vsct.tock.bot.engine.user.PlayerType.user
import fr.vsct.tock.bot.jackson.BotEngineJacksonConfiguration
import fr.vsct.tock.shared.defaultZoneId
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.jackson.addSerializer
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.listProperty
import fr.vsct.tock.shared.property
import fr.vsct.tock.translator.UserInterfaceType
import mu.KotlinLogging
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64

/**
 *
 */
object XrayService {

    private val logger = KotlinLogging.logger {}

    private val testedBotId: String = property("tock_bot_test_botId", "please set a bot id to test")
    private val startSentence: String = property("tock_bot_test_start_sentence", "")
    private val userId = PlayerId("testUser", user)
    private val botId = PlayerId("testBot", bot)
    private val configurationNames = listProperty("tock_bot_test_configuration_names", emptyList())
    private val jiraProject: String = property("tock_bot_test_jira_project", "please set a jira project")
    private val testTypeField: String = property("tock_bot_test_jira_xray_test_type_field", "please set a test type field")
    private val manualStepsField: String = property("tock_bot_test_jira_xray_manual_test_field", "please set a manual type field")
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

    fun executePlans(): Boolean {
        return try {
            TockTestClient.getBotConfigurations(testedBotId)
                    .filter { it.connectorType == ConnectorType.rest }
                    .filter { configurationNames.isEmpty() || configurationNames.contains(it.name) }
                    .map {
                        exec(XrayExecutionConfiguration(it))
                    }
                    .all { it }
        } catch (t: Throwable) {
            logger.error(t)
            false
        }
    }

    private fun exec(configuration: XrayExecutionConfiguration): Boolean {
        return configuration
                .testPlanKeys
                .mapNotNull { planKey ->
                    logger.info { "start plan $planKey execution" }
                    try {
                        val testPlan = getTestPlan(configuration, planKey)
                        if (testPlan.dialogs.isNotEmpty()) {
                            executePlan(configuration, testPlan)
                        } else {
                            logger.warn { "empty test plan for $configuration - skipped" }
                            null
                        }
                    } finally {
                        logger.info { "plan $planKey executed" }
                    }
                }
                .all { it }
    }

    private fun executePlan(
            configuration: XrayExecutionConfiguration,
            testPlan: TestPlan): Boolean {
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
                            if (dialogReport.error) "Failed execution ${dialogReport.errorMessage ?: ""}" else "Successful execution",
                            if (dialogReport.error) FAIL else PASS,
                            if (dialog == null) {
                                emptyList()
                            } else {
                                dialog.actions.filter {
                                    it.playerId.type == bot
                                }.map {
                                    val status = if (!dialogReport.error) {
                                        PASS
                                    } else if (stepViewed) {
                                        TODO
                                    } else if (dialogReport.errorActionId == it.id) {
                                        stepViewed = true
                                        FAIL
                                    } else {
                                        PASS
                                    }
                                    XrayTestExecutionStepReport(
                                            when (status) {
                                                PASS -> "Test successful"
                                                TODO -> "Skipped"
                                                FAIL ->
                                                    if (dialogReport.returnedMessage != null) {
                                                        "TestFailed : ${dialogReport.returnedMessage?.toPrettyString()}"
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
        return if (
        XrayClient.sendTestExecution(xrayExecution).isSuccessful
                && execution.nbErrors == 0) {
            xrayExecution.tests.all { it.status == PASS }
        } else {
            false
        }
    }

    private fun getTestPlan(configuration: XrayExecutionConfiguration, planKey: String): TestPlan {
        val tests = XrayClient.getTestPlanTests(planKey)
        return with(configuration) {
            TestPlan(
                    tests
                            .filter { it.supportConf(configuration.botConfiguration.name) }
                            .map { getDialogReport(configuration, it) },
                    planKey,
                    botConfiguration.applicationId,
                    botConfiguration.namespace,
                    botConfiguration.nlpModel,
                    botConfiguration._id!!,
                    if (startSentence.isBlank()) null else MessageParser.parse(startSentence).first(),
                    botConfiguration.targetConnectorType,
                    "planKey_${configuration.botConfiguration.applicationId}"
            )
        }
    }

    private fun getDialogReport(configuration: XrayExecutionConfiguration, test: XrayTest): TestDialogReport {
        val userInterface = test.findUserInterface()
        val steps = XrayClient.getTestSteps(test.key)
        return TestDialogReport(
                steps.flatMap {
                    listOfNotNull(
                            parseStepData(configuration, userInterface, it.id, userId, it.data.raw, it.attachments.firstOrNull { it.fileName == "user.message" }),
                            parseStepData(configuration, userInterface, it.id, botId, it.result.raw, it.attachments.firstOrNull { it.fileName == "bot.message" })
                    )
                }, test.key)
    }

    private fun parseStepData(
            configuration: XrayExecutionConfiguration,
            userInterface: UserInterfaceType,
            stepId: Long,
            playerId: PlayerId,
            raw: String?,
            attachment: XrayAttachment?): TestActionReport? {
        val message = if (attachment != null) {
            XrayClient.getAttachmentToString(attachment)
        } else {
            raw
        }
        return message
                .takeUnless { it.isNullOrBlank() }
                ?.run {
                    TestActionReport(
                            playerId,
                            instant,
                            MessageParser.parse(replace("\${botUrl}", configuration.botUrl)),
                            configuration.botConfiguration.targetConnectorType,
                            userInterface,
                            "${if (playerId.type == bot) "b" else "u"}${stepId}")
                }
    }

    /**
     * Generate an Xray test.
     *
     * @param dialog the dialog used to build the test
     * @param testName the optional test name
     * @param linkedJira the optional User Story ticket related to the test
     * @param labelTestPlansMap a map of label -> test plan key.
     * if [linkedJira] is set, get the labels of this US and add the test to all test plans specified in the map with the retrieved labels.
     *
     */
    fun generateXrayTest(
            dialog: DialogReport,
            testName: String = "Test",
            linkedJira: String? = null,
            labelTestPlansMap: Map<String, String> = emptyMap()): XrayTest? {
        if (dialog.actions.isEmpty()) {
            logger.warn { "no action for dialog $dialog" }
            return null
        }
        //define steps
        val steps: MutableList<XrayBuildTestStep> = mutableListOf()
        dialog.actions.forEach { a ->
            val user = a.playerId.type == user
            val m = a.message
            val mData = if (m.isSimpleMessage()) m.toPrettyString() else ""
            val mAttachments =
                    listOfNotNull(
                            if (m.isSimpleMessage()) null
                            else XrayBuildStepAttachment(
                                    String(Base64.getEncoder().encode(m.toPrettyString().toByteArray())),
                                    if (user) "user.message" else "bot.message"
                            )
                    )

            if (user) {
                steps.add(
                        XrayBuildTestStep(
                                (steps.size + 1).toString(),
                                mData,
                                "",
                                mAttachments
                        )
                )
            } else {
                steps.lastOrNull()?.apply {
                    if (result.isNotBlank() || attachments.any { it.filename == "bot.message" }) {
                        steps.add(
                                XrayBuildTestStep(
                                        (steps.size + 1).toString(),
                                        "",
                                        mData,
                                        mAttachments
                                )
                        )
                    } else {
                        steps.removeAt(steps.size - 1)
                        steps.add(
                                copy(
                                        result = mData,
                                        attachments = attachments + mAttachments
                                )
                        )
                    }
                }
                        ?: logger.warn { "no first step for $dialog" }
            }

        }
        //create test
        val test = JiraTest(
                jiraProject,
                testName,
                "",
                testTypeField,
                manualStepsField
        )
        val jira = XrayClient.createTest(test)
        if (linkedJira != null) {
            XrayClient.linkTest(jira.key, linkedJira)
            if (labelTestPlansMap.isNotEmpty()) {
                val labels = XrayClient.getLabels(linkedJira)
                labels
                        .filter { labelTestPlansMap.containsKey(it) }
                        .forEach {
                            XrayClient.addTestToTestPlan(jira.key, labelTestPlansMap[it]!!)
                        }
            }
        }

        steps.forEach {
            XrayClient.saveStep(jira.key, it)
        }

        XrayPrecondition.getPreconditionForUserInterface(dialog.actions.first().userInterfaceType)
                ?.apply {
                    XrayClient.addPrecondition(this, jira.key)
                }

        return XrayTest(jira.key)
    }
}