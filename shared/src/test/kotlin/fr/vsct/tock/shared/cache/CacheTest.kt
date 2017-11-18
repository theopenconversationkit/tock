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

package fr.vsct.tock.shared.cache

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import fr.vsct.tock.shared.tockInternalInjector
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import kotlin.test.assertEquals

/**
 *
 */
class CacheTest {

    private val cache: TockCache = mock()
    val id: Id<String> = "id".toId()
    val type = "type"
    val value: String = "val"

    @Before
    fun before() {
        val module = Kodein.Module {
            bind<TockCache>() with instance(cache)
        }
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(Kodein {
            import(module)
        })
    }

    @After
    fun after() {
        removeFromCache(id, type)
    }

    @Test
    fun getOrCache_shouldUseTheMemoryCacheAndInvokeTheUnderlyingCacheOnlyOnce() {
        assertEquals(value, getOrCache(id, type, { value }))
        assertEquals(value, getOrCache(id, type, { value }))
        verify(cache).get(id, type)
    }

    @Test
    fun getFromCache_shouldUseTheMemoryCacheAndNotInvokeTheUnderlyingCache() {
        putInCache(id, type, value)
        assertEquals(value, getFromCache(id, type)!!)
        assertEquals(value, getFromCache(id, type)!!)
        verify(cache, never()).get(id, type)
    }
}