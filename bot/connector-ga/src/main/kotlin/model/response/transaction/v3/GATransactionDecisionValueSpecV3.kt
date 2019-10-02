package ai.tock.bot.connector.ga.model.response.transaction.v3

import ai.tock.bot.connector.ga.model.GAIntent
import ai.tock.bot.connector.ga.model.response.GAInputValueData

data class GATransactionDecisionValueSpecV3(
    val order: GAOrder,
    val orderOptions: GAOrderOptionsV3?,
    val paymentParameters: GAPaymentParameters?,
    val presentationOptions: GAPresentationOptions?
): GAInputValueData(GAIntent.transactionDecisionV3.type!!)

data class GAPresentationOptions(
    val actionDisplayName: GAActionDisplayName
)

enum class GAActionDisplayName{
    PLACE_ORDER,
    PAY,
    BUY,
    SEND,
    BOOK,
    RESERVE,
    SCHEDULE,
    SUBSCRIBE
}