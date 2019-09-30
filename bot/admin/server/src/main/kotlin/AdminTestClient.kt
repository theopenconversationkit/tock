package ai.tock.bot.admin

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.test.TestClientService
import ai.tock.bot.admin.test.TestPlan
import ai.tock.bot.admin.test.TestPlanExecution
import org.litote.kmongo.Id

internal class AdminTestClient : TestClientService {

    override fun saveAndExecuteTestPlan(testPlan: TestPlan, executionId: Id<TestPlanExecution>): TestPlanExecution =
            BotAdminService.saveAndExecuteTestPlan(testPlan.namespace, testPlan, executionId)

    override fun getBotConfigurations(namespace: String, botId: String): List<BotApplicationConfiguration> =
            BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, botId)

    override fun priority(): Int = 1
}