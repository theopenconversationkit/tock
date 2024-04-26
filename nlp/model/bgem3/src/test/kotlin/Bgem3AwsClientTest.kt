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
import ai.tock.nlp.bgem3.Bgem3AwsClient.ParsedEntityResponse
import ai.tock.nlp.bgem3.Bgem3Configuration
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
    fun testParseIntents() {
        val config = Bgem3Configuration(Region.EU_WEST_3, "test", "application/json", "sa-voyageurs-dev")
        val client = Bgem3AwsClient(config)
        val response = client.parseIntent(Bgem3AwsClient.ParsedRequest("I like you so much"))
        assertEquals(response.intent?.name, "POSITIVE")
        assertTrue { response.intent?.score!! > 0.99 }
    }

    @Test
    fun testParseEntities() {
        val config = Bgem3Configuration(Region.EU_WEST_3, "test", "application/json", "sa-voyageurs-dev")
        val client = Bgem3AwsClient(config)
        val response = client.parseEntities(Bgem3AwsClient.ParsedRequest("Paris Marseille demain 15h"))
        // TODO to complete
        assertNotNull(response.entities)
        assertEquals(response.entities[0].entity, "ORIGIN")
        assertEquals(response.entities[1].entity, "DESTINATION")
        assertEquals(response.entities[2].entity, "MOMENT")
    }

    @Test
    fun testParsedIntentResponseDeserializeJson(){
        val parsedIntent = Bgem3AwsClient.ParsedIntent("GREETINGS",0.98)
        val parsedIntentResponse = Bgem3AwsClient.ParsedIntentResponse(parsedIntent)
        val sdkBytes = SdkBytes.fromString(mapper.writeValueAsString(parsedIntentResponse),Charsets.UTF_8)
        val builder = InvokeEndpointResponse.builder()
        builder.body(sdkBytes)
        builder.contentType("application/json")
        val response = mapper.readValue<Bgem3AwsClient.ParsedIntentResponse>(builder.build().body().asInputStream())
        assertEquals(response.intent?.name, "GREETINGS")
        assertEquals(response.intent?.score, 0.98)
    }

    @Test
    fun testParsedEntityResponseDeserializeJson(){
        val parsedEntity = Bgem3AwsClient.ParsedEntity(0,5,"value","TRAIN","MODE_TRANSPORT",95.2,"role","extractor")
        val parsedEntityResponse = ParsedEntityResponse(listOf(parsedEntity))
        val sdkBytes = SdkBytes.fromString(mapper.writeValueAsString(parsedEntityResponse),Charsets.UTF_8)
        val builder = InvokeEndpointResponse.builder()
        builder.body(sdkBytes)
        builder.contentType("application/json")
        val response = mapper.readValue<ParsedEntityResponse>(builder.build().body().asInputStream())
        println(response)
    }


}
