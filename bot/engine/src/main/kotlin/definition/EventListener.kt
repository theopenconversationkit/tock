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

package ai.tock.bot.definition

import ai.tock.bot.connector.ConnectorData
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.event.Event

/**
 * Listen all events - this is the standard process to handle [Event] that are not [Action] (when you need to handle them).
 *
 * You can also intercept [Action]s if useful.
 *
 * To be declared in [BotDefinition.eventListener].
 */
interface EventListener {
    /**
     * Listen new event.
     *
     * @param controller the controller
     * @param connectorData the connector specific data
     * @param event the new event
     * @return true if the event is handled
     */
    fun listenEvent(
        controller: ConnectorController,
        connectorData: ConnectorData,
        event: Event,
    ): Boolean
}
