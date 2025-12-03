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

package ai.tock.bot.connector.messenger.model.send

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionPriority

/**
 * cf [https://developers.facebook.com/docs/messenger-platform/send-api-reference#request]
 */
enum class NotificationType {
    REGULAR,
    SILENT_PUSH,
    NO_PUSH,
    ;

    companion object {
        fun toNotificationType(action: Action): NotificationType {
            return when (action.metadata.priority) {
                ActionPriority.high -> SILENT_PUSH
                ActionPriority.urgent -> REGULAR
                else -> NO_PUSH
            }
        }
    }
}
