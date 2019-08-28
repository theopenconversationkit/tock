package fr.vsct.tock.bot.connector

enum class ConnectorNotifyStateModifier {
    KEEP_CURRENT_STATE,
    ACTIVATE_ONLY_FOR_THIS_NOTIFICATION,
    REACTIVATE
}