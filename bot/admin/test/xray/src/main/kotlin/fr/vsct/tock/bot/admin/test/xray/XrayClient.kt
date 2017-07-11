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

import fr.vsct.tock.bot.admin.test.xray.model.XrayAttachment
import fr.vsct.tock.bot.admin.test.xray.model.XrayTest
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestExecution
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestStep
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.basicAuthInterceptor
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import okhttp3.ResponseBody
import retrofit2.Response

/**
 *
 */
object XrayClient {

    private val xrayTimeoutInSeconds = longProperty("tock_bot_test_xray_timeout_in_ms", 30000L)
    private val xrayLogin = property("tock_bot_test_xray_login", "please set xray login")
    private val xrayPassword = property("tock_bot_test_xray_password", "please set xray password")
    private val xrayUrl = property("tock_bot_test_xray_url", "please set xray url")

    val xray: XrayApi

    init {
        xray = retrofitBuilderWithTimeoutAndLogger(
                xrayTimeoutInSeconds,
                interceptors = listOf(basicAuthInterceptor(xrayLogin, xrayPassword)))
                .addJacksonConverter()
                .baseUrl(xrayUrl)
                .build()
                .create()
    }

    fun getTestPlanTests(testPlanKey: String): List<XrayTest> {
        val tests = xray.getTestsOfTestPlan(testPlanKey).execute().body() ?: error("no test in $testPlanKey")
        return xray.getTests(tests.joinToString(";") { it.key })
                .execute()
                .body()
                ?: error("unable to get tests for $tests")
    }

    fun getTestSteps(testKey: String): List<XrayTestStep>
            = xray.getTestSteps(testKey).execute().body() ?: error("no test steps for $testKey")

    fun sendTestExecution(execution: XrayTestExecution): Response<ResponseBody>
            = xray.sendTestExecution(execution).execute()

    fun getAttachmentToString(attachment: XrayAttachment): String
            = xray.getAttachment(attachment.id, attachment.fileName).execute().body()?.string() ?: "error : empty jira attachment"
}