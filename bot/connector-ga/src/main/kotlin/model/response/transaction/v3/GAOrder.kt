package ai.tock.bot.connector.ga.model.response.transaction.v3

import ai.tock.bot.connector.ga.model.response.GAImage
import ai.tock.bot.connector.ga.model.response.GAMerchant

/**
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/Order
 */
data class GAOrder(
    val googleOrderId: String? = null,
    val merchantOrderId: String,
    val buyerInfo: GAUserInfo? = null,
    val image: GAImage? = null,
    val createTime: String ,
    val lastUpdateTime: String? = null,
    val transactionMerchant: GAMerchant? = null,
    val contents: GAContents,
    val priceAttributes: List<GAPriceAttribute>? = emptyList(),
    val followUpActions: List<GAActionV3>? = emptyList(),
    val termsOfServiceUrl: String? = null,
    val note: String? = null,
    val purchase: GAPurchaseOrderExtension? = null,
    val ticket: GATicketOrderExtension? = null
)


