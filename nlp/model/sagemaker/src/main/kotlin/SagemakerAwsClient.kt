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
package ai.tock.nlp.sagemaker

import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient
import software.amazon.awssdk.services.sagemaker.SageMakerClient
import software.amazon.awssdk.services.sagemaker.model.DescribeEndpointRequest
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest
import java.nio.charset.Charset

class SagemakerAwsClient(private val configuration: SagemakerAwsClientProperties) {

    val name = configuration.name

    // for intentions and entities
    data class ParsedRequest(
        val text: String
    )

    data class ParsedIntentResponse(
        val intent: ParsedIntent? = null,
        val intent_ranking: List<ParsedIntent> = emptyList()
    )

    data class ParsedIntent(
        val name: String?,
        val score: Double?
    )

    data class ParsedEntitiesResponse(
        val entities: List<ParsedEntity> = emptyList()
    )

    data class ParsedEntity(
        val start: Int,
        val end: Int,
        val value: String,
        val entity: String,
        val confidence: Double,
        val role: String? = null,
    )

    private val runtimeClient: SageMakerRuntimeClient = SageMakerRuntimeClient.builder()
        .region(configuration.region)
        .credentialsProvider(software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create())
        .build()

    private val sagemakerClient: SageMakerClient = SageMakerClient.builder()
        .region(configuration.region)
        .credentialsProvider(software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create())
        .build()

    fun parseIntent(request: ParsedRequest) = invokeSageMakerIntentEndpoint(request.text)

    fun parseEntities(request: ParsedRequest): ParsedEntitiesResponse = invokeSageMakerEntitiesEndpoint(request.text)

    private fun invokeSageMakerIntentEndpoint(
        payload: String,
    ): ParsedIntentResponse {
        val endpointRequest = InvokeEndpointRequest.builder()
            .endpointName(configuration.endpointName)
            .contentType(configuration.contentType)
            .body(SdkBytes.fromString("{\"inputs\":\"$payload\"}", Charset.defaultCharset()))
            .build()
        val response = runtimeClient.invokeEndpoint(endpointRequest)
        return mapper.readValue<ParsedIntentResponse>(response.body().asInputStream())
    }

    private fun invokeSageMakerEntitiesEndpoint(
        payload: String,
    ): ParsedEntitiesResponse {
        val endpointRequest = InvokeEndpointRequest.builder()
            .endpointName(configuration.endpointName)
            .contentType(configuration.contentType)
            .body(SdkBytes.fromString("{\"inputs\":\"$payload\"}", Charset.defaultCharset()))
            .build()
        val response = runtimeClient.invokeEndpoint(endpointRequest)
        val entities = mapper.readValue<List<ParsedEntity>>(response.body().asInputStream())
        return ParsedEntitiesResponse(entities)
    }

    fun healthcheck(): Boolean {
        val endpointRequest = DescribeEndpointRequest.builder()
            .endpointName(configuration.endpointName)
            .build()
        val response = sagemakerClient.describeEndpoint(endpointRequest)
       return response.endpointStatus() == software.amazon.awssdk.services.sagemaker.model.EndpointStatus.IN_SERVICE
    }
}
