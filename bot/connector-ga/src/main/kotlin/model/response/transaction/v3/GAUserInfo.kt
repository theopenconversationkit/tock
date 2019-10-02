package ai.tock.bot.connector.ga.model.response.transaction.v3

/**
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/Order#UserInfo
 */
data class GAUserInfo (
    val email : String?,
    val firstName: String?,
    val lastName: String?,
    val displayName: String?,
    val phoneNumbers: List<GAPhoneNumber>?
)

data class GAPhoneNumber (
    val e164PhoneNumber : String?,
    val extension: String?,
    val preferredDomesticCarrierCode: String?
)