package fr.vsct.tock.bot.connector.twitter

import fr.vsct.tock.bot.connector.ConnectorCallbackBase

/**
 * The twitter [ConnectorCallback].
 */
class TwitterConnectorCallback(
    applicationId: String
) : ConnectorCallbackBase(applicationId, twitterConnectorType)