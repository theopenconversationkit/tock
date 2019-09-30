package ai.tock.bot.connector.messenger.model.send

import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.message.Choice

data class CallButton(
    val title: String,
    val payload: String
) : Button(ButtonType.phone_number) {

    override fun toChoice(): Choice {
        return Choice(
            SendChoice.PHONE_CALL_INTENT,
            mapOf(
                SendChoice.TITLE_PARAMETER to title,
                SendChoice.PHONE_CALL_INTENT to payload
            )
        )
    }
}