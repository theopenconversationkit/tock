package fr.vsct.tock.bot.connector.ga.model.request

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/SignInStatus
 */
enum class GASignInStatus {
    SIGN_IN_STATUS_UNSPECIFIED,
    OK,
    CANCELLED,
    ERROR
}