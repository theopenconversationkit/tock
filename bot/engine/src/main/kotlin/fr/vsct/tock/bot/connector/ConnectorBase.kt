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

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.StoryHandlerDefinition
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.injector

/**
 * Base implementation of [Connector]. Connector implementations should usually extend this class.
 */
abstract class ConnectorBase(override val connectorType: ConnectorType) : Connector {
    private val userTimelineDAO: UserTimelineDAO by injector.instance()
    /**
     * Returns [connectorType.toString] method.
     */
    override fun toString(): String = "Connector($connectorType)"

    final override fun notify(
        controller: ConnectorController,
        recipientId: PlayerId,
        intent: IntentAware,
        step: StoryStep<out StoryHandlerDefinition>?,
        parameters: Map<String, String>,
        stateModifier: ConnectorNotifyStateModifier
    ) {
        val userTimeline = userTimelineDAO.loadWithoutDialogs(recipientId)
        val userState = userTimeline.userState
        val currentState = userState.botDisabled

        if(stateModifier == ConnectorNotifyStateModifier.ACTIVATE_ONLY_FOR_THIS_NOTIFICATION
            || stateModifier == ConnectorNotifyStateModifier.REACTIVATE) {
            userState.botDisabled = false
            userTimelineDAO.save(userTimeline)
        }

        notify(controller, recipientId, intent, step, parameters)

        if(stateModifier == ConnectorNotifyStateModifier.ACTIVATE_ONLY_FOR_THIS_NOTIFICATION) {
            val userTimelineAfterNotification = userTimelineDAO.loadWithoutDialogs(recipientId)
            userTimelineAfterNotification.userState.botDisabled = currentState
            userTimelineDAO.save(userTimeline)
        }
    }
}