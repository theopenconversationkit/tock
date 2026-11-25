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

import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.UserTimeline

/**
 * Used by connector implementations to check lifecycle of an user event.
 */
interface ConnectorCallback {
    /**
     * The application id.
     */
    val applicationId: String

    /**
     * Called by [ConnectorController.handle] when the user is locked.
     */
    fun userLocked(event: Event)

    /**
     * Called by [ConnectorController.handle] when the user lock is released.
     */
    fun userLockReleased(event: Event)

    /**
     * Called by [ConnectorController.handle] when the event is not handled.
     */
    fun eventSkipped(event: Event)

    /**
     * Called by [ConnectorController.handle] when the event is answered.
     */
    fun eventAnswered(event: Event)

    /**
     * Called by [ConnectorController.handle] when an exception is thrown.
     */
    fun exceptionThrown(
        event: Event,
        throwable: Throwable,
    )

    /**
     * Called when the initial UserTimeline is loaded.
     * Allows connectors to react to the timeline state, such as sending
     * welcome messages for new conversations.
     */
    fun initialUserTimelineLoaded(userTimeline: UserTimeline) {
    }
}
