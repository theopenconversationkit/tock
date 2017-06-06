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

package fr.vsct.tock.bot.engine.user

import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.Story

/**
 * The user timeline - all dialogs and data of the user.
 */
class UserTimeline(val playerId: PlayerId,
                   val userPreferences: UserPreferences = UserPreferences(),
                   val userState: UserState = UserState(),
                   val dialogs: MutableList<Dialog> = mutableListOf()) {

    /**
     * Returns the current dialog.
     */
    fun currentDialog(): Dialog? = dialogs.lastOrNull()

    /**
     * Returns the current story.
     */
    fun currentStory(): Story? = currentDialog()?.currentStory()

    /**
     * Does this timeline as at least one action of the bot?
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
}