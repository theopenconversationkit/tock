package ai.tock.bot.connector.ga.model.request

data class GAHoldValue(val userDecision: GAHoldStatus?
) : GAArgumentValue(GAArgumentValueType.holdValue
)
