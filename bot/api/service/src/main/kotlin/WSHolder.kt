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

package ai.tock.bot.api.service

import ai.tock.bot.api.model.websocket.ResponseData
import ai.tock.shared.Executor
import ai.tock.shared.injector
import ai.tock.shared.longProperty
import ai.tock.shared.provide
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import mu.KotlinLogging
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

internal class WSHolder {
    companion object {
        private val timeoutInSeconds: Long = longProperty("tock_api_timout_in_s", 10)
        private val logger = KotlinLogging.logger {}

        private val wsRepository: Cache<String, WSHolder> =
            CacheBuilder.newBuilder().expireAfterWrite(timeoutInSeconds + 1, SECONDS).build()

        private val executor: Executor get() = injector.provide()

        fun getHolderIfPresent(requestId: String): WSHolder? = wsRepository.getIfPresent(requestId)

        fun setHolder(requestId: String): WSHolder = WSHolder().apply { wsRepository.put(requestId, this) }
    }

    private val response: MutableList<ResponseData> = CopyOnWriteArrayList()
    private val seen: MutableSet<ResponseData> = CopyOnWriteArraySet()

    @Volatile
    private var latch: CountDownLatch = CountDownLatch(1)

    fun receive(response: ResponseData) {
        logger.debug { "add to holder: $response" }
        this.response.add(response)
        synchronized(response) {
            latch.countDown()
        }
    }

    fun wait(): List<ResponseData> {
        logger.debug { "start await" }
        latch.await(timeoutInSeconds, SECONDS)
        synchronized(response) {
            val r = response.sortedBy { it.botResponse?.context?.date ?: Instant.now() }
            logger.debug { "responses: $r" }
            if (r.lastOrNull()?.botResponse?.context?.lastResponse == false) {
                latch = CountDownLatch(1)
            }

            return r.filterNot { seen.contains(it) }.apply { seen.addAll(this) }
        }
    }

    fun waitForResponse(sendResponse: (ResponseData?) -> Unit = {}): Unit =
        executor.executeBlocking {
            var response: ResponseData?
            do {
                val responses = wait()
                response = responses.lastOrNull()
                responses.forEach {
                    sendResponse(it)
                }
            } while (response?.botResponse?.context?.lastResponse == false)
        }
}
