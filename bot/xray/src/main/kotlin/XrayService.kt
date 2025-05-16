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

import ai.tock.bot.admin.dialog.DialogReport
import ai.tock.bot.admin.test.DialogExecutionReport
import ai.tock.bot.admin.test.TestActionReport
import ai.tock.bot.admin.test.TestDialogReport
import ai.tock.bot.admin.test.TestPlan
import ai.tock.bot.admin.test.TestPlanDAO
import ai.tock.bot.admin.test.TestPlanExecution
import ai.tock.bot.admin.test.TestPlanExecutionStatus
import ai.tock.bot.admin.test.findTestClient
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.message.parser.MessageParser
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType.bot
import ai.tock.bot.engine.user.PlayerType.user
import ai.tock.bot.xray.XrayClient.getProjectFromIssue
import ai.tock.bot.xray.XrayClient.getProjectTestPlans
import ai.tock.bot.xray.model.JiraIssueType
import ai.tock.bot.xray.model.JiraTest
import ai.tock.bot.xray.model.XrayAttachment
import ai.tock.bot.xray.model.XrayBuildStepAttachment
import ai.tock.bot.xray.model.XrayBuildTestStep
import ai.tock.bot.xray.model.XrayExecutionConfiguration
import ai.tock.bot.xray.model.XrayPrecondition
import ai.tock.bot.xray.model.XrayStatus.FAIL
import ai.tock.bot.xray.model.XrayStatus.PASS
import ai.tock.bot.xray.model.XrayStatus.TODO
import ai.tock.bot.xray.model.XrayTest
import ai.tock.bot.xray.model.XrayTestExecution
import ai.tock.bot.xray.model.XrayTestExecutionCreation
import ai.tock.bot.xray.model.XrayTestExecutionInfo
import ai.tock.bot.xray.model.XrayTestExecutionReport
import ai.tock.bot.xray.model.XrayTestExecutionStepReport
import ai.tock.bot.xray.model.XrayTestPlan
import ai.tock.bot.xray.model.XrayTextExecutionFields
import ai.tock.shared.Dice
import ai.tock.shared.defaultLocale
import ai.tock.shared.defaultZoneId
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.listProperty
import ai.tock.shared.mapListProperty
import ai.tock.shared.property
import ai.tock.shared.provide
import ai.tock.translator.UserInterfaceType
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.OffsetDateTime.ofInstant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 *
 */
