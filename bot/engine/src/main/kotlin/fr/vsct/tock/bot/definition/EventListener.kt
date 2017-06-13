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

package fr.vsct.tock.bot.definition

import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.engine.Bot
import fr.vsct.tock.bot.engine.event.Event

/**
 * To listen custom events (ie not [fr.vsct.tock.bot.engine.action.Action])
 */
interface EventListener {

    /**
     * Listen new event.
     *
     * @param bot the bot used
     * @param connector the connector used
     * @param event the new event
     */
    fun listenEvent(bot: Bot, connector: Connector, event: Event)
}