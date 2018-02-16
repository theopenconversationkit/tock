package fr.vsct.tock.bot.connector.ga.model.request

import fr.vsct.tock.bot.connector.ga.request.GAResultType

data class GATransactionDecisionCheckResult(
        val resultType: GAResultType
)