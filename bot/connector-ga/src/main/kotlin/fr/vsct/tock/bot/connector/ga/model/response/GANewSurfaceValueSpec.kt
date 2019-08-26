package fr.vsct.tock.bot.connector.ga.model.response

import fr.vsct.tock.bot.connector.ga.model.GAIntent

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/NewSurfaceValueSpec
 */
data class GANewSurfaceValueSpec(
        val capabilities: Set<String>,
        val context: String,
        val notificationTitle: String
): GAInputValueData(GAIntent.newSurface.type!!)