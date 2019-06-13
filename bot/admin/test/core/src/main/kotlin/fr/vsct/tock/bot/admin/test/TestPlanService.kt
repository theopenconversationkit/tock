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

package fr.vsct.tock.bot.admin.test

import com.github.salomonbrys.kodein.instance
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.admin.dialog.DialogReportDAO
import fr.vsct.tock.bot.connector.rest.client.ConnectorRestClient
import fr.vsct.tock.bot.connector.rest.client.model.ClientConnectorType
import fr.vsct.tock.bot.connector.rest.client.model.ClientMessage
import fr.vsct.tock.bot.connector.rest.client.model.ClientMessageRequest
import fr.vsct.tock.bot.connector.rest.client.model.ClientSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.Dice
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.provide
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
        return testPlanDAO.getPlans().filter { it.namespace == namespace && it.nlpModel == nlpModel }
    }

    fun getTestPlansByNamespace(namespace: String): List<TestPlan> {
        return testPlanDAO.getPlans().filter { it.namespace == namespace }
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
        return testPlanDAO.getPlan(planId)
    }

    /**
     * This function saves the given test plan in the mongo database and then run the test plan.
     *
     * @param client is the client to use for running the test plan.
     * @param plan is the common test plan to run.
     * @return the results of the test plan execution as a TestPlanExecution object.
     */
    fun saveAndRunTestPlan(client: ConnectorRestClient, plan: TestPlan): TestPlanExecution {
        testPlanDAO.saveTestPlan(plan)
        return runTestPlan(client, plan)
    }

    /**
     * This function execute the given common test plan.
     * It goes over all steps of related tests and sends each step as a dialog.
     *
     * @param client is the client to use for the dialogs.
     * @param plan is the common test plan to run.
     * @return the results of the test plan execution as a TestPlanExecution object.
     */
    fun runTestPlan(client: ConnectorRestClient, plan: TestPlan): TestPlanExecution {
        val start = Instant.now()
        val dialogs: MutableList<DialogExecutionReport> = mutableListOf()
        var nbErrors: Int = 0
        // run all the steps as dialog, one by one
        plan.dialogs.forEach {
            runDialog(client, plan, it).run {
                dialogs.add(this)
                if (error) {
                    nbErrors++
                }
            }
        }
        // store the test plan execution into the right Object
        val exec = TestPlanExecution(
            plan._id,
            dialogs,
            nbErrors,
            duration = Duration.between(start, Instant.now())
        )
        // save the test plan execution into the database
        testPlanDAO.saveTestExecution(exec)
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
                        botMessages = body?.messages?.toMutableList() ?: mutableListOf()
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
                        return DialogExecutionReport(
                            dialog.id,
                            true,
                            it.id,
                            botMessage.toMessage()
                        )
                    }
                }
            }

            DialogExecutionReport(dialog.id)
        } catch (e: Exception) {
            logger.error(e)
            DialogExecutionReport(dialog.id, true, errorMessage = e.message)
        } finally {
            userTimelineDAO.remove(PlayerId(playerId, PlayerType.user))
        }
    }

    private fun getPath(testPlan: TestPlan): String {
        val applicationId = testPlan.applicationId
        return applicationIdPathCache.get(applicationId) {
            botConfigurationDAO.getConfigurationById(testPlan.botApplicationConfigurationId)?.path ?: "/$applicationId"
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
//            deepEquals(it.toClientMessage())
            deepEqualsTest(it.toClientMessage(), this)
        }
    }

    /**
     * This function checks if the answer sent by the bot equals the expected expectedMessage stored in the test step.
     *
     * @param expectedMessage is the message to expect as an answer from the bot.
     * @return true if messages are the same, false otherwise.
     *
     *
     * mesage non modifiable donc obligé de paser par copy
     */
    private fun ClientMessage.deepEquals(expectedMessage: ClientMessage): Boolean {
        return if (expectedMessage is ClientSentence && this is ClientSentence) {

            expectedMessage.copy(
                    text = expectedMessage.text?.trim(),
                    messages = expectedMessage.messages.map { it.copy(connectorType = ClientConnectorType.none) }.toMutableList()
            ) == copy(
                    text = text?.trim(),
                    messages = messages.map { it.copy(connectorType = ClientConnectorType.none) }.toMutableList())
        } else {
            expectedMessage == this
        }
    }

    private fun deepEqualsTest(expectedMessage: ClientMessage, botAnswer: ClientMessage): Boolean {
        return expectedMessage is ClientSentence && botAnswer is ClientSentence && (expectedMessage == botAnswer || expectedMessage.text?.trim() == botAnswer.text?.trim())
    }

}