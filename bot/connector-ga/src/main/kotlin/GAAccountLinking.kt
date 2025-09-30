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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.ga.model.request.GARequest
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.shared.injector
import ai.tock.shared.provide

internal class GAAccountLinking {

    companion object {

        private val userTimelineDAO: UserTimelineDAO get() = injector.provide()

        internal fun getUserId(message: GARequest) =
            message.user.accessToken?.split("|")?.get(0) ?: message.conversation.conversationId

        internal fun isUserAuthenticated(message: GARequest) = message.user.accessToken != null

        internal suspend fun switchTimeLine(applicationId: String, newLoggedUserId: PlayerId, oldUserId: PlayerId, controller: ConnectorController) {
            val oldTimeline = userTimelineDAO.loadWithLastValidDialog(
                controller.botDefinition.namespace,
                oldUserId,
                storyDefinitionProvider = controller.storyDefinitionLoader(applicationId)
            )
            val newTimeline = UserTimeline(
                newLoggedUserId,
                oldTimeline.userPreferences,
                oldTimeline.userState,
                oldTimeline.dialogs.map {
                    it.copy(
                        playerIds = it.playerIds.filter { playerId ->
                            playerId.type != PlayerType.user
                        }.toSet() + newLoggedUserId
                    )
                }.toMutableList(),
                oldTimeline.temporaryIds
            )
            userTimelineDAO.save(newTimeline, controller.botDefinition)
        }
    }
}
