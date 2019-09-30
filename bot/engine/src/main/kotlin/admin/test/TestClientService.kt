package ai.tock.bot.admin.test

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.shared.Loader
import org.litote.kmongo.Id

/**
 * Retrieve a test client from [ServiceLoader].
 */
fun findTestClient(): TestClientService =
        Loader.loadServices<TestClientService>().maxBy { it.priority() } ?: error("no test client found")


/**
 * A tock client, used to get info from Tock in a potential test context.
 */
interface TestClientService {

    fun saveAndExecuteTestPlan(testPlan: TestPlan, executionId: Id<TestPlanExecution>): TestPlanExecution

    fun getBotConfigurations(namespace:String, botId: String): List<BotApplicationConfiguration>

    fun priority(): Int
}