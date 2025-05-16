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

import ai.tock.bot.xray.XrayConfiguration.xrayUrl
import ai.tock.bot.xray.model.JiraAttachment
import ai.tock.bot.xray.model.JiraIssue
import ai.tock.bot.xray.model.JiraIssueLink
import ai.tock.bot.xray.model.JiraKey
import ai.tock.bot.xray.model.JiraTest
import ai.tock.bot.xray.model.JiraTestProject
import ai.tock.bot.xray.model.JiraType
import ai.tock.bot.xray.model.XrayAttachment
import ai.tock.bot.xray.model.XrayBuildTestStep
import ai.tock.bot.xray.model.XrayTest
import ai.tock.bot.xray.model.XrayTestExecution
import ai.tock.bot.xray.model.XrayTestExecutionCreation
import ai.tock.bot.xray.model.XrayTestPlan
import ai.tock.bot.xray.model.XrayTestStep
import ai.tock.bot.xray.model.XrayUpdateTest
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.basicAuthInterceptor
import ai.tock.shared.create
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.StringReader

/**
 *
 */
object XrayClient {

    private val logger = KotlinLogging.logger {}

    private val xrayTimeoutInSeconds = longProperty("tock_bot_test_xray_timeout_in_ms", 60000L)
    private val xrayLogin = property("tock_bot_test_xray_login", "please set xray login")
    private val xrayPassword = property("tock_bot_test_xray_password", "please set xray password")

    val xray: XrayApi

    init {
        xray = retrofitBuilderWithTimeoutAndLogger(
            xrayTimeoutInSeconds,
            interceptors = listOf(basicAuthInterceptor(xrayLogin, xrayPassword))
        )
            .addJacksonConverter()
            .baseUrl(xrayUrl)
            .build()
            .create()
    }

    /**
     * Return all the tests contained in the test plan given in parameter.
     * First of all, the function will gather all tests of the given test plan,
     * and then, it will retrieve the Xray issue for each test.
     *
     * @param testPlanKey Identifier of the test plan to obtain its tests.
     * @return a list of XrayTest to represent all tests of the targeted test plan.
     */
    fun getTestPlanTests(testPlanKey: String): List<XrayTest> {
        // retrieve all tests of the given test plan
        val tests = xray.getTestsOfTestPlan(testPlanKey).execute().body() ?: error("no test in $testPlanKey")
        // and retrieve the content of those tests, including steps, and return them
        return xray.getTests(tests.joinToString(";") { it.key })
            .execute()
            .body()
            ?: error("unable to get tests for $tests")
    }

    fun getTests(testKey: String): List<XrayTest> {
        return xray.getTests(testKey)
            .execute()
            .body()
            ?: error("unable to get the test $testKey")
    }

    /**
     * Ask the Jira API for the steps of a given test.
     *
     * @param testKey is the identifier of the test to retrieve the steps.
     * @return a list of steps as a XrayTestStep object.
     */
    fun getTestSteps(testKey: String): List<XrayTestStep> =
        xray.getTestSteps(testKey).execute().body() ?: error("no test steps for $testKey")

    /**
     * This functions will search for the issue using the JQL query given in parameters
     * and return the identifier of the retrieved issue.
     * This function will return a result only if there is only one issue retrieved by the JQL query.
     *
     * @param jql is the query format of Jira to search for issues.
     * @return The identifier of the retrieved issue in String format.
     */
    fun getKeyOfSearchedIssue(jql: String): SearchIssueResult {
        val klaxon = Klaxon()

        // get the body content of the response
        val body = xray.searchIssue(jql).execute().body()?.string()

        // parse the body
        val parsed = klaxon.parseJsonObject(StringReader(body))
        val issuesArray = parsed.array<Any>("issues")

        // if only one issue has been found, return the identifier of the issue
        return when (issuesArray?.size ?: 0) {
            0 -> NoIssueRetrieved
            1 -> {
                IssueRetrieved(
                    issuesArray?.get("key")?.value.toString().replace("[", "").replace("]", "")
                )
            }
            else -> TooMuchIssuesRetrieved
        }
    }

