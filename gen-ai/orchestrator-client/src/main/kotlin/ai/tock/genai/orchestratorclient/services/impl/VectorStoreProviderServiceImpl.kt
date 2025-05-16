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

package ai.tock.genai.orchestratorclient.services.impl

import ai.tock.genai.orchestratorclient.api.VectorStoreProviderApi
import ai.tock.genai.orchestratorclient.requests.VectorStoreProviderSettingStatusRequest
import ai.tock.genai.orchestratorclient.responses.ProviderSettingStatusResponse
import ai.tock.genai.orchestratorclient.retrofit.GenAIOrchestratorClient
import ai.tock.genai.orchestratorclient.services.VectorStoreProviderService
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreProvider

class VectorStoreProviderServiceImpl: VectorStoreProviderService {
    private val retrofit = GenAIOrchestratorClient.getClient()
    private val vectorStoreProviderApi = retrofit.create(VectorStoreProviderApi::class.java)

    override fun checkSetting(query: VectorStoreProviderSettingStatusRequest): ProviderSettingStatusResponse? {
        val response = vectorStoreProviderApi.checkVectorStoreSetting(
            query,
            query.vectorStoreSetting?.provider ?: VectorStoreProvider.Default
        ).execute()
        return response.body()
    }
}
