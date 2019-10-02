package ai.tock.bot.connector.ga.model.response.transaction.v3

data class GaItemOption(
    val id: String? = null,
    val name: String? = null,
    val prices: List<GAPriceAttribute>? = emptyList(),
    val note: String? = null,
    val quantity: Int? = null,
    val productId: String?
)