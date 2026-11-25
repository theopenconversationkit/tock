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

package ai.tock.shared

import ai.tock.shared.cache.TockCache
import ai.tock.shared.cache.mongo.MongoCache
import ai.tock.shared.security.NoOpTockUserListener
import ai.tock.shared.security.TockUserListener
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.security.auth.spi.WebSecurityMode
import ai.tock.shared.security.mongo.DefaultMongoCredentialsProvider
import ai.tock.shared.security.mongo.MongoCredentialsProvider
import ai.tock.shared.vertx.TockVertxProvider
import ai.tock.shared.vertx.VertxProvider
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/**
 * [sharedModule] is declared in tock-shared-core, but tock-shared adds the Vert.x and MongoDB bindings:
 * the projects that depend on tock-shared must keep all of them.
 */
class SharedModuleTest {
    private val kodein = Kodein { import(sharedModule) }

    @Test
    fun `sharedModule keeps the Vert_x and MongoDB bindings of tock-shared`() {
        assertEquals("ai.tock.shared.vertx", kodein.instance<Executor>().javaClass.packageName)
        assertSame(MongoCache, kodein.instance<TockCache>())
        assertSame(TockVertxProvider, kodein.instance<VertxProvider>())
        assertSame(NoOpTockUserListener, kodein.instance<TockUserListener>())
        assertSame(DefaultMongoCredentialsProvider, kodein.instance<MongoCredentialsProvider>())
        listOf(WebSecurityMode.COOKIES, WebSecurityMode.COOKIES_ENCRYPTED, WebSecurityMode.PASSTHROUGH).forEach {
            assertNotNull(kodein.instance<WebSecurityHandler>(tag = it.name))
        }
    }
}
