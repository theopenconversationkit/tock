package ai.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#CancellationInfo
 */
data class GACancellationInfo(
        val reason: String
)