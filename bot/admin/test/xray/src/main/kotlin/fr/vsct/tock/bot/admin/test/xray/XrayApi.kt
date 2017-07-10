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

import fr.vsct.tock.bot.admin.test.xray.model.XrayTest
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestExecution
import fr.vsct.tock.bot.admin.test.xray.model.XrayTestStep
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 *
 */
interface XrayApi {

    @GET("/rest/raven/1.0/api/testplan/{testPlanKey}/test")
    fun getTestsOfTestPlan(@Path("testPlanKey") testPlanKey: String): Call<List<XrayTest>>

    @GET("/rest/raven/1.0/api/test")
    fun getTests(@Query("keys") testKeys: List<String>): Call<List<XrayTest>>

    @GET("/rest/raven/1.0/api/test/{testKey}/step")
    fun getTestSteps(@Path("testKey") testKey: String): Call<List<XrayTestStep>>

    @POST("/rest/raven/1.0/import/execution")
    fun sendTestExecution(@Body execution: XrayTestExecution): Call<ResponseBody>

    @GET("/plugins/servlet/raven/attachment/{id}/{fileName}")
    fun getAttachment(@Path("id") id: Long, @Path("fileName") fileName: String): Call<ResponseBody>
}