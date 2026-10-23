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

package ai.tock.bot.admin.verticle

import ai.tock.shared.security.TockUserRole
import ai.tock.shared.security.auth.TockAuthProvider
import ai.tock.shared.vertx.WebVerticle
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.vertx.core.http.HttpMethod
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KnowledgeBaseVerticleTest {
    private class CaptureVerticle : WebVerticle() {
        override fun configure() {}

        override fun authProvider(): TockAuthProvider = mockk(relaxed = true)
    }

    /**
     * Every route registration funnels through the non-inline [WebVerticle.blocking]; capturing the role set it receives
     * per path is a real check of the route wiring, without deploying a Vert.x server. The inline json helpers are
     * expanded into [KnowledgeBaseVerticle.configure] at compile time and still bottom out in this call.
     */
    @Test
    fun `index lifecycle and bulk import export are admin only while entry CRUD stays open to botUser`() {
        val roles = mutableMapOf<String, Set<TockUserRole>?>()
        val verticle = spyk(CaptureVerticle())
        every { verticle.blocking(any<HttpMethod>(), any(), any<Set<TockUserRole>>(), any(), any()) } answers {
            roles[secondArg()] = thirdArg()
        }

        KnowledgeBaseVerticle().configure(verticle)

        val root = "/bots/:botId/knowledge-base"
        val adminOnly = setOf(TockUserRole.admin, TockUserRole.technicalAdmin)
        for (path in listOf("$root/index", "$root/import/preview", "$root/import", "$root/export")) {
            assertEquals(adminOnly, roles[path], "route $path must be admin-only")
            assertFalse(TockUserRole.botUser in roles.getValue(path)!!, "route $path must refuse botUser")
        }
        // Entry CRUD and read routes remain open to botUser.
        assertTrue(TockUserRole.botUser in roles.getValue("$root/entries")!!)
        assertTrue(TockUserRole.botUser in roles.getValue("$root/sync")!!)
    }
}
