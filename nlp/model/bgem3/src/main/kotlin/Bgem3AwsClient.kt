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
package ai.tock.nlp.bgem3

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest
import java.nio.charset.Charset

class Bgem3AwsClient(private val configuration: Bgem3Configuration) {

    data class ParsedResponse(
        val intent: ParsedIntent? = null,
        val intent_ranking: List<ParsedIntent> = emptyList()
    )

    data class ParseRequest(
        val text: String
    )

    data class ParsedIntent(
        val label: String?,
        val score: Double?
    )

    private val runtimeClient : SageMakerRuntimeClient = SageMakerRuntimeClient.builder()
        .region(configuration.region)
        .credentialsProvider(ProfileCredentialsProvider.create(configuration.profileName))
        .build()

    fun parse(request: ParseRequest): ParsedResponse = invokeSageMakerEndpoint(request.text)

    private fun invokeSageMakerEndpoint(
        payload: String,
    ): ParsedResponse {
        val endpointRequest = InvokeEndpointRequest.builder()
            .endpointName(configuration.endpointName)
            .contentType(configuration.contentType)
            .body(SdkBytes.fromString(payload, Charset.defaultCharset()))
            .build()
        val response = runtimeClient.invokeEndpoint(endpointRequest)
        val jsonString = response.body().asString(Charset.defaultCharset())
        val label = extractLabel(jsonString)
        val score = extractScore(jsonString)
        return ParsedResponse(ParsedIntent(label,score))
    }

    private fun extractLabel(jsonString: String): String? {
        val regex = "\"label\":\\s*\"([^\"]+)\"".toRegex()
        val matchResult = regex.find(jsonString)
        return matchResult?.groupValues?.get(1)
    }

    private fun extractScore(jsonString: String): Double? {
        val regex = "\"score\":\\s*([\\d.]+)".toRegex()
        val matchResult = regex.find(jsonString)
        return matchResult?.groupValues?.get(1)?.toDoubleOrNull()
    }
}
