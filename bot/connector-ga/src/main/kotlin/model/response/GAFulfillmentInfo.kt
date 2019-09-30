package ai.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#FulfillmentInfo
 */
data class GAFulfillmentInfo(
        val deliveryTime: String
)