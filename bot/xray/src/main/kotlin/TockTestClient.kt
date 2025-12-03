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

package ai.tock.bot.xray

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.test.TestClientService
import ai.tock.bot.admin.test.TestPlan
import ai.tock.bot.admin.test.TestPlanExecution
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.basicAuthInterceptor
import ai.tock.shared.create
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import org.litote.kmongo.Id

/**
 *
 */
internal object TockTestClient : TestClientService {
    private val tockTimeoutInSeconds = longProperty("tock_bot_test_timeout_in_ms", 60 * 60000L)
    private val tockLogin = property("tock_bot_test_login", "please set tock test login")
    private val tockPassword = property("tock_bot_test_password", "please set tock test password")
    private val tockUrl = property("tock_bot_test_url", "http://set_property_tock_bot_test_url")

    val tock: TockTestApi

    init {
        tock =
            retrofitBuilderWithTimeoutAndLogger(
                tockTimeoutInSeconds,
                interceptors = listOf(basicAuthInterceptor(tockLogin, tockPassword)),
            )
                .addJacksonConverter()
                .baseUrl(tockUrl)
                .build()
                .create()
    }

    override fun saveAndExecuteTestPlan(
        testPlan: TestPlan,
        executionId: Id<TestPlanExecution>,
    ): TestPlanExecution {
        return tock.executeTestPlan(testPlan).execute().body() ?: TestPlanExecution(testPlan._id, emptyList(), 1)
    }

    override fun getBotConfigurations(
        namespace: String,
        botId: String,
    ): List<BotApplicationConfiguration> {
        return tock.getBotConfigurations(botId).execute().body() ?: error("not a bot configuration")
    }

    override fun priority(): Int = 0
}

internal class TockRestClientService : TestClientService by TockTestClient
