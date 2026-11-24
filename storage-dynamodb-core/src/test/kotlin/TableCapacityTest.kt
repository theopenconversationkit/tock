/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.storage.dynamodb

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.BillingMode
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TableCapacityTest {
    private val dynamo =
        GenericContainer("amazon/dynamodb-local:2.6.1")
            .withExposedPorts(8000)
            .waitingFor(Wait.forListeningPort())
            .also { it.start() }

    private val client =
        DynamoDbClient
            .builder()
            .httpClient(UrlConnectionHttpClient.create())
            .region(Region.EU_WEST_3)
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("x", "x")))
            .endpointOverride(URI("http://${dynamo.host}:${dynamo.getMappedPort(8000)}"))
            .build()

    @AfterAll
    fun stop() {
        client.close()
        dynamo.stop()
    }

    @Test
    fun `GIVEN a provisioned capacity WHEN the tables are created THEN each table and each index has its units`() {
        DynamoStore.createTables(client, "provisioned_", TableCapacity(BillingMode.PROVISIONED, readUnits = 3, writeUnits = 2))

        val tables = listOf("provisioned_config", "provisioned_runtime", "provisioned_nlp_sentence").map { name -> client.describeTable { it.tableName(name) }.table() }
        val throughputs = tables.map { it.provisionedThroughput() } + tables.flatMap { table -> table.globalSecondaryIndexes().map { it.provisionedThroughput() } }
        // 3 tables and 3 indexes: the free tier covers 25 units
        assertEquals(6, throughputs.size)
        throughputs.forEach {
            assertEquals(3, it.readCapacityUnits())
            assertEquals(2, it.writeCapacityUnits())
        }
    }

    @Test
    fun `GIVEN the default capacity WHEN the tables are created THEN they are on demand`() {
        DynamoStore.createTables(client, "on_demand_", TableCapacity.fromProperties())

        val table = client.describeTable { it.tableName("on_demand_runtime") }.table()
        assertEquals(BillingMode.PAY_PER_REQUEST, table.billingModeSummary().billingMode())
    }

    @Test
    fun `GIVEN no provisioned unit THEN the capacity is refused`() {
        assertFailsWith<IllegalArgumentException> { TableCapacity(BillingMode.PROVISIONED, readUnits = 0) }
        assertNull(TableCapacity(BillingMode.PAY_PER_REQUEST, 0, 0).throughput())
    }
}
