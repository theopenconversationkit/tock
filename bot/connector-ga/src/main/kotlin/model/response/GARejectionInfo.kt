package ai.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#RejectionInfo
 */
data class GARejectionInfo(
        val type: GAReasonType,
        val reason: String
)