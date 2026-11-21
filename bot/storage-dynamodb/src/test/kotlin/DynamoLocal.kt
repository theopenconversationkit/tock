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

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

/**
 * One DynamoDB Local for all the tests of the JVM: [DynamoStore] keeps its client, so the endpoint cannot change.
 * The container is stopped with the JVM (Ryuk).
 */
object DynamoLocal {
    private val container by lazy {
        GenericContainer("amazon/dynamodb-local:2.6.1")
            .withExposedPorts(8000)
            .waitingFor(Wait.forListeningPort())
            .also { it.start() }
    }

    /** Starts DynamoDB Local if needed, and points [DynamoStore] to it. */
    fun start() {
        System.setProperty("tock_dynamodb_endpoint", "http://${container.host}:${container.getMappedPort(8000)}")
        System.setProperty("aws.accessKeyId", "x")
        System.setProperty("aws.secretAccessKey", "x")
        System.setProperty("tock_dynamodb_table_prefix", "test_")
        // short leases, so that the tests of DynamoUserLock wait less (read when DynamoUserLock is initialized)
        System.setProperty("tock_bot_lock_timeout_in_ms", "1000")
    }
}
