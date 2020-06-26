package ai.tock.bot.connector.googlechat

import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.googlechat.builder.googleChatConnectorType


data class GoogleChatConnectorCallback(
    override val applicationId: String,
    val spaceName: String,
    val threadName: String
) : ConnectorCallbackBase(applicationId, googleChatConnectorType)