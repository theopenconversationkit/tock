package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/ProposedOrder#Merchant
 */
data class GAMerchant(
        val id: String,
        val name: String
)