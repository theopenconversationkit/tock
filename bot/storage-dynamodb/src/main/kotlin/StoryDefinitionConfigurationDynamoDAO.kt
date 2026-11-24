/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.storage.dynamodb

import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationRuntimeDAO
import ai.tock.storage.dynamodb.DynamoStore.configTable
import mu.KotlinLogging

/**
 * The stories of a bot: the runtime reads them all at once (see `StoryConfigurationMonitor`), so they live in
 * one partition per bot, `STORY#<namespace>#<botId>`:
 *
 * | sort key        | item                                                                  |
 * |-----------------|-----------------------------------------------------------------------|
 * | `S#<_id>`       | the story                                                             |
 * | `I#<intent>`    | the guard of the Mongo unique index (namespace, botId, intent): its owner |
 *
 * The admin keeps the history of the stories: [save] stores the published version as it is.
 * No change stream: [listenChanges] listens to the publications of the admin ([DynamoChanges]).
 */
object StoryDefinitionConfigurationDynamoDAO : StoryDefinitionConfigurationRuntimeDAO {
    private val logger = KotlinLogging.logger {}

    private const val OWNER = "owner"

    private fun pk(
        namespace: String,
        botId: String,
    ) = "STORY#$namespace#$botId"

    private fun pk(story: StoryDefinitionConfiguration) = pk(story.namespace, story.botId)

    private fun documentKey(story: StoryDefinitionConfiguration) = DynamoStore.key(pk(story), "S#${story._id}")

    private fun guardKey(story: StoryDefinitionConfiguration) = DynamoStore.key(pk(story), "I#${story.intent.name}")

    override fun listenChanges(listener: () -> Unit) = DynamoChanges.listen(DynamoChanges.STORY, listener)

    override fun getStoryDefinitionsByNamespaceAndBotId(
        namespace: String,
        botId: String,
    ): List<StoryDefinitionConfiguration> =
        DynamoStore
            .queryPartition(configTable, pk(namespace, botId), skPrefix = "S#", consistentRead = true)
            .map { DocumentCodec.decode<StoryDefinitionConfiguration>(it.bytes(DOC)) }
            // a namespace or a bot id with a '#' could share the partition of another bot
            .filter { it.namespace == namespace && it.botId == botId }

    override fun getStoryDefinitionByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String,
    ): StoryDefinitionConfiguration? = getStoryDefinitionsByNamespaceAndBotId(namespace, botId).firstOrNull { it.storyId == storyId }

    private fun stored(story: StoryDefinitionConfiguration): StoryDefinitionConfiguration? =
        documentKey(story)
            .let { key ->
                DynamoStore.getItem(configTable, key.getValue(PK).s(), key.getValue(SK).s())
            }?.let { DocumentCodec.decode<StoryDefinitionConfiguration>(it.bytes(DOC)) }

    /**
     * Stores [story], as published by the admin.
     *
     * @throws IllegalArgumentException if another story of the bot has the same intent (the Mongo unique index)
     */
    fun save(story: StoryDefinitionConfiguration) {
        val previous = stored(story)
        val id = story._id.toString()
        val written =
            DynamoStore.transactWrite(
                listOfNotNull(
                    DynamoStore.transactPut(configTable, documentKey(story) + (DOC to b(DocumentCodec.encode(story)))),
                    DynamoStore.transactPut(
                        configTable,
                        guardKey(story) + (OWNER to s(id)),
                        "attribute_not_exists(#pk) OR #owner = :id",
                        mapOf("#pk" to PK, "#owner" to OWNER),
                        mapOf(":id" to s(id)),
                    ),
                    previous
                        ?.takeIf { it.intent.name != story.intent.name }
                        ?.let { DynamoStore.transactDelete(configTable, guardKey(it)) },
                ),
            )
        require(written) { "another story of ${story.namespace}:${story.botId} has the intent ${story.intent.name}" }
    }

    override fun delete(story: StoryDefinitionConfiguration) {
        val stored = stored(story) ?: return
        DynamoStore.batchWrite(configTable, deleteKeys = listOf(documentKey(stored), guardKey(stored)))
    }

    /** Stores the stories whose id and intent are free, as the Mongo DAO does with its unique indexes. */
    override fun createBuiltInStoriesIfNotExist(stories: List<StoryDefinitionConfiguration>) {
        stories.forEach { story ->
            val created =
                DynamoStore.transactWrite(
                    listOf(
                        DynamoStore.transactPut(
                            configTable,
                            documentKey(story) + (DOC to b(DocumentCodec.encode(story))),
                            "attribute_not_exists(#pk)",
                            mapOf("#pk" to PK),
                        ),
                        DynamoStore.transactPut(
                            configTable,
                            guardKey(story) + (OWNER to s(story._id.toString())),
                            "attribute_not_exists(#pk)",
                            mapOf("#pk" to PK),
                        ),
                    ),
                )
            if (!created) logger.trace { "built-in story ${story.storyId} already exists" }
        }
    }
}
