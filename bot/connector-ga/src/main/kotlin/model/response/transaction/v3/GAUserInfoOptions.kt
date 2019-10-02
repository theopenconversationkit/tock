package ai.tock.bot.connector.ga.model.response.transaction.v3

data class GAUserInfoOptions(
    val userInfoProperties: Set<UserInfoProperty>?
)

enum class UserInfoProperty{
    USER_INFO_PROPERTY_UNSPECIFIED,
    EMAIL
}