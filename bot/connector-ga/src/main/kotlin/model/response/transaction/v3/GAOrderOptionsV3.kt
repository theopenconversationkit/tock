package ai.tock.bot.connector.ga.model.response.transaction.v3

data class GAOrderOptionsV3(
    val requestDeliveryAddress: Boolean = false,
    val userInfoOptions: GAUserInfoOptions? = null
)