package ai.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/OrderOptions#CustomerInfoOptions
 */
data class GACustomerInfoOptions(
        val customerInfoProperties:Set<GACustomerInfoProperty>
)