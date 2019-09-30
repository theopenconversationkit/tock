package ai.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#UserNotification
 */
data class GAUserNotification(
        val title: String,
        val text: String
)