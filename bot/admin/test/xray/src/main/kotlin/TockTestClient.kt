/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.admin.test.xray

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.test.TestClientService
import fr.vsct.tock.bot.admin.test.TestPlan
import fr.vsct.tock.bot.admin.test.TestPlanExecution
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.basicAuthInterceptor
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger

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
        tock = retrofitBuilderWithTimeoutAndLogger(
                tockTimeoutInSeconds,
                interceptors = listOf(basicAuthInterceptor(tockLogin, tockPassword)))
                .addJacksonConverter()
                .baseUrl(tockUrl)
                .build()
                .create()
    }

    override fun saveAndExecuteTestPlan(testPlan: TestPlan): TestPlanExecution {
        return tock.executeTestPlan(testPlan).execute().body() ?: TestPlanExecution(testPlan._id, emptyList(), 1)
    }

    override fun executeTestPlan(testPlan: TestPlan): TestPlanExecution {
        return tock.executeTestPlan(testPlan).execute().body() ?: TestPlanExecution(testPlan._id, emptyList(), 1)
    }

    override fun getBotConfigurations(namespace: String, botId: String): List<BotApplicationConfiguration> {
        return tock.getBotConfigurations(botId).execute().body() ?: error("not a bot configuration")
    }

    override fun priority(): Int = 0
}

internal class TockRestClientService : TestClientService by TockTestClient