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
import ai.tock.nlp.sagemaker.SagemakerAwsClientProperties
import org.junit.jupiter.api.Disabled
import software.amazon.awssdk.regions.Region
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *  Tests are disabled because it calls sagemaker endpoints on aws that can be expensive. So be careful if you want to really execute it !!
 *  In each test, set endpoint name and profile you want to test in SagemakerAwsClientProperties
 */
class SagemakerAwsClientIntegrationTest {
    @Test
    @Disabled
    fun testParseIntents() {
        val config = SagemakerAwsClientProperties("test", Region.EU_WEST_3, "default", "application/json", "default")
        val client = SagemakerAwsClient(config)
        val response = client.parseIntent(SagemakerAwsClient.ParsedRequest("je veux un TGV Paris Marseille demain à 18h"))
        assertEquals(response.intent?.name, "evoyageurs:search_by_od")
        assertTrue { response.intent?.score!! > 0.93 }
    }

    @Test
    @Disabled
    fun testParseEntities() {
        val config = SagemakerAwsClientProperties("test", Region.EU_WEST_3, "default", "application/json", "default")
        val client = SagemakerAwsClient(config)
        val response = client.parseEntities(SagemakerAwsClient.ParsedRequest("Est-ce que mon TGV 8536 de Cannes à Montpellier a du retard ?"))
        println(response)
        assertEquals(response.entities[0].value, "TGV")
        assertEquals(response.entities[0].start, 15)
        assertEquals(response.entities[0].end, 18)
        assertEquals(response.entities[0].entity, "evoyageurs:mode")
        assertEquals(response.entities[0].role, "mode")
        assert(response.entities[0].confidence > 0.99)

        assertEquals(response.entities[1].value, "8536")
        assertEquals(response.entities[1].start, 19)
        assertEquals(response.entities[1].end, 23)
        assertEquals(response.entities[1].entity, "evoyageurs:train")
        assertEquals(response.entities[1].role, "train")
        assert(response.entities[1].confidence > 0.99)

        assertEquals(response.entities[2].value, "Cannes")
        assertEquals(response.entities[2].start, 27)
        assertEquals(response.entities[2].end, 33)
        assertEquals(response.entities[2].entity, "evoyageurs:location")
        assertEquals(response.entities[2].role, "origin")
        assert(response.entities[2].confidence > 0.99)

        assertEquals(response.entities[3].value, "Montpellier")
        assertEquals(response.entities[3].start, 36)
        assertEquals(response.entities[3].end, 47)
        assertEquals(response.entities[3].entity, "evoyageurs:location")
        assertEquals(response.entities[3].role, "destination")
        assert(response.entities[3].confidence > 0.99)
    }

    @Test
    @Disabled
    fun testParseEntitiesEnglishRequest() {
        val config = SagemakerAwsClientProperties("test", Region.EU_WEST_3, "default", "application/json", "default")
        val client = SagemakerAwsClient(config)
        val response = client.parseEntities(SagemakerAwsClient.ParsedRequest("Is my TGV 8536 from Cannes to Montpellier delayed?"))
        println(response)
        assertEquals(response.entities[0].value, "TGV")
        assertEquals(response.entities[0].start, 6)
        assertEquals(response.entities[0].end, 9)
        assertEquals(response.entities[0].entity, "evoyageurs:mode")
        assertEquals(response.entities[0].role, "mode")
        assert(response.entities[0].confidence > 0.99)

        assertEquals(response.entities[1].value, "8536")
        assertEquals(response.entities[1].start, 10)
        assertEquals(response.entities[1].end, 14)
        assertEquals(response.entities[1].entity, "evoyageurs:train")
        assertEquals(response.entities[1].role, "train")
        assert(response.entities[1].confidence > 0.99)

        assertEquals(response.entities[2].value, "Cannes")
        assertEquals(response.entities[2].start, 20)
        assertEquals(response.entities[2].end, 26)
        assertEquals(response.entities[2].entity, "evoyageurs:location")
        assertEquals(response.entities[2].role, "origin")
        assert(response.entities[2].confidence > 0.99)

        assertEquals(response.entities[3].value, "Montpellier")
        assertEquals(response.entities[3].start, 30)
        assertEquals(response.entities[3].end, 41)
        assertEquals(response.entities[3].entity, "evoyageurs:location")
        assertEquals(response.entities[3].role, "destination")
        assert(response.entities[3].confidence > 0.99)
    }
}
