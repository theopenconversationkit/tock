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
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.rest.client.ConnectorRestClient
import fr.vsct.tock.bot.connector.rest.client.model.ClientAttachment
import fr.vsct.tock.bot.connector.rest.client.model.ClientAttachmentType
import fr.vsct.tock.bot.connector.rest.client.model.ClientChoice
import fr.vsct.tock.bot.connector.rest.client.model.ClientConnectorType
import fr.vsct.tock.bot.connector.rest.client.model.ClientLocation
import fr.vsct.tock.bot.connector.rest.client.model.ClientMessage
import fr.vsct.tock.bot.connector.rest.client.model.ClientMessageRequest
import fr.vsct.tock.bot.connector.rest.client.model.ClientSentence
import fr.vsct.tock.bot.connector.rest.client.model.ClientSentenceElement
import fr.vsct.tock.bot.connector.rest.client.model.ClientSentenceSubElement
import fr.vsct.tock.bot.connector.rest.client.model.ClientUserInterfaceType
import fr.vsct.tock.bot.connector.rest.client.model.ClientUserLocation
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.message.Attachment
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.Location
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.bot.engine.message.Sentence
import fr.vsct.tock.bot.engine.message.SentenceElement
import fr.vsct.tock.bot.engine.message.SentenceSubElement
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserLocation
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.Dice
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.translator.UserInterfaceType
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

    private fun SendAttachment.AttachmentType.toClientAttachmentType(): ClientAttachmentType {
        return ClientAttachmentType.valueOf(name)
    }

    private fun ClientAttachmentType.toAttachmentType(): SendAttachment.AttachmentType {
        return SendAttachment.AttachmentType.valueOf(name)
    }

    private fun Location.toClientLocation(): ClientLocation {
        return ClientLocation(location?.let { ClientUserLocation(it.lat, it.lng) })
    }

    private fun ClientLocation.toLocation(): Location {
        return Location(location?.let { UserLocation(it.lat, it.lng) })
    }

    private fun ConnectorType.toClientConnectorType(): ClientConnectorType {
        return ClientConnectorType(id, userInterfaceType.toClientUserInterfaceType(), asynchronous)
    }

    private fun ClientConnectorType.toConnectorType(): ConnectorType {
        return ConnectorType(id, userInterfaceType.toUserInterfaceType(), asynchronous)
    }

    private fun UserInterfaceType.toClientUserInterfaceType(): ClientUserInterfaceType {
        return ClientUserInterfaceType.valueOf(name)
    }

    private fun ClientUserInterfaceType.toUserInterfaceType(): UserInterfaceType {
        return UserInterfaceType.valueOf(name)
    }

    internal fun Message.toClientMessage(): ClientMessage {
        return when (this) {
            is Sentence -> ClientSentence(text, messages.map { it.toClientSentenceElement() }.toMutableList())
            is Choice -> ClientChoice(intentName, parameters)
            is Attachment -> ClientAttachment(url, type.toClientAttachmentType())
            is Location -> toClientLocation()
            else -> error("unsupported message $this")
        }
    }

    internal fun ClientMessage.toMessage(): Message {
        return when (this) {
            is ClientSentence -> Sentence(text, messages.map { it.toSentenceElement() }.toMutableList())
            is ClientChoice -> Choice(intentName, parameters)
            is ClientAttachment -> Attachment(url, type.toAttachmentType())
            is ClientLocation -> toLocation()
            else -> error("unsupported message $this")
        }
    }

    private fun SentenceElement.toClientSentenceElement(): ClientSentenceElement {
        return ClientSentenceElement(
                connectorType.toClientConnectorType(),
                attachments.map { it.toClientMessage() as ClientAttachment },
                choices.map { it.toClientMessage() as ClientChoice },
                texts,
                locations.map { it.toClientLocation() },
                metadata,
                subElements.map { it.toClientSentenceSubElement() }
        )
    }

    private fun ClientSentenceElement.toSentenceElement(): SentenceElement {
        return SentenceElement(
                connectorType.toConnectorType(),
                attachments.map { it.toMessage() as Attachment },
                choices.map { it.toMessage() as Choice },
                texts,
                locations.map { it.toLocation() },
                metadata,
                subElements.map { it.toSentenceSubElement() }
        )
    }

    private fun SentenceSubElement.toClientSentenceSubElement(): ClientSentenceSubElement {
        return ClientSentenceSubElement(
                attachments.map { it.toClientMessage() as ClientAttachment },
                choices.map { it.toClientMessage() as ClientChoice },
                texts,
                locations.map { it.toClientLocation() },
                metadata
        )
    }

    private fun ClientSentenceSubElement.toSentenceSubElement(): SentenceSubElement {
        return SentenceSubElement(
                attachments.map { it.toMessage() as Attachment },
                choices.map { it.toMessage() as Choice },
                texts,
                locations.map { it.toLocation() },
                metadata
        )
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
                    expectedBotMessages = answer.body().messages.toMutableList()
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