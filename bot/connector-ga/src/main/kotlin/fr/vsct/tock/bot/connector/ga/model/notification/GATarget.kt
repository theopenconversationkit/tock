package fr.vsct.tock.bot.connector.ga.model.notification

data class GATarget(
    val userId: String,
    val intent: String,
    val locale: String
)