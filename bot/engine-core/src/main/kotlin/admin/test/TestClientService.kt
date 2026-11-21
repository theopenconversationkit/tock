/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.shared.Loader
import org.litote.kmongo.Id

/**
 * Retrieve a test client from [ServiceLoader].
 */
fun findTestClient(): TestClientService = Loader.loadServices<TestClientService>().maxByOrNull { it.priority() } ?: error("no test client found")

/**
 * A tock client, used to get info from Tock in a potential test context.
 */
interface TestClientService {
    fun saveAndExecuteTestPlan(
        testPlan: TestPlan,
        executionId: Id<TestPlanExecution>,
    ): TestPlanExecution

    fun getBotConfigurations(
        namespace: String,
        botId: String,
    ): List<BotApplicationConfiguration>

    fun priority(): Int
}
