/*
 * Copyright (C) 2017/2026 SNCF Connect & Tech
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

package ai.tock.shared.security.auth

import ai.tock.shared.security.auth.KeycloakOAuth2Provider.Companion.parseUserRoles
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class KeycloakOAuth2ProviderTest {
    @Test
    fun `GIVEN a comma-separated role attribute WHEN parsing THEN only the listed roles are returned`() {
        assertEquals(setOf("botUser", "nlpUser"), parseUserRoles("botUser, nlpUser"))
    }

    @Test
    fun `GIVEN a JSON array role attribute WHEN parsing THEN only the listed roles are returned`() {
        val token = JsonObject("""{"tock_roles":["admin","botUser"]}""")
        assertEquals(setOf("admin", "botUser"), parseUserRoles(token.getValue("tock_roles")))
    }

    @Test
    fun `GIVEN a stringified array role attribute WHEN parsing THEN the brackets are removed`() {
        assertEquals(setOf("admin", "botUser"), parseUserRoles("[admin, botUser]"))
    }

    @Test
    fun `GIVEN unknown roles WHEN parsing THEN they are ignored`() {
        assertEquals(setOf("nlpUser"), parseUserRoles(JsonArray().add("offline_access").add("nlpUser")))
        assertEquals(emptySet(), parseUserRoles("offline_access,uma_authorization"))
    }

    @Test
    fun `GIVEN a missing or empty role attribute WHEN parsing THEN no role is returned`() {
        assertEquals(emptySet(), parseUserRoles(null))
        assertEquals(emptySet(), parseUserRoles(""))
        assertEquals(emptySet(), parseUserRoles(JsonArray()))
    }
}
