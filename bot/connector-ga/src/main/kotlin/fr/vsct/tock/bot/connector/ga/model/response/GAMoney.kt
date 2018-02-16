package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/Price#Money
 */
data class GAMoney(
        val currencyCode: String,
        val units: String?,
        val nanos: Long
)