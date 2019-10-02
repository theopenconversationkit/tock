package ai.tock.bot.connector.ga.model.response.transaction.v3

import ai.tock.bot.connector.ga.model.response.GAActionType
import ai.tock.bot.connector.ga.model.response.GAOpenUrlAction

/**
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/Order#Action
 */
data class GAActionV3(
        val type: GAActionType,
        val title: String?,
        val openUrlAction: GAOpenUrlAction
)