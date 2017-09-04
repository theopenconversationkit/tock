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

package fr.vsct.tock.bot.connector

import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserPreferences

/**
 *
 */
interface Connector {

    /**
     * The type of the connector
     */
    val connectorType: ConnectorType

    /**
     * Register the connector for the specified controller.
     * There is usually one controller by application.
     */
    fun register(controller: ConnectorController)

    /**
     * Send an event with this connector.
     */
    fun send(event: Event)

    /**
     * Send an event with the specified delay. Default implementation is #send(Event).
     */
    fun send(event: Event, delayInMs: Long) = send(event)

    /**
     * Load user preferences - default implementation returns null.
     */
    fun loadProfile(applicationId: String, userId: PlayerId): UserPreferences? {
        //default implementation returns null
        return null
    }

}