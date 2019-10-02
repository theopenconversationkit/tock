package ai.tock.bot.connector.ga.model.response.transaction.v3

import ai.tock.bot.connector.ga.model.GAIntent
import ai.tock.bot.connector.ga.model.response.GAInputValueData

/**
 * @see https://https://developers.google.com/actions/transactions/reference/physical/rest/v3/TransactionRequirementsCheckSpec
 */
class GATransactionRequirementsCheckSpecV3 : GAInputValueData(GAIntent.transactionRequirementsCheckV3.type!!)