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

import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.dialog.ArchivedEntityValue
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.EntityStateValue
import fr.vsct.tock.bot.engine.dialog.Snapshot
import org.litote.kmongo.Id
import java.time.Instant

/**
 * To access [UserTimeline]s.
 */
interface UserTimelineDAO {

    /**
     * Saves the timeline.
     *
     * @param userTimeline the timeline to saveTestPlan
     * @param botDefinition the optional bot definition (in order to add stats about the bot)
     */
    fun save(userTimeline: UserTimeline, botDefinition: BotDefinition? = null)

    /**
     * Update playerId for dialog and user timelines.
     */
    fun updatePlayerId(oldPlayerId: PlayerId, newPlayerId: PlayerId)

    /**
     * Loads with last dialog. If no timeline exists, creates a new one.
     *
     * @param userId the user id of the last message
     * @param priorUserId not null if this user ahs an other id before
     * @param groupId not null if this is a conversation group
     * @param storyDefinitionProvider provides [StoryDefinition] from story ids.
     */
    fun loadWithLastValidDialog(
        userId: PlayerId,
        priorUserId: PlayerId? = null,
        groupId: String? = null,
        storyDefinitionProvider: (String) -> StoryDefinition
    ): UserTimeline

    /**
     * Loads without the dialogs. If no timeline, create a new one.
     */
    fun loadWithoutDialogs(userId: PlayerId): UserTimeline

    /**
     * Loads without the dialogs.
     */
    fun loadByTemporaryIdsWithoutDialogs(temporaryIds: List<String>): List<UserTimeline>

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
    fun getClientDialogs(
        clientId: String,
        storyDefinitionProvider: (String) -> StoryDefinition
    ): List<Dialog>

    /**
     * Returns all dialogs updated after the specified Instant.
     */
    fun getDialogsUpdatedFrom(from: Instant, storyDefinitionProvider: (String) -> StoryDefinition): List<Dialog>

    /**
     * Gets the snapshots of a dialog.
     */
    fun getSnapshots(dialogId: Id<Dialog>): List<Snapshot>

    /**
     * Returns the last story id of the specified user, if any.
     */
    fun getLastStoryId(playerId: PlayerId): String?

    /**
     * Returns the archived values for the state id.
     *
     * @param stateValueId the state id
     * @param oldActionsMap the option action map in order to retrieve the action of archived entity values.
     */
    fun getArchivedEntityValues(
        stateValueId: Id<EntityStateValue>,
        oldActionsMap: Map<Id<Action>, Action> = emptyMap()
    ): List<ArchivedEntityValue>
}
