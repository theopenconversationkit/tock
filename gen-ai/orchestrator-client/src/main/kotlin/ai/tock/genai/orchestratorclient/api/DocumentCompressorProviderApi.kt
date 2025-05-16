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

package ai.tock.genai.orchestratorclient.api

import ai.tock.genai.orchestratorclient.requests.DocumentCompressorProviderSettingStatusRequest
import ai.tock.genai.orchestratorclient.responses.ProviderSettingStatusResponse
import ai.tock.genai.orchestratorcore.models.compressor.DocumentCompressorProvider
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface DocumentCompressorProviderApi {
    @POST("/document-compressor-providers/{provider-id}/setting/status")
    fun checkDocumentCompressorSetting(
        @Body query: DocumentCompressorProviderSettingStatusRequest,
        @Path("provider-id") providerId: DocumentCompressorProvider
    ): Call<ProviderSettingStatusResponse>

}

