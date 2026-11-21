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

package ai.tock.shared.bytecode

import ai.tock.shared.http.HttpRoutes
import kotlin.test.Test
import kotlin.test.assertTrue

class BytecodeDependenciesTest {
    private val classes = BytecodeDependencies.locationOf(HttpRoutes::class.java)

    @Test
    fun `GIVEN the classes of tock-shared-core WHEN their references are read THEN they include the JDK and kotlin types they use`() {
        val references = BytecodeDependencies.references(classes)

        assertTrue(references.any { it.fromClass == "ai/tock/shared/http/HttpRoutes" && it.toClass == "ai/tock/shared/http/HttpRoute" })
        assertTrue(references.any { it.fromClass == "ai/tock/shared/Executor" && it.toClass == "java/util/concurrent/CompletableFuture" })
    }

    @Test
    fun `tock-shared-core references neither Vert_x nor Netty nor the MongoDB driver`() {
        val violations =
            BytecodeDependencies.violations(classes, forbidden = {
                it.startsWith("io/vertx/") || it.startsWith("io/netty/") || it.startsWith("com/mongodb/")
            })

        assertTrue(violations.isEmpty(), violations.joinToString("\n"))
    }
}
