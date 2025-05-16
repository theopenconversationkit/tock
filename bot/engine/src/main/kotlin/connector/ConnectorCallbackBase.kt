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

package ai.tock.bot.connector

import ai.tock.bot.engine.BotRepository.requestTimer
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.monitoring.RequestTimer
import ai.tock.bot.engine.monitoring.RequestTimerData
import mu.KotlinLogging

/**
 * Base implementation of [ConnectorCallback] - add logging and [RequestTimer] monitoring.
 */
open class ConnectorCallbackBase(
    override val applicationId: String,
    val connectorType: ConnectorType
) : ConnectorCallback {

    private val requestTimerData = requestTimer.start("${connectorType.id}_response")
    private var lockTimerData: RequestTimerData? = null

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun userLocked(event: Event) {
        lockTimerData = requestTimer.start("${connectorType.id}_user_lock")
    }

    override fun userLockReleased(event: Event) {
        logger.trace { "lock released for $event" }
        lockTimerData?.also { requestTimer.end(it) }
    }

    override fun eventSkipped(event: Event) {
        logger.warn { "event skipped: $event" }
        requestTimer.error("event skipped: $event", requestTimerData)
        requestTimer.end(requestTimerData)
    }

    override fun eventAnswered(event: Event) {
        logger.trace { "event answered: $event" }
        requestTimer.end(requestTimerData)
    }

    override fun exceptionThrown(event: Event, throwable: Throwable) {
        logger.error("error thown for $event", throwable)
        requestTimer.throwable(throwable, requestTimerData)
        requestTimer.end(requestTimerData)
    }
}
