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

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.array
import com.beust.klaxon.obj
import fr.vsct.tock.bot.admin.test.xray.model.JiraAttachment
import fr.vsct.tock.bot.admin.test.xray.model.JiraIssue
import fr.vsct.tock.bot.admin.test.xray.model.JiraIssueLink
import fr.vsct.tock.bot.admin.test.xray.model.JiraKey
import fr.vsct.tock.bot.admin.test.xray.model.JiraTest
import fr.vsct.tock.bot.admin.test.xray.model.JiraType
import fr.vsct.tock.bot.admin.test.xray.model.XrayAttachment
import fr.vsct.tock.bot.admin.test.xray.model.XrayBuildTestStep
import fr.vsct.tock.bot.admin.test.xray.model.XrayTest
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestExecution
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestStep
import fr.vsct.tock.bot.admin.test.xray.model.XrayUpdateTest
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.basicAuthInterceptor
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response

/**
 *
 */
object XrayClient {

    private val logger = KotlinLogging.logger {}

    private val xrayTimeoutInSeconds = longProperty("tock_bot_test_xray_timeout_in_ms", 60000L)
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

    fun createTest(test: JiraTest): JiraIssue
            = xray.createTest(test).execute().body() ?: error("error during creating test $test")

    fun saveStep(testKey: String, step: XrayBuildTestStep)
            = xray.saveStep(testKey, step).execute().body()

    fun addPrecondition(preConditionKey: String, jiraId: String) =
            xray.addPrecondition(
                    preConditionKey,
                    XrayUpdateTest(listOf(jiraId))
            ).execute().body()

    fun updateTest(jiraId: String, test: JiraTest)
            = xray.updateTest(jiraId, test).execute().body()

    fun uploadAttachment(issueId: String, name: String, content: String): JiraAttachment
            =
            xray.addAttachment(
                    issueId,
                    MultipartBody.Part.createFormData(
                            "file",
                            name,
                            RequestBody.create(MediaType.parse("text/plain"), content)))
                    .execute()
                    .body()?.firstOrNull() ?: error("error during attachment of $content")

    fun linkTest(key1: String, key2: String) {
        xray.linkIssue(JiraIssueLink(JiraType("Associate"), JiraKey(key1), JiraKey(key2))).execute()
    }

    fun getLabels(key: String): List<String> {
        val response = xray.getIssue(key).execute()
        val body = Parser().parse(StringBuilder(response.body()!!.string())) as JsonObject
        return body.obj("fields")?.array<String>("labels") ?: emptyList()
    }

    fun addTestToTestPlan(test: String, testPlan: String)
            = xray.addTestToTestPlans(testPlan, XrayUpdateTest(listOf(test))).execute()
}