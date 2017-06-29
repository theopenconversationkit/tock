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
import fr.vsct.tock.bot.admin.dialog.DialogReport
import fr.vsct.tock.bot.admin.dialog.DialogReportDAO
import fr.vsct.tock.bot.connector.rest.client.ConnectorRestClient
import fr.vsct.tock.bot.connector.rest.client.model.ClientMessage
import fr.vsct.tock.bot.connector.rest.client.model.ClientMessageRequest
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.Dice
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import mu.KotlinLogging
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
    private val executor: Executor by injector.instance()

    fun getPlanExecutions(plan: TestPlan): List<TestPlanExecution> {
        return testPlanDAO.getPlanExecutions(plan._id!!)
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

    fun removeDialogFromTestPlan(plan: TestPlan, dialogId: String) {
        saveTestPlan(plan.copy(dialogs = plan.dialogs.filter { it.id != dialogId }))
    }

    fun addDialogToTestPlan(plan: TestPlan, dialogId: String) {
        saveTestPlan(plan.copy(dialogs = plan.dialogs + dialogDAO.getDialog(dialogId)!!))
    }

    fun removeTestPlan(plan: TestPlan) {
        testPlanDAO.removeTestPlan(plan._id!!)
    }

    fun saveTestPlan(plan: TestPlan) {
        testPlanDAO.save(plan)
    }

    fun getTestPlan(planId: String): TestPlan? {
        return testPlanDAO.getPlan(planId)
    }

    fun saveAndRunTestPlan(client: ConnectorRestClient, plan: TestPlan): TestPlanExecution {
        testPlanDAO.save(plan)
        return runTestPlan(client, plan)
    }

    fun runTestPlan(client: ConnectorRestClient, plan: TestPlan): TestPlanExecution {
        val start = Instant.now()
        val dialogs: MutableList<DialogExecutionReport> = mutableListOf()
        var nbErrors: Int = 0
        plan.dialogs.forEach {
            runDialog(client, plan, it).run {
                dialogs.add(this)
                if (error) {
                    nbErrors++
                }
            }
        }
        val exec = TestPlanExecution(
                plan._id!!,
                dialogs,
                nbErrors,
                duration = Duration.between(start, Instant.now())
        )
        testPlanDAO.save(exec)
        return exec
    }


    private fun runDialog(
            client: ConnectorRestClient,
            testPlan: TestPlan,
            dialog: DialogReport): DialogExecutionReport {
        val playerId = Dice.newId()
        val botId = Dice.newId()
        return try {
            var expectedBotMessages: MutableList<ClientMessage> = mutableListOf()
            //send first action if specified
            if (testPlan.startAction != null) {
                client.talk(testPlan.applicationId,
                        ClientMessageRequest(
                                playerId,
                                botId,
                                testPlan.startAction!!.toClientMessage()
                        ))
            }

            dialog.actions.forEach {
                if (it.playerId.type == PlayerType.user) {
                    val answer = client.talk(testPlan.applicationId,
                            ClientMessageRequest(
                                    playerId,
                                    botId,
                                    it.message.toClientMessage()
                            ))
                    expectedBotMessages = answer.body()?.messages?.toMutableList() ?: mutableListOf()
                } else {
                    val expectedMessage = expectedBotMessages.removeAt(0)
                    if (expectedMessage != it.message.toClientMessage()) {
                        return DialogExecutionReport(
                                dialog.id,
                                true,
                                it.id,
                                expectedMessage.toMessage())
                    }
                }
            }

            DialogExecutionReport(dialog.id)
        } catch(e: Exception) {
            logger.error(e)
            DialogExecutionReport(dialog.id, true, errorMessage = e.message)
        } finally {
            userTimelineDAO.remove(PlayerId(playerId, PlayerType.user))
        }
    }

}