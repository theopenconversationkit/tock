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

package fr.vsct.tock.bot.engine.dialog

import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.provide
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

/**
 * A dialog is a conversation between users and bots.
 * Conversation history is split into a list of [stories].
 * The dialog has a (current) [state].
 */
data class Dialog(
    val playerIds: Set<PlayerId>,
    var id: Id<Dialog> = newId(),
    val state: DialogState = DialogState(),
    val stories: MutableList<Story> = mutableListOf()
) {

    /**
     * The last update date.
     */
    val lastDateUpdate: Instant get() = stories.lastOrNull()?.lastAction?.date ?: Instant.now()

    /**
     * The current story if any.
     */
    val currentStory: Story? get() = stories.lastOrNull()

    /**
     * All old actions.
     */
    fun allActions(): List<Action> = stories.flatMap { it.actions }

    /**
     * The [Snapshots] of the dialog.
     */
    val snapshots: List<Snapshot> by lazy { injector.provide<UserTimelineDAO>().getSnapshots(id) }
}