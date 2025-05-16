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

package ai.tock.bot.admin

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.test.TestClientService
import ai.tock.bot.admin.test.TestPlan
import ai.tock.bot.admin.test.TestPlanExecution
import ai.tock.bot.admin.test.findTestService
import org.litote.kmongo.Id

internal class AdminTestClient : TestClientService {

    override fun saveAndExecuteTestPlan(testPlan: TestPlan, executionId: Id<TestPlanExecution>): TestPlanExecution =
        findTestService().saveAndExecuteTestPlan(testPlan.namespace, testPlan, executionId)

    override fun getBotConfigurations(namespace: String, botId: String): List<BotApplicationConfiguration> =
        BotAdminService.getBotConfigurationsByNamespaceAndBotId(namespace, botId)

    override fun priority(): Int = 1
}
