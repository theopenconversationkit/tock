package fr.vsct.tock.bot.admin

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.test.TestClientService
import fr.vsct.tock.bot.admin.test.TestPlan
import fr.vsct.tock.bot.admin.test.TestPlanExecution
import org.litote.kmongo.Id

internal class AdminTestClient : TestClientService {

    override fun saveAndExecuteTestPlan(testPlan: TestPlan, executionId: Id<TestPlanExecution>): TestPlanExecution =
            BotAdminService.saveAndExecuteTestPlan(testPlan.namespace, testPlan, executionId)

    override fun getBotConfigurations(namespace: String, botId: String): List<BotApplicationConfiguration> =
            BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, botId)

    override fun priority(): Int = 1
}