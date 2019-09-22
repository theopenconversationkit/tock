package fr.vsct.tock.bot.connector.ga

import fr.vsct.tock.bot.connector.ga.model.request.GARequest
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.provide

internal class GAAccountLinking {

    companion object {

        private val userTimelineDAO: UserTimelineDAO get() = injector.provide()

        internal fun getUserId(message: GARequest) =
            message.user.accessToken?.split("|")?.get(0) ?: message.conversation.conversationId

        internal fun isUserAuthenticated(message: GARequest) = message.user.accessToken != null

        internal fun switchTimeLine(newLoggedUserId: PlayerId, oldUserId: PlayerId, controller: ConnectorController) {
            val oldTimeline = userTimelineDAO.loadWithLastValidDialog(
                oldUserId,
                storyDefinitionProvider = controller.storyDefinitionLoader())
            val newTimeline = UserTimeline(
                newLoggedUserId,
                oldTimeline.userPreferences,
                oldTimeline.userState,
                oldTimeline.dialogs.map {
                    it.copy(playerIds = it.playerIds.filter { playerId ->
                        playerId.type != PlayerType.user
                    }.toSet() + newLoggedUserId)
                }.toMutableList(),
                oldTimeline.temporaryIds
            )
            userTimelineDAO.save(newTimeline)
        }

    }


}
