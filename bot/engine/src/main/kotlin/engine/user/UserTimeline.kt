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

package ai.tock.bot.engine.user

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.Story

/**
 * The user timeline - all dialogs and data of the user.
 */
class UserTimeline(
    /**
     * The user id.
     */
    val playerId: PlayerId,
    /**
     * User data, first name, email, etc.
     */
    val userPreferences: UserPreferences = UserPreferences(),
    /**
     * The user state, with simple flags.
     */
    val userState: UserState = UserState(),
    /**
     * The dialogs of the timeline.
     */
    val dialogs: MutableList<Dialog> = mutableListOf(),
    /**
     * Temporary ids (of type [PlayerType.temporary] linked to this user timeline.
     */
    val temporaryIds: MutableSet<String> = mutableSetOf(),
) {
    /**
     * Returns the current dialog.
     */
    val currentDialog: Dialog? get() = dialogs.lastOrNull()

    /**
     * Returns the current story.
     */
    val currentStory: Story? get() = currentDialog?.currentStory

    /**
     * Does this timeline has at least one answer of a bot?
     */
    fun containsBotAction(): Boolean {
        return dialogs.any {
            it.stories.any {
                it.actions.any {
                    it.playerId.type == PlayerType.bot
                }
            }
        }
    }

    /**
     * Last action if any.
     */
    val lastAction: Action? get() = dialogs.findLast { it.lastAction != null }?.lastAction

    /**
     * Last user action if any.
     */
    val lastUserAction: Action? get() = dialogs.findLast { it.lastUserAction != null }?.lastUserAction

    override fun toString(): String {
        return "UserTimeline(playerId=$playerId)"
    }
}
