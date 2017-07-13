/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.shared

import fr.vsct.tock.shared.vertx.defaultVertxOptions
import fr.vsct.tock.shared.vertx.vertx
import io.vertx.core.VertxOptions
import io.vertx.core.impl.VertxInternal
import org.junit.Test
import java.util.concurrent.ThreadPoolExecutor
import kotlin.test.assertEquals

/**
 *
 */
class VertxTest {

    @Test
    fun testThatVertxOptionCouldBeOverrided() {
        defaultVertxOptions = VertxOptions(defaultVertxOptions)
        defaultVertxOptions.workerPoolSize = 100
        assertEquals(100, ((vertx as VertxInternal).workerPool as ThreadPoolExecutor).maximumPoolSize)
    }
}