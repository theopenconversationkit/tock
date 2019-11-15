package ai.tock.bot.connector.ga.model.request

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/ResultType
 */
enum class GAResultType {
    RESULT_TYPE_UNSPECIFIED,

    OK,

    USER_ACTION_REQUIRED,

    ASSISTANT_SURFACE_NOT_SUPPORTED,

    REGION_NOT_SUPPORTED

}