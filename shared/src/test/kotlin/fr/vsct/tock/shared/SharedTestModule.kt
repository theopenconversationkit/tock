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

package fr.vsct.tock.shared

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import fr.vsct.tock.shared.cache.TockCache
import org.litote.kmongo.Id
import java.time.Duration
import java.util.concurrent.Callable

/**
 * Test module used by tests using Ioc.
 */
val sharedTestModule = Kodein.Module {
    bind<Executor>() with provider { TestExecutor }
    bind<TockCache>() with provider { NoOpCache }
}

private object NoOpCache : TockCache {
    private val map: MutableMap<Pair<Id<*>, String>, Any> = mutableMapOf()

    override fun <T> get(id: Id<T>, type: String): T? {
        @Suppress("UNCHECKED_CAST")
        return map[id to type] as T?
    }

    override fun <T : Any> put(id: Id<T>, type: String, data: T) {
        map[id to type] = data
    }

    override fun <T> getAll(type: String): Map<Id<T>, Any> {
        return map
                .entries
                .filter { it.key.second == type }
                .map {
                    @Suppress("UNCHECKED_CAST")
                    it.key.first as Id<T> to it.value
                }
                .toMap()
    }

    override fun <T> remove(id: Id<T>, type: String) {
        map - id to type
    }
}

private object TestExecutor : Executor {

    override fun executeBlocking(delay: Duration, runnable: () -> Unit) {
        runnable.invoke()
    }

    override fun executeBlocking(runnable: () -> Unit) {
        runnable.invoke()
    }

    override fun <T> executeBlocking(blocking: Callable<T>, result: (T?) -> Unit) {
        result.invoke(blocking.call())
    }

    override fun setPeriodic(initialDelay: Duration, delay: Duration, runnable: () -> Unit): Long {
        runnable.invoke()
        return 0L
    }
}