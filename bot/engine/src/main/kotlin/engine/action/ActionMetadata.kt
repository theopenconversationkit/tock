/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.engine.action

data class ActionMetadata(
        /** Is it the last answer of the bot. */
        var lastAnswer: Boolean = false,
        /** Significance deals with the notification level. */
        var priority: ActionPriority = ActionPriority.normal,
        /** tag deals with type of message notification. */
        var notificationType: ActionNotificationType? = null,
        /** metadata dependant from some connectors */
        var connectorMetadata: MutableMap<Metadata, Any> = mutableMapOf()
)