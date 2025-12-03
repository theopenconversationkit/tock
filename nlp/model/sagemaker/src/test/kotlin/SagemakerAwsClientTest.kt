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

import ai.tock.nlp.sagemaker.SagemakerAwsClient
import ai.tock.nlp.sagemaker.SagemakerAwsClient.ParsedEntitiesResponse
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class SagemakerAwsClientTest {
    @Test
    fun testParsedIntentResponseDeserializeJson() {
        val parsedIntent = SagemakerAwsClient.ParsedIntent("GREETINGS", 0.98)
        val parsedIntentResponse = SagemakerAwsClient.ParsedIntentResponse(parsedIntent)
        val jsonString = mapper.writeValueAsString(parsedIntentResponse)
        val sdkBytes = SdkBytes.fromString(jsonString, Charsets.UTF_8)
        val builder = InvokeEndpointResponse.builder()
        builder.body(sdkBytes)
        builder.contentType("application/json")
        val response = mapper.readValue<SagemakerAwsClient.ParsedIntentResponse>(builder.build().body().asInputStream())
        assertEquals(response.intent?.name, "GREETINGS")
        assertEquals(response.intent?.score, 0.98)
    }

    @Test
    fun testParsedEntityResponseDeserializeJson() {
        val parsedEntity = SagemakerAwsClient.ParsedEntity(0, 5, "value", "TRAIN", 0.98, "role")
        val parsedEntityResponse = ParsedEntitiesResponse(listOf(parsedEntity))
        val jsonString = mapper.writeValueAsString(parsedEntityResponse)
        val sdkBytes = SdkBytes.fromString(jsonString, Charsets.UTF_8)
        val builder = InvokeEndpointResponse.builder()
        builder.body(sdkBytes)
        builder.contentType("application/json")
        val response = mapper.readValue<ParsedEntitiesResponse>(builder.build().body().asInputStream())
        assertEquals(response.entities[0].start, 0)
        assertEquals(response.entities[0].end, 5)
        assertEquals(response.entities[0].value, "value")
        assertEquals(response.entities[0].entity, "TRAIN")
        assertEquals(response.entities[0].confidence, 0.98)
        assertEquals(response.entities[0].role, "role")
    }
}
