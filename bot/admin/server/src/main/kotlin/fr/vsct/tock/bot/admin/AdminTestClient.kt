package fr.vsct.tock.bot.admin

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.test.TestClientService
import fr.vsct.tock.bot.admin.test.TestPlan
import fr.vsct.tock.bot.admin.test.TestPlanExecution

internal class AdminTestClient : TestClientService {

    override fun saveAndExecuteTestPlan(testPlan: TestPlan): TestPlanExecution =
            BotAdminService.saveAndExecuteTestPlan(testPlan.namespace, testPlan)

    override fun executeTestPlan(testPlan: TestPlan): TestPlanExecution =
            BotAdminService.executeTestPlan(testPlan.namespace, testPlan)

    override fun getBotConfigurations(namespace: String, botId: String): List<BotApplicationConfiguration> =
            BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, botId)

    override fun priority(): Int = 1
}