package ai.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/Price
 */
data class GAPrice(
        val type: GAPriceType,
        val amount: GAMoney
)