package ai.tock.bot.connector.ga.model.response.transaction.v3

import ai.tock.bot.connector.ga.model.request.transaction.v3.GAPaymentType
import ai.tock.bot.connector.ga.model.response.GAStatusCode

data class GAPaymentParameters(
    val merchantPaymentOption: GAMerchantPaymentOption?
)

data class GAMerchantPaymentOption(
    val merchantPaymentMethod: List<GAMerchantPaymentMethod>,
    val defaultMerchantPaymentMethodId: String?,
    val managePaymentMethodUrl: String?
)

data class GAMerchantPaymentMethod(
    val paymentMethodGroup: String?,
    val paymentMethodId: String?,
    val paymentMethodDisplayInfo: GAPaymentMethodDisplayInfo,
    val paymentMethodStatus: GAPaymentMethodStatus?
)

data class GAPaymentMethodDisplayInfo(
    val paymentType: GAPaymentType?,
    val paymentMethodDisplayName: String?
)

data class GAPaymentMethodStatus(
    val status: GAStatusCode?,
    val statusMessage: String?
)

enum class GAPaymentMethodStatusValue{
    STATUS_UNSPECIFIED,
    STATUS_OK,
    STATUS_REQUIRE_FIX,
    STATUS_INAPPLICABLE
}