    fun getProjectFromIssue(issueKey: String): JiraTestProject {
        val klaxon = Klaxon()
        val xrayResponse = xray.getIssue(issueKey).execute()

        return if (xrayResponse.isSuccessful) {
            val parsed = klaxon.parseJsonObject(StringReader(xrayResponse.body()?.string() ?: ""))
            val project = (parsed.getValue("fields") as JsonObject).map["project"]
            JiraTestProject((project as JsonObject)["key"].toString())
        } else {
            logger.error { "ERROR -- Jira project not retrieved -->" + xrayResponse.errorBody().toString() }
            JiraTestProject("")
        }
    }

    /**
     * Create a new Test execution issue in Jira.
     *
     * @param testExecutionFields contains all required information to be able to create the new issue.
     */
    fun createNewTestExecutionIssue(textExectuionFields: XrayTestExecutionCreation): JiraIssue {
        return xray.createTestExecution(textExectuionFields).execute().body()
            ?: error("Test execution creation has failed.")
    }

    /**
     * Send the test execution to Jira.
     *
     * @param execution is the result of the test execution.
     * @return the answer of Jira after the test execution reception.
     */
    fun sendTestExecution(execution: XrayTestExecution): Response<ResponseBody> =
        xray.sendTestExecution(execution).execute()

    /**
     * This function converts an attachment file into a String.
     *
     * @param attachment is the attachment stored as an XrayAttachment object linked to a test step.
     * @return the content of the attachment in String format.
     */
    fun getAttachmentToString(attachment: XrayAttachment): String =
        xray.getAttachment(attachment.id, attachment.fileName).execute().body()?.string()
            ?: "error : empty jira attachment"

    fun createTest(test: JiraTest): JiraIssue =
        xray.createTest(test).execute().body() ?: error("error during creating test $test")

    fun saveStep(testKey: String, step: XrayBuildTestStep) = xray.saveStep(testKey, step).execute().body()

    fun deleteStep(testKey: String, stepId: Long) =
        xray.deleteStep(testKey, stepId).execute().body() ?: error("error during test step deletion")

    fun getIssue(issueKey: String) = xray.getIssue(issueKey).execute().body() ?: error("error: issue not found")

    fun addPrecondition(preConditionKey: String, jiraId: String) =
        xray.addPrecondition(
            preConditionKey,
            XrayUpdateTest(listOf(jiraId))
        ).execute().body()

    fun updateTest(jiraId: String, test: JiraTest) = xray.updateTest(jiraId, test).execute().body()

    fun uploadAttachment(issueId: String, name: String, content: String): JiraAttachment =
        xray.addAttachment(
            issueId,
            MultipartBody.Part.createFormData(
                "file",
                name,
                RequestBody.create("text/plain".toMediaType(), content)
            )
        )
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

    fun getLinkedIssues(key: String, linkedField: String): List<String> {
        val response = xray.getIssue(key).execute()
        val body = Parser().parse(StringBuilder(response.body()!!.string())) as JsonObject
        return body.obj("fields")?.array<String>(linkedField) ?: emptyList()
    }

    fun addTestToTestPlan(test: String, testPlan: String) =
        xray.addTestToTestPlans(testPlan, XrayUpdateTest(listOf(test))).execute()

    fun getProjectTestPlans(project: String): List<XrayTestPlan> {
        val response = xray.searchTestPlans(jql = "project= $project AND issuetype=\"Test Plan\" order by key ASC", fields = listOf("key", "summary")).execute()
        val searchResult = response.body() ?: error("No test plans for project $project !")

        return searchResult.issues
    }
}

sealed class SearchIssueResult
object TooMuchIssuesRetrieved : SearchIssueResult()
object NoIssueRetrieved : SearchIssueResult()
data class IssueRetrieved(val key: String) : SearchIssueResult()
