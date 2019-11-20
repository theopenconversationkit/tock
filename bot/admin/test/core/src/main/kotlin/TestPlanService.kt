/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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
import ai.tock.bot.connector.rest.client.model.ClientMessage
import ai.tock.bot.connector.rest.client.model.ClientMessageRequest
import ai.tock.bot.connector.rest.client.model.ClientSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.Sentence
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

    fun getTestPlansByNamespaceAndNlpModel(namespace: String, nlpModel: String): List<TestPlan> {
        return testPlanDAO.getTestPlans().filter { it.namespace == namespace && it.nlpModel == nlpModel }
    }

    fun getTestPlansByNamespace(namespace: String): List<TestPlan> {
        return testPlanDAO.getTestPlans().filter { it.namespace == namespace }
    }

    fun getTestPlansByApplication(applicationId: String): List<TestPlan> {
        return testPlanDAO.getPlansByApplicationId(applicationId)
    }

    fun removeDialogFromTestPlan(plan: TestPlan, dialogId: Id<Dialog>) {
        saveTestPlan(plan.copy(dialogs = plan.dialogs.filter { it.id != dialogId }))
    }

    fun addDialogToTestPlan(plan: TestPlan, dialogId: Id<Dialog>) {
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

    fun getTestPlanExecution(testPlan: TestPlan, testExecutionId: Id<TestPlanExecution>): TestPlanExecution? {
        return testPlanDAO.getTestPlanExecution(testPlan, testExecutionId);
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
    fun saveAndRunTestPlan(client: ConnectorRestClient, plan: TestPlan, executionId: Id<TestPlanExecution>): TestPlanExecution {
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
    fun runTestPlan(client: ConnectorRestClient, plan: TestPlan, executionId: Id<TestPlanExecution>): TestPlanExecution {
        val start = Instant.now()
        val dialogs: MutableList<DialogExecutionReport> = mutableListOf()
        var nbErrors = 0

        // store the test plan execution into the right Object
        val exec = TestPlanExecution(
                plan._id,
                dialogs,
                nbErrors,
                duration = Duration.between(start, Instant.now()),
                _id = executionId,
                status = TestPlanExecutionStatus.PENDING
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

        if (nbErrors != 0) {
            exec.nbErrors = nbErrors
        }

        // update the test plan execution into the database
        testPlanDAO.saveTestExecution(exec.copy(nbErrors = nbErrors, status = TestPlanExecutionStatus.COMPLETE))
        // return the completed test execution
        return exec
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
    private fun runDialog(client: ConnectorRestClient, testPlan: TestPlan, dialog: TestDialogReport): DialogExecutionReport {
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
                                true
                        )
                )
            }

            // run each test step
            // "it" represents here a TestActionReport
            dialog.actions.forEach {
                if (it.playerId.type == PlayerType.user) {
                    // convert the current test step as a request formatted to be understandable by the bot
                    val request = ClientMessageRequest(
                            playerId,
                            botId,
                            it.findFirstMessage().toClientMessage(),
                            testPlan.targetConnectorType.toClientConnectorType(),
                            true
                    )
                    logger.debug { "ASK -- : $request" }
                    // send the converted test step to the bot
                    val answer = client.talk(getPath(testPlan), testPlan.locale, request)
                    // if the bot answers then store the response body, otherwise it is an error
                    if (answer.isSuccessful) {
                        val body = answer.body()
                        logger.debug { "ANSWER -- : $body" }
                        // go over the bot answer to remove emoticons
                        val list = body?.messages?.toMutableList() ?: mutableListOf()
                        list.forEachIndexed { index, message ->
                            var text = (message as ClientSentence).text
                            text?.forEach { c ->
                                // if emoticon is found, remove it
                                if (c.isSurrogate()) {
                                    text = text?.replace("$c", "")?.trim()
                                    list[index] = ClientSentence(text)
                                }
                            }
                        }
                        // then store the bot answer into a proper variable
                        botMessages = list
                        logger.debug { "ANSWER without surrogate -- : $botMessages" }
                    } else {
                        logger.error { "ERROR : " + answer.errorBody()?.string() }
                        return DialogExecutionReport(
                                dialog.id, true, errorMessage = answer.errorBody()?.toString()
                                ?: "Unknown error"
                        )
                    }
                } else {
                    if (botMessages.isEmpty()) {
                        return DialogExecutionReport(
                                dialog.id,
                                true,
                                it.id,
                                errorMessage = "(no answer but one expected)"
                        )
                    }
                    val botMessage = botMessages.removeAt(0)
                    // if the bot's answer does not equal to the test step
                    if (!botMessage.convertAndDeepEquals(it)) {
                        logger.error { "Not the same messages:\n\t\tObtained ----- $botMessage\n\t\tExpected ----- ${it.messages.map { m -> m.toClientMessage() }}" }
                        val givenAnswer = botMessage.toMessage().toPrettyString()
                        val expectedAnswer = it.messages.map { message -> message.toPrettyString() }.joinToString(" - ")
                        return DialogExecutionReport(
                                dialogReportId = dialog.id,
                                error = true,
                                errorActionId = it.id,
                                errorMessage = "Réponse inattendue : \"$givenAnswer\" au lieu de \"$expectedAnswer\" ----- " +
                                        "Mots différents : " + givenAnswer.split(" ").filter { word ->
                                    !expectedAnswer.split(" ").contains(word)
                                }.toString()
                        )
                    }
                }
            }
            DialogExecutionReport(dialog.id)
        } catch (e: Exception) {
            logger.error(e)
            DialogExecutionReport(dialog.id, true, errorMessage = e.message)
        } finally {
            userTimelineDAO.remove(testPlan.namespace, PlayerId(playerId, PlayerType.user))
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
     * @param action is the xray step formatted as a TestActionReport which contains the message sent to the bot.
     * @return true if the messages are equals, false otherwise.
     */
    private fun ClientMessage.convertAndDeepEquals(action: TestActionReport): Boolean {
        return action.messages.any {
            // convert the user message stored in xray to a bot message format
            deepEquals(it.toClientMessage(), this)
        }
    }

    /**
     * This function checks if the answer sent by the bot equals the expected expectedMessage stored in the test step.
     *
     * @param expectedMessage is the message to expect as an answer from the bot.
     * @return true if messages are the same, false otherwise.
     */
    private fun deepEquals(expectedMessage: ClientMessage, botAnswer: ClientMessage): Boolean {
        var botAnswerText = (botAnswer as ClientSentence).text
        botAnswer.text?.forEach { c -> if (c.isSurrogate()) botAnswerText = botAnswerText?.replace("$c", "") }

        return expectedMessage is ClientSentence && botAnswer is ClientSentence && (expectedMessage == botAnswer || expectedMessage.text?.trim() == botAnswerText?.trim())
    }

}