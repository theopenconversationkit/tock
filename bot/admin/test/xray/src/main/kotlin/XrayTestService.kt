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

package ai.tock.bot.admin.test.xray

import ai.tock.bot.admin.test.TestCoreService
import ai.tock.bot.admin.test.TestService
import ai.tock.bot.xray.XrayPlanExecutionConfiguration
import ai.tock.bot.xray.XrayService
import ai.tock.nlp.admin.AdminVerticle
import ai.tock.shared.security.TockUserRole.botUser

private val testCoreService = TestCoreService()

class XrayTestService : TestService by testCoreService {
    override fun registerServices(): (AdminVerticle).() -> Unit =
        {
            testCoreService.registerServices().invoke(this)
            /**
             * Triggered on "Create" button, after providing connector and test plan key.
             * Will reach Jira to gather all test steps and send them to the bot as a conversation
             */
            blockingJsonPost("/xray/execute", botUser) { context, configuration: XrayPlanExecutionConfiguration ->
                XrayService(
                    listOfNotNull(configuration.configurationId),
                    listOfNotNull(configuration.testPlanKey),
                    listOfNotNull(configuration.testKey),
                    configuration.testedBotId,
                ).execute(context.organization)
            }

            blockingJsonGet("/xray/test/plans", botUser) {
                XrayService().getTestPlans()
            }
        }

    override fun priority(): Int = 1
}
