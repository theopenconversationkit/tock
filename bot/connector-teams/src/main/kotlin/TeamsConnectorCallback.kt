package ai.tock.bot.connector.teams

import com.microsoft.bot.schema.models.Activity
import ai.tock.bot.connector.ConnectorCallbackBase

class TeamsConnectorCallback(
        applicationId: String,
        val activity: Activity
) : ConnectorCallbackBase(applicationId, teamsConnectorType)