package ai.tock.bot.connector.teams.messages

import ai.tock.bot.connector.teams.teamsConnectorType
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence

internal object SendActionConverter {

    fun toActivity(action: Action): TeamsBotMessage {
        return if (action is SendSentence) {
            action.message(teamsConnectorType) as? TeamsBotMessage ?: TeamsBotTextMessage(action.stringText ?: "")
        } else TeamsBotTextMessage("")
    }
}