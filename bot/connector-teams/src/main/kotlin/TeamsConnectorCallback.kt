package fr.vsct.tock.bot.connector.teams

import com.microsoft.bot.schema.models.Activity
import fr.vsct.tock.bot.connector.ConnectorCallbackBase

class TeamsConnectorCallback(
        applicationId: String,
        val activity: Activity
) : ConnectorCallbackBase(applicationId, teamsConnectorType)