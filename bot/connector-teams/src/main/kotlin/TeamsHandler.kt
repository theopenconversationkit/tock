package ai.tock.bot.connector.teams

import ai.tock.bot.connector.ConnectorHandler
import ai.tock.bot.definition.ConnectorStoryHandler
import kotlin.reflect.KClass


/**
 * To specify [ConnectorStoryHandler] for Teams connector.
 * [KClass] passed as [value] of this annotation must have a primary constructor
 * with a single not optional [StoryHandlerDefinitionBase] argument.
 */
@ConnectorHandler(connectorTypeId = TEAMS_CONNECTOR_TYPE_ID)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class TeamsHandler(val value: KClass<out ConnectorStoryHandler<*>>)
