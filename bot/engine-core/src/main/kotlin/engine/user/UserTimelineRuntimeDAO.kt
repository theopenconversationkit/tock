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
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import org.litote.kmongo.Id
import java.time.Instant

/**
 * The part of [UserTimelineDAO] that the bots use at runtime.
 *
 * A storage that only serves the bots (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [UserTimelineDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface UserTimelineRuntimeDAO {
    suspend fun save(
        userTimeline: UserTimeline,
        botDefinition: BotDefinition,
        asynchronousProcess: Boolean = true,
    )

    suspend fun save(
        userTimeline: UserTimeline,
        namespace: String,
    )

    suspend fun updatePlayerId(
        namespace: String,
        oldPlayerId: PlayerId,
        newPlayerId: PlayerId,
    ): Long

    suspend fun loadWithLastValidDialog(
        namespace: String,
        userId: PlayerId,
        priorUserId: PlayerId? = null,
        groupId: String? = null,
        storyDefinitionProvider: (String) -> StoryDefinition,
    ): UserTimeline

    suspend fun loadWithoutDialogs(
        namespace: String,
        userId: PlayerId,
    ): UserTimeline

    suspend fun remove(
        namespace: String,
        playerId: PlayerId,
        clearLock: Boolean = true,
    ): Long

    suspend fun getSnapshots(dialogId: Id<Dialog>): List<Snapshot>

    suspend fun getArchivedEntityValues(
        stateValueId: Id<EntityStateValue>,
        oldActionsMap: Map<Id<Action>, Action> = emptyMap(),
    ): List<ArchivedEntityValue>

    companion object {
        /**
         * The bound [UserTimelineRuntimeDAO], or the bound [UserTimelineDAO] adapted to it.
         */
        fun fromInjector(): UserTimelineRuntimeDAO = injector.provideOrDefault<UserTimelineRuntimeDAO> { injector.provide<UserTimelineDAO>().asRuntimeDAO() }
    }
}

/**
 * Adapts this DAO to [UserTimelineRuntimeDAO].
 */
fun UserTimelineDAO.asRuntimeDAO(): UserTimelineRuntimeDAO = UserTimelineRuntimeDAOAdapter(this)

private class UserTimelineRuntimeDAOAdapter(
    private val dao: UserTimelineDAO,
) : UserTimelineRuntimeDAO {
    override suspend fun save(
        userTimeline: UserTimeline,
        botDefinition: BotDefinition,
        asynchronousProcess: Boolean,
    ) = dao.save(userTimeline, botDefinition, asynchronousProcess)

    override suspend fun save(
        userTimeline: UserTimeline,
        namespace: String,
    ) = dao.save(userTimeline, namespace)

    override suspend fun updatePlayerId(
        namespace: String,
        oldPlayerId: PlayerId,
        newPlayerId: PlayerId,
    ): Long = dao.updatePlayerId(namespace, oldPlayerId, newPlayerId)

    override suspend fun loadWithLastValidDialog(
        namespace: String,
        userId: PlayerId,
        priorUserId: PlayerId?,
        groupId: String?,
        storyDefinitionProvider: (String) -> StoryDefinition,
    ): UserTimeline = dao.loadWithLastValidDialog(namespace, userId, priorUserId, groupId, storyDefinitionProvider)

    override suspend fun loadWithoutDialogs(
        namespace: String,
        userId: PlayerId,
    ): UserTimeline = dao.loadWithoutDialogs(namespace, userId)

    override suspend fun remove(
        namespace: String,
        playerId: PlayerId,
        clearLock: Boolean,
    ): Long = dao.remove(namespace, playerId, clearLock)

    override suspend fun getSnapshots(dialogId: Id<Dialog>): List<Snapshot> = dao.getSnapshots(dialogId)

    override suspend fun getArchivedEntityValues(
        stateValueId: Id<EntityStateValue>,
        oldActionsMap: Map<Id<Action>, Action>,
    ): List<ArchivedEntityValue> = dao.getArchivedEntityValues(stateValueId, oldActionsMap)
}
