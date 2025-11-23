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

package ai.tock.bot.admin.test

import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.connector.rest.client.ConnectorRestClient
import ai.tock.bot.connector.rest.client.model.ClientChoice
import ai.tock.bot.connector.rest.client.model.ClientGenericElement
import ai.tock.bot.connector.rest.client.model.ClientGenericMessage
import ai.tock.bot.connector.rest.client.model.ClientMessage
import ai.tock.bot.connector.rest.client.model.ClientMessageRequest
import ai.tock.bot.connector.rest.client.model.ClientSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.shared.Dice
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.provide
import com.github.salomonbrys.kodein.instance
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.litote.kmongo.Id
import java.time.Duration
import java.time.Instant

/**
 *
 */
object TestPlanService {
    private val logger = KotlinLogging.logger {}

    private val testPlanDAO: TestPlanDAO by injector.instance()
    private val userTimelineDAO: UserTimelineDAO by injector.instance()
    private val dialogDAO: DialogReportDAO by injector.instance()
    private val botConfigurationDAO: BotApplicationConfigurationDAO get() = injector.provide()

    private val applicationIdPathCache: Cache<String, String> =
        CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(1)).build()

    fun getPlanExecutions(plan: TestPlan): List<TestPlanExecution> {
        return testPlanDAO.getPlanExecutions(plan._id)
    }

    fun getTestPlansByNamespaceAndNlpModel(
        namespace: String,
        nlpModel: String,
    ): List<TestPlan> {
        return testPlanDAO.getTestPlans().filter { it.namespace == namespace && it.nlpModel == nlpModel }
    }

    fun getTestPlansByNamespace(namespace: String): List<TestPlan> {
        return testPlanDAO.getTestPlans().filter { it.namespace == namespace }
    }

    fun getTestPlansByApplication(applicationId: String): List<TestPlan> {
        return testPlanDAO.getPlansByApplicationId(applicationId)
    }

    fun removeDialogFromTestPlan(
        plan: TestPlan,
        dialogId: Id<Dialog>,
    ) {
        saveTestPlan(plan.copy(dialogs = plan.dialogs.filter { it.id != dialogId }))
    }

    fun addDialogToTestPlan(
        plan: TestPlan,
        dialogId: Id<Dialog>,
    ) {
        saveTestPlan(plan.copy(dialogs = plan.dialogs + TestDialogReport(dialogDAO.getDialog(dialogId)!!)))
    }

    fun removeTestPlan(plan: TestPlan) {
        testPlanDAO.removeTestPlan(plan._id)
    }

    fun saveTestPlan(plan: TestPlan) {
        testPlanDAO.saveTestPlan(plan)
    }

    fun getTestPlan(planId: Id<TestPlan>): TestPlan? {
        return testPlanDAO.getTestPlan(planId)
    }

    fun getTestPlanExecution(
        testPlan: TestPlan,
        testExecutionId: Id<TestPlanExecution>,
    ): TestPlanExecution? {
        return testPlanDAO.getTestPlanExecution(testPlan, testExecutionId)
    }

    fun saveTestPlanExecution(testPlanExecution: TestPlanExecution) {
        testPlanDAO.saveTestExecution(testPlanExecution)
    }

    /**
     * This function saves the given test plan in the mongo database and then run the test plan.
     *
     * @param client is the client to use for running the test plan.
     * @param plan is the common test plan to run.
     * @return the results of the test plan execution as a TestPlanExecution object.
     */
    fun saveAndRunTestPlan(
        client: ConnectorRestClient,
        plan: TestPlan,
        executionId: Id<TestPlanExecution>,
    ): TestPlanExecution {
        testPlanDAO.saveTestPlan(plan)
        return runTestPlan(client, plan, executionId)
    }

    /**
     * This function execute the given common test plan.
     * It goes over all steps of related tests and sends each step as a dialog.
     *
     * @param client is the client to use for the dialogs.
     * @param plan is the common test plan to run.
     * @return the results of the test plan execution as a TestPlanExecution object.
     */
    fun runTestPlan(
        client: ConnectorRestClient,
        plan: TestPlan,
        executionId: Id<TestPlanExecution>,
    ): TestPlanExecution {
        val start = Instant.now()
        val dialogs: MutableList<DialogExecutionReport> = mutableListOf()
        var nbErrors = 0

        // store the test plan execution into the right Object
        val exec =
            TestPlanExecution(
                plan._id,
                dialogs,
                nbErrors,
                duration = Duration.between(start, Instant.now()),
                _id = executionId,
                status = TestPlanExecutionStatus.PENDING,
            )
        // save the test plan execution into the database
        testPlanDAO.saveTestExecution(exec)

        // run all the steps as dialog, one by one
        plan.dialogs.forEach {
            runDialog(client, plan, it).run {
                dialogs.add(this)
                if (error) {
                    nbErrors++
                }
            }
        }

        val finalExecution = exec.copy(nbErrors = nbErrors, status = TestPlanExecutionStatus.COMPLETE)
        // update the test plan execution into the database
        testPlanDAO.saveTestExecution(finalExecution)
        // return the completed test execution
        return finalExecution
    }

    /**
     * This function starts the dialog with the right bot.
     * Dialogs are built using tests steps and they are sent to the bot.
     *
     * @param client is the bot to dialog with.
     * @param testPlan is the common test plan to run.
     * @param dialog is the dialog to send to the bot. It is an object TestDialogReport which is composed by :
     *                          val actions: List<TestActionReport> = emptyList()
     *                          val userInterface: UserInterfaceType
     *                          val id: Id<Dialog>
     * @return the result of the dialog as a DialogExecutionReport object.
     */
    private fun runDialog(
        client: ConnectorRestClient,
        testPlan: TestPlan,
        dialog: TestDialogReport,
    ): DialogExecutionReport {
        val playerId = Dice.newId()
        val botId = Dice.newId()
        return try {
            var botMessages: MutableList<ClientMessage> = mutableListOf()
            // send first action if specified
            // first action is just saying Hi!
            if (testPlan.startAction != null) {
                client.talk(
                    getPath(testPlan),
                    testPlan.locale,
                    ClientMessageRequest(
                        playerId,
                        botId,
                        testPlan.startAction!!.toClientMessage(),
                        testPlan.targetConnectorType.toClientConnectorType(),
                        true,
                    ),
                )
            }

            dialog.actions.forEachIndexed { testStepIndex, testStepMessages ->
                if (testStepMessages.playerId.type == PlayerType.user) {
                    // convert the current test step as a request formatted to be understandable by the bot
                    val request =
                        ClientMessageRequest(
                            playerId,
                            botId,
                            testStepMessages.findFirstMessage().toClientMessage(),
                            testPlan.targetConnectorType.toClientConnectorType(),
                            true,
                        )
                    logger.debug { "ASK -- : $request" }
                    // send the converted test step to the bot
                    val answer = client.talk(getPath(testPlan), testPlan.locale, request)
                    // if the bot answers then store the response body, otherwise it is an error
                    if (answer.isSuccessful) {
                        val body = answer.body()
                        logger.debug { "ANSWER -- : $body" }
                        // go over the bot answer to remove emoticons
                        botMessages = body?.messages?.map { it as ClientSentence }
                            ?.map { it.copy(text = it.text?.cleanSurrogateChars()) }?.toMutableList() ?: mutableListOf()
                        logger.debug { "ANSWER without surrogate -- : $botMessages" }
                    } else {
                        logger.error { "ERROR : " + answer.errorBody()?.string() }
                        return DialogExecutionReport(
                            dialog.id,
                            true,
                            errorMessage =
                                answer.errorBody()?.toString()
                                    ?: "Unknown error",
                        )
                    }
                } else {
                    if (botMessages.isEmpty()) {
                        return DialogExecutionReport(
                            dialog.id,
                            true,
                            testStepMessages.id,
                            errorMessage = "(no answer but one expected)",
                        )
                    }
                    val botMessage = botMessages.removeAt(0)
                    // if the bot's answer does not equal to the test step
                    val errorMessage = botMessage.convertAndEquals(testStepMessages)
                    if (null != errorMessage) {
                        val givenAnswer = botMessage.toMessage().toPrettyString()
                        val expectedAnswer =
                            testStepMessages.messages.joinToString(" - ") { message -> message.toPrettyString() }
                        return DialogExecutionReport(
                            dialogReportId = dialog.id,
                            error = true,
                            errorActionId = testStepMessages.id,
                            errorMessage = "Error : $errorMessage - during steps comparison : $givenAnswer / expected : $expectedAnswer",
                            indexOfStepError = testStepIndex,
                        )
                    }
                }
            }
            DialogExecutionReport(dialog.id)
        } catch (e: Exception) {
            logger.error(e)
            DialogExecutionReport(dialog.id, true, errorMessage = e.message)
        } finally {
            runBlocking {
                userTimelineDAO.remove(testPlan.namespace, PlayerId(playerId, PlayerType.user))
            }
        }
    }

    private fun getPath(testPlan: TestPlan): String {
        val applicationId = testPlan.applicationId
        return applicationIdPathCache.get(applicationId) {
            botConfigurationDAO.getConfigurationById(testPlan.botApplicationConfigurationId)?.path
                ?: "/$applicationId"
        }
    }

    /**
     * This function checks if at least, one message returned by the bot, equals to the message of the current test step.
     * Test step is first converted into a bot client message before starting the comparison using another function.
     * This conversion is essential to compare the same objects.
     *
     * @param step is the xray step formatted as a TestActionReport which contains the message sent to the bot.
     * @return true if the messages are equals, false otherwise.
     */
    private fun ClientMessage.convertAndEquals(step: TestActionReport): String? {
        val result =
            step.messages.map {
                this.checkEquality(it.toClientMessage())
            }

        return if (result.any { it == null }) {
            null
        } else {
            result.joinToString("\t")
        }
    }

    /**
     * This function checks if the answer sent by the bot equals the expected expectedMessage stored in the test step.
     *
     * @param expectedMessage is the message to expect as an answer from the bot.
     * @return true if messages are the same, false otherwise.
     */
    fun ClientMessage.checkEquality(expectedMessage: ClientMessage): String? {
        if (expectedMessage !is ClientSentence || this !is ClientSentence) {
            return if (expectedMessage == this) null else "Messages differs : \"$this\" / expected \"$expectedMessage\""
        }

        if (text?.cleanSurrogateChars()?.trim() != expectedMessage.text?.cleanSurrogateChars()?.trim()) {
            return "Text differs : \"${this.text}\" / expected \"${expectedMessage.text}\""
        }

        return messages.zip(expectedMessage.messages)
            .mapNotNull { (subMessage, expectedSubMessage) -> subMessage.partiallyEquals(expectedSubMessage) }
            .firstOrNull()
    }
}

