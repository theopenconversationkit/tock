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

import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.Snapshot
import org.litote.kmongo.Id
import java.time.Instant

/**
 * To access [UserTimeline]s.
 */
interface UserTimelineDAO {

    /**
     * Save the timeline.
     */
    fun save(userTimeline: UserTimeline)

    /**
     * Load with last dialog. If no timeline exists, creates a new one.
     */
    fun loadWithLastValidDialog(userId: PlayerId, storyDefinitionProvider: (String) -> StoryDefinition): UserTimeline

    /**
     * Load without the dialogs. If no timeline, create a new one.
     */
    fun loadWithoutDialogs(userId: PlayerId): UserTimeline

    /**
     * Remove the timeline and the associated dialogs.
     */
    fun remove(playerId: PlayerId)

    /**
     * Remove all timelines and associated dialogs of a client.
     */
    fun removeClient(clientId: String)

    /**
     * Returns the dialogs of specified client id.
     */
    fun getClientDialogs(clientId: String,
                         storyDefinitionProvider: (String) -> StoryDefinition): List<Dialog>

    /**
     * Returns all dialogs updated after the specified Instant.
     */
    fun getDialogsUpdatedFrom(from: Instant, storyDefinitionProvider: (String) -> StoryDefinition): List<Dialog>

    /**
     * Get the snapshots of a dialog.
     */
    fun getSnapshots(dialogId: Id<Dialog>): List<Snapshot>
}
