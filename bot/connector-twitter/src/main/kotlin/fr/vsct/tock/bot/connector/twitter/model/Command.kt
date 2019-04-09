package fr.vsct.tock.bot.connector.twitter.model

enum class Command(val command: String) {
    APPEND("APPEND"),
    FINALIZE("FINALIZE"),
    INIT("INIT"),
    STATUS("STATUS")
}