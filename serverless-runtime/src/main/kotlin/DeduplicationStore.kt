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

package ai.tock.serverless.queue

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Remembers the requests being processed or processed, so that a request delivered twice is processed once.
 */
interface DeduplicationStore {
    /**
     * Marks [key] as processed for [ttl], and returns true, or returns false when it is already marked.
     */
    fun markIfAbsent(
        key: String,
        ttl: Duration,
    ): Boolean

    /** Removes the mark of [key]: its processing failed, the queue may deliver it again. */
    fun unmark(key: String)
}

/**
 * A [DeduplicationStore] of one process: it does not see the requests processed by the other instances.
 */
class InMemoryDeduplicationStore : DeduplicationStore {
    private val marks = ConcurrentHashMap<String, Instant>()

    override fun markIfAbsent(
        key: String,
        ttl: Duration,
    ): Boolean {
        val now = Instant.now()
        marks.entries.removeIf { it.value.isBefore(now) }
        return marks.putIfAbsent(key, now + ttl) == null
    }

    override fun unmark(key: String) {
        marks.remove(key)
    }
}
