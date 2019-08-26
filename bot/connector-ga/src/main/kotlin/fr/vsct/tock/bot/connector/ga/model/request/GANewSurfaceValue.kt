package fr.vsct.tock.bot.connector.ga.model.request

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/NewSurfaceValue
 */
data class GANewSurfaceValue (
    val status: GANewSurfaceStatus
    ) : GAArgumentValue(
    GAArgumentValueType.newSurfaceValue
    )
