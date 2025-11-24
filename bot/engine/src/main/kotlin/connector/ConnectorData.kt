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

import ai.tock.bot.engine.user.PlayerId

/**
 * The connector data that connector sends to the [ConnectorController]
 */
open class ConnectorData(
    /**
     * The callback (used mostly in synchronous [Connector]).
     */
    val callback: ConnectorCallback,
    /**
     * The previous user id - used when the user had a "temporary" identifier
     * and the definitive identifier is now known.
     */
    val priorUserId: PlayerId? = null,
    /**
     * To know if the timeline has to be saved - default is true.
     * Set to false for bots that you don't need to register.
     */
    var saveTimeline: Boolean = true,
    /**
     * An optional group id (for users group conversation handling).
     */
    val groupId: String? = null,
    /**
     * An optional referer.
     */
    val referer: String? = null,
    /**
     * optional metadata metadata from connector
     */
    val metadata: Map<String, String> = emptyMap(),
) {
    /**
     * Set to true if the bot does not make any answer to a user sentence.
     * Synchronous [Connector]s usually do not support skipping answer.
     */
    var skipAnswer: Boolean = false
}
