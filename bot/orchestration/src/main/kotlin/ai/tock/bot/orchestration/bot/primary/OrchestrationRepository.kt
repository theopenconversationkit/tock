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

package ai.tock.bot.orchestration.bot.primary

import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.orchestration.bot.primary.OrchestrationStatus.ACTIVE
import ai.tock.bot.orchestration.bot.primary.OrchestrationStatus.CLOSED
import ai.tock.bot.orchestration.shared.OrchestrationMetaData
import ai.tock.bot.orchestration.shared.OrchestrationTargetedBot
import ai.tock.bot.orchestration.shared.SecondaryBotAction
import ai.tock.shared.TOCK_BOT_DATABASE
import ai.tock.shared.injector
import ai.tock.shared.provide
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.Id
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.push
import org.litote.kmongo.setValue

interface OrchestrationRepository {
    fun create(
        playerId: PlayerId,
        targetMetadata: OrchestrationMetaData,
        target: OrchestrationTargetedBot,
        actions: List<SecondaryBotAction>,
    ): Orchestration

    fun get(playerId: PlayerId): Orchestration?

    fun update(
        id: Id<Orchestration>,
        action: SecondaryBotAction,
    )

    fun end(playerId: PlayerId)
}

object MongoOrchestrationRepository : OrchestrationRepository {
    private val col: MongoCollection<Orchestration> by lazy {

        injector.provide<MongoDatabase>(TOCK_BOT_DATABASE).getCollection<Orchestration>()
            .apply {
                ensureIndex(Orchestration::playerId)
                ensureIndex(Orchestration::playerId, Orchestration::status)
            }
    }

    override fun create(
        playerId: PlayerId,
        targetMetadata: OrchestrationMetaData,
        target: OrchestrationTargetedBot,
        actions: List<SecondaryBotAction>,
    ): Orchestration {
        val orchestration = Orchestration(playerId = playerId, targetMetadata = targetMetadata, targetBot = target, history = actions.toMutableList())
        col.insertOne(orchestration)
        return orchestration
    }

    override fun get(playerId: PlayerId): Orchestration? =
        col.findOne(
            Orchestration::playerId eq playerId,
            Orchestration::status eq ACTIVE,
        )

    override fun update(
        id: Id<Orchestration>,
        action: SecondaryBotAction,
    ) {
        col.updateOne(Orchestration::id eq id, push(Orchestration::history, action))
    }

    override fun end(playerId: PlayerId) {
        col.updateMany(
            Orchestration::playerId eq playerId,
            setValue(Orchestration::status, CLOSED),
        )
    }
}
