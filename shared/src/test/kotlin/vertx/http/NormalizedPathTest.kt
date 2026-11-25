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

package ai.tock.shared.vertx.http

import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import io.vertx.core.internal.net.RFC3986
import kotlin.test.Test
import kotlin.test.assertEquals

class NormalizedPathTest {
    @Test
    fun `GIVEN paths WHEN they are normalized THEN the result is the one of Vert_x`() {
        listOf(
            "/",
            "/io/alcmeon/start",
            "/io//alcmeon/start",
            "/io/alc%6Deon/start",
            "/io/a%20b/start",
            "/io/./x/../alcmeon",
            "/io/a%2fb",
            "/io/a+b",
            "/io/%7euser/",
            "/io/x/.",
            "/io/x/..",
            "/../io",
        ).forEach { path ->
            assertEquals(RFC3986.normalizePath(path), HttpRequest(HttpMethod.POST, path).normalizedPath, path)
        }
    }
}