class XrayService(
    private val configurationIds: List<String> = listProperty("tock_bot_test_configuration_ids", emptyList()),
    private val testPlanKeys: List<String> = listProperty("tock_bot_test_xray_test_plan_keys", emptyList()),
    private val testKeys: List<String> = listProperty("tock_bot_test_xray_test_keys", emptyList()),
    private val testedBotId: String = property("tock_bot_test_botId", "please set a bot id to test"),
) {

    private class TestPlanExecutionReport(
        val configuration: XrayExecutionConfiguration,
        val planKey: String,
        val testPlan: TestPlan,
        val execution: TestPlanExecution,
    )

    private val logger = KotlinLogging.logger {}

    private val configurationNames: List<String> = listProperty("tock_bot_test_configuration_names", emptyList())
    private val startSentence: String = property("tock_bot_test_start_sentence", "")
    private val userId = PlayerId("testUser", user)
    private val botId = PlayerId("testBot", bot)
    private val connectorJiraMap = mapListProperty("tock_bot_test_connector_jira_map", emptyMap())
    private val jiraProject: String = property("tock_bot_test_jira_project", "please set a jira project")
    private val testTypeField: String =
        property("tock_bot_test_jira_xray_test_type_field", "please set a test type field")
    private val manualStepsField: String =
        property("tock_bot_test_jira_xray_manual_test_field", "please set a test type field")
    private val linkedField: String = property("tock_bot_test_jira_linked_field", "")
    private val locale: Locale = Locale.forLanguageTag(property("tock_bot_test_locale", defaultLocale.toLanguageTag()))
    private val instant = Instant.now()
    private val jiraRegisteredBotUrl: String = property("tock_bot_test_jira_registered_bot_url", "\${botUrl}")

    init {
        XrayConfiguration.configure()
    }

    fun getTestPlans(): List<XrayTestPlan> = getProjectTestPlans(jiraProject)

    fun execute(namespace: String): XrayPlanExecutionResult {
        return when {
            testKeys.isNotEmpty() && testPlanKeys.isNotEmpty() -> throw AssertionError("Cannot execute test plan and test at the same time.")
            testKeys.isNotEmpty() -> executeTests(namespace)
            testPlanKeys.isNotEmpty() -> executeTestPlans(namespace)
            else -> {
                logger.warn { "Specify \"testPlanKey\" OR (xor) \"testKey\"." }
                XrayPlanExecutionResult(
                    success = 0,
                    total = 0,
                    errorMessage = "ERROR : Specify \"testPlanKey\" OR (xor) \"testKey\"."
                )
            }
        }
    }

    /**
     * Execution of one or several test plans.
     * It will retrieve the right connector and then execute all tests belonging to targeted test plans using Xray links.
     * Once tests are executed, results are send to Xray.
     */
    fun executeTestPlans(namespace: String): XrayPlanExecutionResult {
        logger.info { "execute plans with namespace $namespace" }
        val jiraProject = getProjectFromIssue(testPlanKeys[0])
        val executionId = Dice.newId()

        return try {
            // getBotConfiguration retrieves all configuration for the selected namespace
            // getBotConfiguration will reach information stored in tab Configuration on BotAdmin site
            findTestClient().getBotConfigurations(namespace, testedBotId)
                // retrieve all REST connectors and select the good one
                .filter { it.connectorType == ConnectorType.rest }
                .filter {
                    configurationIds.contains(it._id.toString()) ||
                        (configurationIds.isEmpty() && configurationNames.contains(it.name))
                }
                // test plan execution
                .flatMap {
                    exec(XrayExecutionConfiguration(it, testPlanKeys, jiraProject), executionId.toId())
                }
                .let {
                    sendToXray(it)
                }
        } catch (t: Throwable) {
            logger.error(t)
            XrayPlanExecutionResult(0, 0, "No test run")
        }
    }

    /**
     * Execution of only one test instead of an entire test plan.
     * Test execution has to be linked to an existing test plan, so in this function, a dummy test plan is used.
     * Execution data will not be saved in the database, there will be no history about it.
     */
    fun executeTests(namespace: String): XrayPlanExecutionResult {
        val dummyTestPlanKey = "AUTO_${testKeys[0]}"
        val jiraProject = getProjectFromIssue(testKeys[0])
        val executionId = Dice.newId()

        logger.info { "Execute tests with namespace $namespace" }
        logger.debug { "Execution with id : $executionId started for test plan $dummyTestPlanKey" }
        return try {
            // getBotConfiguration retrieves all configuration for the selected namespace
            // getBotConfiguration will reach information stored in tab Configuration on BotAdmin site
            findTestClient().getBotConfigurations(namespace, testedBotId)
                // retrieve all REST connectors and select the good one
                .filter { it.connectorType == ConnectorType.rest }
                .filter {
                    configurationIds.contains(it._id.toString()) ||
                        (configurationIds.isEmpty() && configurationNames.contains(it.name))
                }
                // test execution of a dummy test plan
                .flatMap {
                    execTestsOnly(
                        XrayExecutionConfiguration(it, listOf(dummyTestPlanKey), jiraProject),
                        dummyTestPlanKey,
                        testKeys,
                        executionId.toId()
                    )
                }
                .let {
                    logger.debug { "Execution with id : $executionId ended for test plan $dummyTestPlanKey" }
                    sendToXray(it)
                }
        } catch (t: Throwable) {
            logger.error(t.message)
            XrayPlanExecutionResult(0, 0, "No test run: ${t.message}")
        }
    }

    /**
     * This function will take all the test plan executions results and send them to Xray as a Test Execution.
     *
     * @param reports is the list of the reports of all test plan executions.
     * @return the results of the test plan execution as a XRay Test Execution
     */
    private fun sendToXray(reports: List<TestPlanExecutionReport>): XrayPlanExecutionResult {
        reports
            .groupBy { it.planKey }
            .forEach { planKey, plans ->
                // if it is a multi-connector test plan
                if (plans.map { it.testPlan.dialogs.map { dialogExecutionReport -> dialogExecutionReport.id } }
                    // no duplicate
                    .run { toSet().size == size }
                ) {
                    val firstExecution = plans.map { it.execution.date }.minOrNull()!!
                    sendXrayExecution(
                        planKey,
                        ofInstant(firstExecution, defaultZoneId),
                        ofInstant(
                            firstExecution.plus(
                                plans.map { it.execution.duration.toMillis() }.sum(),
                                ChronoUnit.MILLIS
                            ),
                            defaultZoneId
                        ),
                        plans.map { it.configuration }.distinctBy { it.botConfiguration.name },
                        plans.flatMap { it.testPlan.dialogs },
                        plans.flatMap { it.execution.dialogs }
                    )
                } else {
                    plans.all {
                        sendXrayExecution(
                            planKey,
                            ofInstant(it.execution.date, defaultZoneId),
                            ofInstant(it.execution.date.plus(it.execution.duration), defaultZoneId),
                            listOf(it.configuration),
                            it.testPlan.dialogs,
                            it.execution.dialogs
                        )
                    }
                }
            }

        return XrayPlanExecutionResult(
            success = reports.sumOf { it.execution.dialogs.filter { dialogExecutionReport -> !dialogExecutionReport.error }.size },
            total = reports.sumOf { it.execution.dialogs.size },
            errorMessage = reports.map { it.execution.dialogs.find { dialogExecutionReport -> dialogExecutionReport.error }?.errorMessage }
                .toString()
        )
    }

    /**
     * This function sends each test dialogs to Xray with its result (fail or not).
     *
     * @param planKey is the identifier of the executed test plan.
     * @param start is the start time of test plan execution.
     * @param end is the end time of test plan execution.
     * @param configurations is the configuration used to launch test plan execution.
     * @param dialogs is the list of all dialogs of the executed test plan.
     * @param executionDialogs is the list of reports of executed dialogs.
     * @return the status of the whole execution - true if all tests passed, false otherwise.
     */
    private fun sendXrayExecution(
        planKey: String,
        start: OffsetDateTime,
        end: OffsetDateTime,
        configurations: List<XrayExecutionConfiguration>,
        dialogs: List<TestDialogReport>,
        executionDialogs: List<DialogExecutionReport>,
    ): Boolean {
        // try to get the jira identifier of the test plan
        val testExecutionKeyIssue = XrayClient.getKeyOfSearchedIssue(
            "project = \"${configurations[0].jiraTestProject.key}\" and " +
                "issuetype = \"Test Execution\" and " +
                "summary ~ \"$planKey ${configurations[0].botConfiguration.name}\""
        )

        val testExecutionKey = when (testExecutionKeyIssue) {
            TooMuchIssuesRetrieved -> error("too many test executions have been retrieved with summary \"$planKey ${configurations[0].botConfiguration.name}\".")
            NoIssueRetrieved -> {
                XrayClient.createNewTestExecutionIssue(
                    XrayTestExecutionCreation(
                        XrayTextExecutionFields(
                            configurations.get(0).jiraTestProject,
                            "$planKey - ${
                            configurations.map { it.botConfiguration.name }.toSortedSet().joinToString()
                            }",
                            "Automatized Test Execution",
                            JiraIssueType("Test Execution")
                        )
                    )
                ).key
            }
            is IssueRetrieved -> testExecutionKeyIssue.key
        }

        // gather test execution information
        val xrayExecution = XrayTestExecution(
            testExecutionKey,
            XrayTestExecutionInfo(
                "$planKey - ${configurations.map { it.botConfiguration.name }.toSortedSet().joinToString()}",
                "Automatized Test Execution",
                start,
                end,
                planKey,
                configurations.map { it.environment }.toSortedSet().toList()

            ),
            executionDialogs
                .sortedBy { it.dialogReportId.toString() }
                .map { dialogReport ->
                    val dialog = dialogs.firstOrNull {
                        it.id == dialogReport.dialogReportId
                    }
                    var stepViewed = false
                    XrayTestExecutionReport(
                        testKey = dialogReport.dialogReportId.toString(),
                        start = ofInstant(dialogReport.date, defaultZoneId),
                        finish = ofInstant(dialogReport.date, defaultZoneId).plus(dialogReport.duration),
                        comment = if (dialogReport.error) "Failed execution ${
                        dialogReport.errorMessage
                            ?: ""
                        }" else "Successful execution",
                        status = if (dialogReport.error) FAIL else PASS,
                        steps = dialog?.actions?.filter {
                            it.playerId.type == bot
                        }?.map { testActionReport ->
                            val status = when {
                                !dialogReport.error -> PASS
                                stepViewed -> TODO
                                dialogReport.errorActionId == testActionReport.id -> {
                                    stepViewed = true
                                    FAIL
                                }
                                else -> PASS
                            }
                            XrayTestExecutionStepReport(
                                when (status) {
                                    PASS -> "Test successful"
                                    TODO -> "Skipped"
                                    FAIL ->
                                        when {
                                            dialogReport.returnedMessage != null -> "TestFailed : ${dialogReport.returnedMessage?.toPrettyString()}"
                                            dialogReport.errorMessage != null -> "TestFailed : ${dialogReport.errorMessage}"
                                            else -> "Test failed"
                                        }
                                },
                                status
                            )
                        }
                            ?: emptyList()
                    )
                }
        )
        // and send the execution to xray
        return if (
            XrayClient.sendTestExecution(xrayExecution).isSuccessful &&
            executionDialogs.none { it.error }
        ) {
            xrayExecution.tests.all { it.status == PASS }
        } else {
            false
        }
    }

    /**
     * For each test plan to execute, this function will retrieve the test plan issue in Xray
     * and then it will execute it.
     *
     * @param configuration is the configuration to execute the test plan.
     * @return the list of all tests plan that have been executed and their execution results.
     */
    private fun exec(
        configuration: XrayExecutionConfiguration,
        executionId: Id<TestPlanExecution>,
    ): List<TestPlanExecutionReport> {
        return configuration
            .xrayTestPlanKeys
            .mapNotNull { xrayPlanKey ->
                logger.info { "Start plan $xrayPlanKey execution" }
                try {
                    val exec = TestPlanExecution(
                        (xrayPlanKey + "_" + configuration.botConfiguration.applicationId).toId(),
                        mutableListOf(),
                        0,
                        duration = Duration.between(Instant.now(), Instant.now()),
                        _id = executionId,
                        status = TestPlanExecutionStatus.PENDING
                    )
                    // save the test plan execution into the database
                    saveTestPlanExecution(exec)

                    // retrieve the Xray issue with associated tests for the current test plan
                    val xrayTestPlan = getTestPlanFromXray(configuration, xrayPlanKey)
                    // if the current test plan has dialogs to send, then execute it, otherwise skip it and jump to the next one
                    if (xrayTestPlan.dialogs.isNotEmpty()) {
                        executePlan(configuration, xrayPlanKey, xrayTestPlan, executionId)
                    } else {
                        logger.info { "Empty test plan for $configuration - skipped" }
                        null
                    }
                } finally {
                    logger.info { "Plan $xrayPlanKey executed" }
                }
            }
    }

    private fun saveTestPlanExecution(testPlanExecution: TestPlanExecution) {
        val testPlanDAO: TestPlanDAO = injector.provide()
        testPlanDAO.saveTestExecution(testPlanExecution)
    }

    private fun execTestsOnly(
        configuration: XrayExecutionConfiguration,
        planKey: String,
        testKeys: List<String>,
        executionId: Id<TestPlanExecution>,
    ): List<TestPlanExecutionReport> {

        return configuration
            .xrayTestPlanKeys
            .mapNotNull { xrayPlanKey ->
                logger.info { "Start plan $xrayPlanKey execution for test plan $planKey" }
                try {
                    // create a test plan with all given tests
                    val testPlan = createTestPlanWithTests(configuration, planKey, testKeys)
                    logger.debug { "Common Tock test plan created : ${testPlan.hashCode()} for test plan $planKey" }
                    // if the current test plan has dialogs to send, then execute it, otherwise skip it and jump to the next one
                    if (testPlan.dialogs.isNotEmpty()) {
                        executePlan(configuration, testPlan.name, testPlan, executionId)
                    } else {
                        logger.debug { "Empty test plan for $configuration - skipped" }
                        null
                    }
                } finally {
                    logger.info { "Plan $xrayPlanKey executed for test plan $planKey" }
                }
            }
    }

    /**
     * This function will execute the common Test Plan.
     *
     * @param configuration is the configuration to execute the test plan.
     * @param xrayTestPlanKey is the Xray identifier of the test plan to execute.
     * @param testPlan is the test plan to execute formatted in the common Test Plan manageable by Tock test framework.
     * @return a TestPlanExecutionReport which contains test plan execution results
     */
    private fun executePlan(
        configuration: XrayExecutionConfiguration,
        xrayTestPlanKey: String,
        testPlan: TestPlan,
        executionId: Id<TestPlanExecution>,
    ): TestPlanExecutionReport {
        logger.debug { "Execute test plan ${testPlan.name} with execution id $executionId" }
        val execution = findTestClient().saveAndExecuteTestPlan(testPlan, executionId)
        logger.debug { "Test plan execution ${execution.testPlanId}" }
        return TestPlanExecutionReport(
            configuration,
            xrayTestPlanKey,
            testPlan,
            execution
        )
    }

    /**
     * This function converts the Xray Test Plan into a common Test Plan, understandable and usable by the testing framework of Tock.
     *
     * @param configuration is the configuration to execute the test plan.
     * @param xrayPlanKey is the Xray identifier of the current test plan.
     * @return the current test plan as a common Test Plan understandable by Tock.
     */
    private fun getTestPlanFromXray(configuration: XrayExecutionConfiguration, xrayPlanKey: String): TestPlan {
        // retrieve all tests of the test plan as XrayTest objects
        val xrayTests = XrayClient.getTestPlanTests(xrayPlanKey)
        // create the common Test Plan using the retrieved tests
        return with(configuration) {
            TestPlan(
                // retrieve all test steps
                xrayTests
                    .filter {
                        val connectorJiras = connectorJiraMap[configuration.botConfiguration.ownerConnectorType?.id]
                            ?: emptyList()
                        connectorJiras.isEmpty() || XrayClient.getLinkedIssues(it.key, linkedField).intersect(
                            connectorJiras
                        ).isNotEmpty()
                    }
                    .filter { it.supportConf(configuration.botConfiguration.name) }
                    // retrieve all steps of tests of the test plan
                    .map { getDialogReport(configuration, it) },
                xrayPlanKey,
                botConfiguration.applicationId,
                botConfiguration.namespace,
                botConfiguration.nlpModel,
                botConfiguration._id,
                locale,
                if (startSentence.isBlank()) null else MessageParser.parse(startSentence).first(),
                botConfiguration.targetConnectorType,
                "${xrayPlanKey}_${configuration.botConfiguration.applicationId}".toId()
            )
        }
    }

    private fun createTestPlanWithTests(
        configuration: XrayExecutionConfiguration,
        planKey: String,
        testKeys: List<String>,
    ): TestPlan {
        val xrayTests = mutableListOf<XrayTest>()
        testKeys.forEach {
            xrayTests.addAll(XrayClient.getTests(it))
        }

        return with(configuration) {
            TestPlan(
                xrayTests.filter { it.supportConf(configuration.botConfiguration.name) }
                    .map {
                        getDialogReport(configuration, it)
                    },
                planKey,
                botConfiguration.applicationId,
                botConfiguration.namespace,
                botConfiguration.nlpModel,
                botConfiguration._id,
                locale,
                if (startSentence.isBlank()) null else MessageParser.parse(startSentence).first(),
                botConfiguration.targetConnectorType,
                "${planKey}_${configuration.botConfiguration.applicationId}".toId()
            )
        }
    }

    /**
     * This function will retrieve all steps for a given xrayTest and parse step content.
     * It also parses content according of attachment file presence.
     * All these steps are converted in a TestDialogReport as actions.
     *
     * @param configuration
     * @param xrayTest is the XrayTest to get its steps.
     * @return all the tests steps as a TestDialogReport to submit to the targeted bot.
     */
    private fun getDialogReport(configuration: XrayExecutionConfiguration, xrayTest: XrayTest): TestDialogReport {
        val userInterface = xrayTest.findUserInterface()
        val xraySteps = XrayClient.getTestSteps(xrayTest.key)
        return TestDialogReport(
            xraySteps.flatMap { xrayTestStep ->
                listOfNotNull(
                    parseStepData(
                        configuration,
                        userInterface,
                        xrayTestStep.id,
                        userId,
                        xrayTestStep.data.raw,
                        xrayTestStep.attachments.firstOrNull { it.fileName == "user.message" }
                    ),
                    parseStepData(
                        configuration,
                        userInterface,
                        xrayTestStep.id,
                        botId,
                        xrayTestStep.result.raw,
                        xrayTestStep.attachments.firstOrNull { it.fileName == "bot.message" }
                    )
                )
            },
            userInterface,
            xrayTest.key.toId()
        )
    }

    /**
     * This function parses the content of a test step.
     * If the content of the step is stored into a attached file, then it returns its content.
     * Otherwise, it means the content is written in the provided step field, so the function returns the field content.
     *
     * @return the content of the step in String format, which can be in a file or in the field.
     */
    private fun parseStepData(
        configuration: XrayExecutionConfiguration,
        userInterface: UserInterfaceType,
        stepId: Long,
        playerId: PlayerId,
        raw: String?,
        xrayAttachment: XrayAttachment?,
    ): TestActionReport? {
        val message = if (xrayAttachment != null) {
            XrayClient.getAttachmentToString(xrayAttachment)
        } else {
            raw
        }
        return message
            .takeUnless { it.isNullOrBlank() }
            ?.run {
                TestActionReport(
                    playerId,
                    instant,
                    MessageParser.parse(replace(jiraRegisteredBotUrl, configuration.botUrl)),
                    configuration.botConfiguration.targetConnectorType,
                    userInterface,
                    "${if (playerId.type == bot) "b" else "u"}$stepId".toId()
                )
            }
    }

    /**
     * Generate an Xray test.
     *
     * @param dialog the dialog used to build the test
     * @param testName the optional test name generator - labels of [linkedJira] are passed as parameter.
     * @param linkedJira the optional User Story ticket related to the test
     * @param testsPlans the tests plans to include for this test
     * @param labelTestPlansMap a map of label -> test plan key.
     * if [linkedJira] is set, get the labels of this US and add the test to all test plans specified in the map with the retrieved labels.
     *
     */
    fun generateXrayTest(
        dialog: DialogReport,
        testName: (List<String>) -> String = { "Test" },
        linkedJira: String? = null,
        testsPlans: List<String>,
        labelTestPlansMap: Map<String, String> = emptyMap(),
    ): XrayTest? {
        val steps = defineTestSteps(dialog)

        val labels = linkedJira?.let { XrayClient.getLabels(it) } ?: emptyList()

        val test = JiraTest(
            jiraProject,
            testName.invoke(labels),
            "",
            listOf("automatisation"),
            testTypeField,
            manualStepsField,
            "cucumber-tock"
        )
        val jira = XrayClient.createTest(test)
        if (linkedJira != null) {
            XrayClient.linkTest(jira.key, linkedJira)
            testsPlans.forEach {
                XrayClient.addTestToTestPlan(jira.key, it)
            }
            if (labelTestPlansMap.isNotEmpty()) {
                labels
                    .filter { labelTestPlansMap.containsKey(it) }
                    .forEach {
                        XrayClient.addTestToTestPlan(jira.key, labelTestPlansMap[it]!!)
                    }
            }
        }

        steps?.forEach {
            XrayClient.saveStep(jira.key, it)
        }

        XrayPrecondition.getPreconditionForUserInterface(dialog.actions.first().userInterfaceType)
            ?.apply {
                XrayClient.addPrecondition(this, jira.key)
            }

        return XrayTest(jira.key)
    }

    fun updateXrayTest(dialog: DialogReport, testKey: String): XrayTest? {
        val steps = defineTestSteps(dialog)

        // call kept to check if issue exists
        XrayClient.getIssue(testKey)

        // remove all existing test steps
        val stepList = XrayClient.getTestSteps(testKey)
        stepList.forEach { currentStep ->
            XrayClient.deleteStep(testKey, currentStep.id)
        }

        steps?.forEach {
            XrayClient.saveStep(testKey, it)
        }

        return XrayTest(testKey)
    }

    private fun defineTestSteps(dialog: DialogReport): List<XrayBuildTestStep>? {
        if (dialog.actions.isEmpty()) {
            logger.warn { "no action for dialog $dialog" }
            return null
        }
        val steps: MutableList<XrayBuildTestStep> = mutableListOf()
        dialog.actions.forEach { action ->
            val user = action.playerId.type == user
            val m = if (!action.message.isEmptyMessage()) action.message else error("Cannot save empty client message.")
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
        return steps
    }

    private fun Message.isEmptyMessage() =
        this == Sentence(text = null, messages = mutableListOf(), userInterface = null, delay = 0)
}
