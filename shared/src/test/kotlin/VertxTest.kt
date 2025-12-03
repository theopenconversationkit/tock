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

import ai.tock.shared.vertx.TockVertxProvider
import ai.tock.shared.vertx.VertxProvider
import ai.tock.shared.vertx.defaultVertxOptions
import ai.tock.shared.vertx.vertx
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.vertx.core.VertxOptions
import io.vertx.core.internal.VertxInternal
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.ThreadPoolExecutor
import kotlin.test.assertEquals

/**
 *
 */
class VertxTest {
    @BeforeEach
    fun before() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein.invoke {
                bind<VertxProvider>() with provider { TockVertxProvider }
            },
        )
    }

    @AfterEach
    fun after() {
        tockInternalInjector = KodeinInjector()
    }

    @Test
    fun testThatVertxOptionCouldBeOverrided() {
        defaultVertxOptions = VertxOptions(defaultVertxOptions)
        defaultVertxOptions.workerPoolSize = 100
        assertEquals(100, ((vertx as VertxInternal).workerPool().executor() as ThreadPoolExecutor).maximumPoolSize)
    }
}
