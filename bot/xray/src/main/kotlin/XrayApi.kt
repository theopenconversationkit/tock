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

import ai.tock.bot.xray.model.JiraAttachment
import ai.tock.bot.xray.model.JiraIssue
import ai.tock.bot.xray.model.JiraIssueLink
import ai.tock.bot.xray.model.JiraTest
import ai.tock.bot.xray.model.SearchResult
import ai.tock.bot.xray.model.XrayBuildTestStep
import ai.tock.bot.xray.model.XrayTest
import ai.tock.bot.xray.model.XrayTestExecution
import ai.tock.bot.xray.model.XrayTestExecutionCreation
import ai.tock.bot.xray.model.XrayTestStep
import ai.tock.bot.xray.model.XrayUpdateTest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 *
 */
interface XrayApi {
    @GET("/rest/raven/1.0/api/testplan/{testPlanKey}/test")
    fun getTestsOfTestPlan(
        @Path("testPlanKey") testPlanKey: String,
    ): Call<List<XrayTest>>

    @POST("/rest/raven/1.0/api/testplan/{testPlanKey}/test")
    fun addTestToTestPlans(
        @Path("testPlanKey") testPlanKey: String,
        @Body update: XrayUpdateTest,
    ): Call<ResponseBody>

    @GET("/rest/raven/1.0/api/test")
    fun getTests(
        @Query("keys") testKeys: String,
    ): Call<List<XrayTest>>

    @GET("/rest/raven/1.0/api/test/{testKey}/step")
    fun getTestSteps(
        @Path("testKey") testKey: String,
    ): Call<List<XrayTestStep>>

    @POST("/rest/raven/1.0/import/execution")
    fun sendTestExecution(
        @Body execution: XrayTestExecution,
    ): Call<ResponseBody>

    @GET("/plugins/servlet/raven/attachment/{id}/{fileName}")
    fun getAttachment(
        @Path("id") id: Long,
        @Path("fileName") fileName: String,
    ): Call<ResponseBody>

    @PUT("/rest/raven/1.0/api/test/{testKey}/step")
    fun saveStep(
        @Path("testKey") testKey: String,
        @Body execution: XrayBuildTestStep,
    ): Call<ResponseBody>

    @DELETE("/rest/raven/1.0/api/test/{testKey}/step/{id}")
    fun deleteStep(
        @Path("testKey") testKey: String,
        @Path("id") stepId: Long,
    ): Call<ResponseBody>

    @POST("/rest/raven/1.0/api/precondition/{preConditionKey}/test")
    fun addPrecondition(
        @Path("preConditionKey") preConditionKey: String,
        @Body associate: XrayUpdateTest,
    ): Call<ResponseBody>

    @POST("/rest/api/2/issue")
    fun createTest(
        @Body test: JiraTest,
    ): Call<JiraIssue>

    @POST("/rest/api/2/issue")
    fun createTestExecution(
        @Body fields: XrayTestExecutionCreation,
    ): Call<JiraIssue>

    @POST("/rest/api/2/issueLink")
    fun linkIssue(
        @Body link: JiraIssueLink,
    ): Call<ResponseBody>

    @PUT("/rest/api/2/issue/{id}")
    fun updateTest(
        @Path("id") id: String,
        @Body test: JiraTest,
    ): Call<ResponseBody>

    @Multipart
    @POST("/rest/api/2/issue/{id}/attachments")
    fun addAttachment(
        @Path("id") id: String,
        @Part body: MultipartBody.Part,
        @Header("X-Atlassian-Token") token: String = "no-check",
    ): Call<List<JiraAttachment>>

    @GET("/rest/api/2/issue/{id}")
    fun getIssue(
        @Path("id") id: String,
    ): Call<ResponseBody>

    @GET("/rest/api/2/search")
    fun searchIssue(
        @Query("jql") jql: String,
    ): Call<ResponseBody>

    @GET("/rest/api/2/search")
    fun searchTestPlans(
        @Query("jql") jql: String,
        @Query("fields") fields: List<String>,
    ): Call<SearchResult>
}
