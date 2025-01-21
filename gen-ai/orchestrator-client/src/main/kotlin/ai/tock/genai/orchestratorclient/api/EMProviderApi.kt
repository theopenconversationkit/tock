/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.genai.orchestratorclient.api

import ai.tock.genai.orchestratorclient.requests.EMProviderSettingStatusRequest
import ai.tock.genai.orchestratorclient.responses.ProviderSettingStatusResponse
import ai.tock.genai.orchestratorcore.models.em.EMProvider
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface EMProviderApi {
    @POST("/em-providers/{provider-id}/setting/status")
    fun checkEMSetting(
        @Body query: EMProviderSettingStatusRequest,
        @Path("provider-id") providerId: EMProvider
    ): Call<ProviderSettingStatusResponse>

}