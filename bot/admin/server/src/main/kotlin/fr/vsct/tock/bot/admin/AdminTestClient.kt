package fr.vsct.tock.bot.admin

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.test.TestClientService
import fr.vsct.tock.bot.admin.test.TestPlan
import fr.vsct.tock.bot.admin.test.TestPlanExecution
import fr.vsct.tock.bot.admin.test.TestPlanService

internal class AdminTestClient : TestClientService {

    override fun executeTestPlan(testPlan: TestPlan): TestPlanExecution =
            TestPlanService.runTestPlan(
                    BotAdminService.getRestClient(
                            BotAdminService.getBotConfiguration(
                                    testPlan.botApplicationConfigurationId,
                                    testPlan.namespace
                            )
                    ),
                    testPlan
            )

    override fun getBotConfigurations(namespace: String, botId: String): List<BotApplicationConfiguration> =
            BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, botId)

    override fun priority(): Int = 1
}