package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/OrderOptions
 */
data class GAOrderOptions(
        val requestDeliveryAddress:Boolean = false,
        val customerInfoOptions: GACustomerInfoOptions?
)