package fr.vsct.tock.bot.connector.ga.model.response

enum class GAState {
    CREATED,
    REJECTED,
    CONFIRMED,
    CANCELLED,
    IN_TRANSIT,
    RETURNED,
    FULFILLED,
    CHANGE_REQUESTED
}