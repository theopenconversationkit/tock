package fr.vsct.tock.bot.connector.ga.model.request

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/NewSurfaceStatus
 */
enum class GANewSurfaceStatus {
    OK,// Handoff completed
    CANCELLED, // User denied handoff
    NEW_SURFACE_STATUS_UNSPECIFIED // Unknown status
}