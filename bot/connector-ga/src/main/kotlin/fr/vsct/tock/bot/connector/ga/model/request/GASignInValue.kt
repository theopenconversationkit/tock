package fr.vsct.tock.bot.connector.ga.model.request

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/ResultType
 */
data class GASignInValue (
    val status: GASignInStatus
    ) : GAArgumentValue(
    GAArgumentValueType.signInValue
    )
