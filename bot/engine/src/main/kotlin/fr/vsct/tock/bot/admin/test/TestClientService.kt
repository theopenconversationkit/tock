package fr.vsct.tock.bot.admin.test

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.shared.Loader

/**
 * Retrieve a test client from [ServiceLoader].
 */
fun findTestClient(): TestClientService =
        Loader.loadServices<TestClientService>().maxBy { it.priority() } ?: error("no test client found")


/**
 * A tock client, used to get info from Tock in a potential test context.
 */
interface TestClientService {

    fun saveAndExecuteTestPlan(testPlan: TestPlan): TestPlanExecution

    fun executeTestPlan(testPlan: TestPlan): TestPlanExecution

    fun getBotConfigurations(namespace:String, botId: String): List<BotApplicationConfiguration>

    fun priority(): Int
}