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

import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.ArchivedEntityValue
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.EntityStateValue
import ai.tock.bot.engine.dialog.Snapshot
import org.litote.kmongo.Id
import java.time.Instant

/**
 * To access [UserTimeline]s.
 */
interface UserTimelineDAO {

    /**
     * Saves the timeline.
     *
     * @param userTimeline the timeline to save
     * @param botDefinition the bot definition (in order to add stats about the bot)
     * @param asynchronousProcess boolean to disable/enable asynchronous saving
     */
    suspend fun save(userTimeline: UserTimeline, botDefinition: BotDefinition, asynchronousProcess: Boolean = true)

    /**
     * Saves the timeline.
     *
     * @param userTimeline the timeline to save
     * @param namespace the namespace of the current bot
     */
    suspend fun save(userTimeline: UserTimeline, namespace: String)

    /**
     * Update playerId for dialog and user timelines.
     */
    suspend fun updatePlayerId(namespace: String, oldPlayerId: PlayerId, newPlayerId: PlayerId)

    /**
     * Loads with last dialog. If no timeline exists, creates a new one.
     *
     * @param namespace the namespace of the bot
     * @param userId the user id of the last message
     * @param priorUserId not null if this user has an other id before
     * @param groupId not null if this is a conversation group
     * @param storyDefinitionProvider provides [StoryDefinition] from story ids.
     */
    suspend fun loadWithLastValidDialog(
        namespace: String,
        userId: PlayerId,
        priorUserId: PlayerId? = null,
        groupId: String? = null,
        storyDefinitionProvider: (String) -> StoryDefinition
    ): UserTimeline

    /**
     * Loads without the dialogs. If no timeline, create a new one.
     */
    suspend fun loadWithoutDialogs(namespace: String, userId: PlayerId): UserTimeline

    /**
     * Loads without the dialogs.
     */
    suspend fun loadByTemporaryIdsWithoutDialogs(namespace: String, temporaryIds: List<String>): List<UserTimeline>

    /**
     * Remove the timeline and the associated dialogs.
     */
    suspend fun remove(namespace: String, playerId: PlayerId)

    /**
     * Remove all timelines and associated dialogs of a client.
     */
    suspend fun removeClient(namespace: String, clientId: String)

    /**
     * Returns the dialogs of specified client id.
     */
    suspend fun getClientDialogs(
        namespace: String,
        clientId: String,
        storyDefinitionProvider: (String) -> StoryDefinition
    ): List<Dialog>

    /**
     * Returns all dialogs updated after the specified Instant.
     */
    suspend fun getDialogsUpdatedFrom(
        namespace: String,
        from: Instant,
        storyDefinitionProvider: (String) -> StoryDefinition
    ): List<Dialog>

    /**
     * Gets the snapshots of a dialog.
     */
    suspend fun getSnapshots(dialogId: Id<Dialog>): List<Snapshot>

    /**
     * Returns the last story id of the specified user, if any.
     */
    suspend fun getLastStoryId(namespace: String, playerId: PlayerId): String?

    /**
     * Returns the archived values for the state id.
     *
     * @param stateValueId the state id
     * @param oldActionsMap the option action map in order to retrieve the action of archived entity values.
     */
    suspend fun getArchivedEntityValues(
        stateValueId: Id<EntityStateValue>,
        oldActionsMap: Map<Id<Action>, Action> = emptyMap()
    ): List<ArchivedEntityValue>
}