private fun ClientGenericMessage.partiallyEquals(expected: ClientGenericMessage): String? {
    if (texts.cleanTexts() != expected.texts.cleanTexts()) {
        return "Message text differs : \"$texts\" / expected \"${expected.texts}\""
    }

    choices.zip(expected.choices)
        .mapNotNull { (obtainedChoice, expectedChoice) -> obtainedChoice.partiallyEquals(expectedChoice) }
        .firstOrNull()?.let { return it }

    subElements.zip(expected.subElements)
        .mapNotNull { (obtainedSubElement, expectedSubElement) -> obtainedSubElement.partiallyEquals(expectedSubElement) }
        .firstOrNull()?.let { return it }

    return null
}

private fun ClientGenericElement.partiallyEquals(expected: ClientGenericElement): String? {
    if (texts.cleanTexts() != expected.texts.cleanTexts()) {
        return "Sub-element text differs : \"$texts\" / expected \"${expected.texts}\""
    }

    return choices.zip(expected.choices)
        .mapNotNull { (obtainedChoice, expectedChoice) -> obtainedChoice.partiallyEquals(expectedChoice) }
        .firstOrNull()
}

private fun ClientChoice.partiallyEquals(expected: ClientChoice): String? {
    val parameter = "_title"
    return if (parameters[parameter] != expected.parameters[parameter]) {
        "Choices differs ${this.parameters[parameter]} / ${expected.parameters[parameter]}"
    } else {
        null
    }
}

private fun Map<String, String>.cleanTexts() = mapValues { (_, v) -> v.cleanSurrogateChars().replace(Regex("<[^>]*>"), "") }

private fun String.cleanSurrogateChars(): String = filterNot { it.isSurrogate() }
