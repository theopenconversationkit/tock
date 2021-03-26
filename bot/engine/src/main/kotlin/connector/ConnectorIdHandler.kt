package ai.tock.bot.connector

import ai.tock.bot.definition.ConnectorStoryHandler
import kotlin.reflect.KClass

/**
 * Annotation used to configure [ConnectorIdHandlers],
 * in order to provide [ConnectorStoryHandler] for each connector id.
 */
@MustBeDocumented
annotation class ConnectorIdHandler(val connectorId: String, val value: KClass<out ConnectorStoryHandler<*>>)
