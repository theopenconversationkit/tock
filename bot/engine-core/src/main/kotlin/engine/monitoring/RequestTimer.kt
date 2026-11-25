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

package ai.tock.bot.engine.monitoring

import ai.tock.bot.engine.BotRepository
import ai.tock.shared.error
import mu.KLogger

fun KLogger.logError(
    throwable: Throwable,
    data: RequestTimerData,
) {
    BotRepository.requestTimer.throwable(throwable, data)
    this.error(throwable)
}

fun KLogger.logError(
    message: String,
    data: RequestTimerData,
) {
    BotRepository.requestTimer.error(message, data)
    this.error(message)
}

/**
 * To track time for requests received and sent.
 * [start] and [end] are guaranteed to be called.
 */
interface RequestTimer {
    /**
     * Called at the start of the request.
     */
    fun start(type: String): RequestTimerData = RequestTimerData(type)

    /**
     * Called when an an error is detected.
     */
    fun error(
        errorMessage: String,
        data: RequestTimerData,
    ) {
        data.error = true
        data.message = errorMessage
    }

    /**
     * Called when exception is caught.
     */
    fun throwable(
        throwable: Throwable,
        data: RequestTimerData,
    ) {
        data.error = true
        data.throwable = throwable
    }

    /**
     * Called at the end of the request.
     */
    fun end(data: RequestTimerData) {
        // do nothing by default
    }
}
