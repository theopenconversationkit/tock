package ai.tock.bot.connector

/**
 * Annotation used to annotate [StoryHandlerDefinitionBase] implementation,
 * in order to provide [ConnectorStoryHandler] for each connector id.
 * Used only by connector implementation.
 */
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class ConnectorIdHandlers(vararg val handlers: ConnectorIdHandler)