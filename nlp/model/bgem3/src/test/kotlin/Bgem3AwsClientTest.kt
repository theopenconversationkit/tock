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

import ai.tock.nlp.bgem3.Bgem3AwsClient
import ai.tock.nlp.bgem3.Bgem3AwsClient.ParsedEntitiesResponse
import ai.tock.nlp.bgem3.Bgem3AwsClientProperties
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Disabled
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

class Bgem3AwsClientTest {

    @Test
    @Disabled  // Test is disabled because it calls a sagemaker endpoint on aws that can be expensive. So be careful if you want to really execute it
    fun testParseIntents() {
        val config = Bgem3AwsClientProperties(Region.EU_WEST_3, "bge-m3-model-intent--v0", "application/json", "sa-voyageurs-dev")
        val client = Bgem3AwsClient(config)
        val response = client.parseIntent(Bgem3AwsClient.ParsedRequest("je veux un TGV Paris Marseille demain à 18h"))
        assertEquals(response.intent?.name, "evoyageurs:search_by_od")
        assertTrue { response.intent?.score!! > 0.98 }
    }

    @Test
    @Disabled // Test is disabled because it calls a sagemaker endpoint on aws that can be expensive. So be careful if you want to really execute it
    fun testParseEntities() {
        val config = Bgem3AwsClientProperties(Region.EU_WEST_3, "bge-m3-model-entities", "application/json", "sa-voyageurs-dev")
        val client = Bgem3AwsClient(config)
        val response = client.parseEntities(Bgem3AwsClient.ParsedRequest("Est-ce que mon TGV 8536 de Cannes à Montpellier a du retard ?"))
        println(response)
        assertEquals(response.entities[0].value , "TGV")
        assertEquals(response.entities[0].start , 15)
        assertEquals(response.entities[0].end , 18)
        assertEquals(response.entities[0].entity , "evoyageurs:mode")
        assertEquals(response.entities[0].role , "mode")
        assert(response.entities[0].confidence > 0.99)


        assertEquals(response.entities[1].value , "8536")
        assertEquals(response.entities[1].start , 19)
        assertEquals(response.entities[1].end , 23)
        assertEquals(response.entities[1].entity , "evoyageurs:train")
        assertEquals(response.entities[1].role , "train")
        assert(response.entities[1].confidence > 0.99)

        assertEquals(response.entities[2].value , "Cannes")
        assertEquals(response.entities[2].start , 27)
        assertEquals(response.entities[2].end , 33)
        assertEquals(response.entities[2].entity , "evoyageurs:location")
        assertEquals(response.entities[2].role , "origin")
        assert(response.entities[2].confidence > 0.99)

        assertEquals(response.entities[3].value , "Montpellier")
        assertEquals(response.entities[3].start , 36)
        assertEquals(response.entities[3].end , 47)
        assertEquals(response.entities[3].entity , "evoyageurs:location")
        assertEquals(response.entities[3].role , "destination")
        assert(response.entities[3].confidence > 0.99)
    }

    @Test
    fun testParsedIntentResponseDeserializeJson(){
        val parsedIntent = Bgem3AwsClient.ParsedIntent("GREETINGS",0.98)
        val parsedIntentResponse = Bgem3AwsClient.ParsedIntentResponse(parsedIntent)
        val jsonString = mapper.writeValueAsString(parsedIntentResponse)
        println(jsonString)
        val sdkBytes = SdkBytes.fromString(jsonString,Charsets.UTF_8)
        val builder = InvokeEndpointResponse.builder()
        builder.body(sdkBytes)
        builder.contentType("application/json")
        val response = mapper.readValue<Bgem3AwsClient.ParsedIntentResponse>(builder.build().body().asInputStream())
        assertEquals(response.intent?.name, "GREETINGS")
        assertEquals(response.intent?.score, 0.98)
    }

    @Test
    fun testParsedEntityResponseDeserializeJson(){
        val parsedEntity = Bgem3AwsClient.ParsedEntity(0,5,"value","TRAIN",0.98,"role")
        val parsedEntityResponse = ParsedEntitiesResponse(listOf(parsedEntity))
        val jsonString =  mapper.writeValueAsString(parsedEntityResponse)
        println(jsonString)
        val sdkBytes = SdkBytes.fromString(jsonString,Charsets.UTF_8)
        val builder = InvokeEndpointResponse.builder()
        builder.body(sdkBytes)
        builder.contentType("application/json")
        val response = mapper.readValue<ParsedEntitiesResponse>(builder.build().body().asInputStream())
        println(response)
    }


}
