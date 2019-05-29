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

import fr.vsct.tock.bot.admin.test.xray.model.JiraAttachment
import fr.vsct.tock.bot.admin.test.xray.model.JiraIssue
import fr.vsct.tock.bot.admin.test.xray.model.JiraIssueLink
import fr.vsct.tock.bot.admin.test.xray.model.JiraTest
import fr.vsct.tock.bot.admin.test.xray.model.XrayBuildTestStep
import fr.vsct.tock.bot.admin.test.xray.model.XrayUpdateTest
import fr.vsct.tock.bot.admin.test.xray.model.XrayTest
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestExecution
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestStep
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
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
    fun getTestsOfTestPlan(@Path("testPlanKey") testPlanKey: String): Call<List<XrayTest>>

    @POST("/rest/raven/1.0/api/testplan/{testPlanKey}/test")
    fun addTestToTestPlans(
            @Path("testPlanKey") testPlanKey: String,
            @Body update: XrayUpdateTest): Call<ResponseBody>

    @GET("/rest/raven/1.0/api/test")
    fun getTests(@Query("keys") testKeys: String): Call<List<XrayTest>>

    @GET("/rest/raven/1.0/api/test/{testKey}/step")
    fun getTestSteps(@Path("testKey") testKey: String): Call<List<XrayTestStep>>

    @POST("/rest/raven/1.0/import/execution")
    fun sendTestExecution(@Body execution: XrayTestExecution): Call<ResponseBody>

    @GET("/plugins/servlet/raven/attachment/{id}/{fileName}")
    fun getAttachment(@Path("id") id: Long, @Path("fileName") fileName: String): Call<ResponseBody>

    @PUT("/rest/raven/1.0/api/test/{id}/step")
    fun saveStep(@Path("id") id: String, @Body execution: XrayBuildTestStep): Call<ResponseBody>

    @POST("/rest/raven/1.0/api/precondition/{preConditionKey}/test")
    fun addPrecondition(
            @Path("preConditionKey") preConditionKey: String,
            @Body associate: XrayUpdateTest): Call<ResponseBody>

    @POST("/rest/api/2/issue")
    fun createTest(@Body test: JiraTest): Call<JiraIssue>

    @POST("/rest/api/2/issueLink")
    fun linkIssue(@Body link: JiraIssueLink): Call<ResponseBody>

    @PUT("/rest/api/2/issue/{id}")
    fun updateTest(@Path("id") id: String, @Body test: JiraTest): Call<ResponseBody>

    @Multipart
    @POST("/rest/api/2/issue/{id}/attachments")
    fun addAttachment(
            @Path("id") id: String,
            @Part body: MultipartBody.Part,
            @Header("X-Atlassian-Token") token: String = "no-check")
            : Call<List<JiraAttachment>>

    @GET("/rest/api/2/issue/{id}")
    fun getIssue(@Path("id") id: String): Call<ResponseBody>
}