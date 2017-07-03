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

package fr.vsct.tock.bot.admin.message

import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendLocation
import fr.vsct.tock.bot.engine.event.EventType
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserLocation
import fr.vsct.tock.translator.UserInterfaceType
import java.util.Locale

/**
 * User location data.
 */
data class LocationConfiguration(val location: UserLocation?,
                                 override val delay: Long = 0) : MessageConfiguration {

    override val eventType: EventType = EventType.location

    override fun toAction(playerId: PlayerId,
                          applicationId: String,
                          recipientId: PlayerId,
                          locale: Locale,
                          userInterfaceType: UserInterfaceType): Action {
        return SendLocation(
                playerId,
                applicationId,
                recipientId,
                location
        )
    }

}