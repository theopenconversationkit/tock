/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.tock.bot.connector.googlechat

import ai.tock.bot.connector.ConnectorHandler
import ai.tock.bot.connector.googlechat.builder.GOOGLE_CHAT_CONNECTOR_TYPE_ID
import ai.tock.bot.definition.ConnectorSpecificHandling
import ai.tock.bot.definition.ConnectorStoryHandler
import ai.tock.bot.definition.StoryHandlerDefinition
import kotlin.reflect.KClass

/**
 * To specify [ConnectorStoryHandler] for Google Chat connector.
 * [KClass] passed as [value] of this annotation must have a primary constructor
 * with a single not optional [StoryHandlerDefinition] argument.
 */
@ConnectorHandler(connectorTypeId = GOOGLE_CHAT_CONNECTOR_TYPE_ID)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class GoogleChatHandler(
    val value: KClass<out ConnectorSpecificHandling>,
)
