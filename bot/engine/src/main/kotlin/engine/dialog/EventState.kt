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

package ai.tock.bot.engine.dialog

import ai.tock.bot.connector.ConnectorType
import ai.tock.translator.UserInterfaceType

/**
 * State in event scope.
 */
data class EventState(
    /**
     * The entity values.
     */
    val entityValues: MutableList<EntityValue> = mutableListOf(),
    /**
     * Is it a "test" event - flag used by automatic tests.
     */
    var testEvent: Boolean = false,
    /**
     * The source connector type.
     */
    var sourceConnectorType: ConnectorType? = null,
    /**
     * The target connector type - usually the source connector but not always.
     */
    var targetConnectorType: ConnectorType? = null,
    /**
     * The user interface - if different of default interface of [ConnectorType].
     */
    var userInterface: UserInterfaceType? = null,
    /**
     * The user verification status.
     */
    var userVerified: Boolean = true,
    /**
     * The current intent of the action.
     */
    var intent: String? = null,
    /**
     * The current step.
     */
    var step: String? = null,
    /**
     * If true, this event is not addressed to the bot, but the bot is notified
     * that it has been sent in a multi users/bots conversation.
     *
     * Default is false.
     */
    var notification: Boolean = false,
    /**
     * For notification event, what is the source of the notification if known?
     */
    var sourceApplicationId: String? = null
) {

    fun getEntity(role: String): List<EntityValue> {
        return entityValues.filter { it.entity.role == role }
    }
}
