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

package ai.tock.bot.mongo

import ai.tock.bot.admin.annotation.BotAnnotation
import ai.tock.bot.admin.dialog.ActionReport
import ai.tock.bot.admin.dialog.DialogReport
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.Footnote
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendDebug
import ai.tock.bot.engine.action.SendLocation
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.action.SendSentenceWithFootnotes
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.DialogState
import ai.tock.bot.engine.dialog.EntityStateValue
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserLocation
import ai.tock.shared.checkMaxLengthAllowed
import ai.tock.shared.jackson.AnyValueWrapper
import ai.tock.shared.security.TockObfuscatorService.obfuscate
import ai.tock.shared.transformData
import ai.tock.translator.UserInterfaceType.textChat
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kotlinx.coroutines.runBlocking
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant
import java.time.Instant.now

/**
 *
 */
@Data(internal = true)
@JacksonData(internal = true)
internal data class DialogCol(
    val playerIds: Set<PlayerId>,
    var _id: Id<Dialog>,
    val state: DialogStateMongoWrapper,
    val stories: List<StoryMongoWrapper>,
    val applicationIds: Set<String> = emptySet(),
    val lastUpdateDate: Instant = now(),
    val groupId: String? = null,
    val test: Boolean = false,
    val namespace: String? = null,
    val rating: Int? = null,
    val review: String? = null,
) {
    companion object {
        private fun getActionWrapper(action: Action): ActionMongoWrapper {
            return when (action) {
                is SendSentence -> SendSentenceMongoWrapper(action)
                is SendSentenceWithFootnotes -> SendSentenceWithFootnotesMongoWrapper(action)
                is SendDebug -> SendDebugMongoWrapper(action)
                is SendChoice -> SendChoiceMongoWrapper(action)
                is SendAttachment -> SendAttachmentMongoWrapper(action)
                is SendLocation -> SendLocationMongoWrapper(action)
                else -> error("action type not supported : $action")
            }
        }
    }

    constructor(dialog: Dialog, userTimeline: UserTimelineCol) : this(
        dialog.playerIds,
        dialog.id,
        DialogStateMongoWrapper(dialog.state),
        dialog.stories.map { StoryMongoWrapper(it) },
        userTimeline.applicationIds,
        groupId = dialog.groupId,
        test = userTimeline.userPreferences.test,
        namespace = userTimeline.namespace,
        review = dialog.review,
        rating = dialog.rating,
    )

    fun toDialog(storyDefinitionProvider: (String) -> StoryDefinition): Dialog {
        return stories.map { it.toStory(_id, storyDefinitionProvider) }.let { stories ->
            Dialog(
                playerIds,
                _id,
                state.toState(stories.flatMap { it.actions }.map { it.toActionId() to it }.toMap()),
                stories.toMutableList(),
                groupId = groupId,
                rating = rating,
                review = review,
            )
        }
    }

    fun toDialogReport(): DialogReport {
        return DialogReport(
            stories
                .flatMap { it.actions }
                .asSequence()
                .distinctBy { it.id }
                .map { it.toAction(_id) }
                .toList()
                .run {
                    val customMessagesMap =
                        runBlocking {
                            UserTimelineMongoDAO.loadConnectorMessages(
                                mapNotNull {
                                    (it as? SendSentenceNotYetLoaded)?.let {
                                        ConnectorMessageColId(
                                            it.toActionId(),
                                            it.dialogId,
                                        )
                                    }
                                },
                            )
                        }

                    map { a ->
                        if (a is SendSentenceNotYetLoaded) {
                            a.setLoadedMessages(
                                customMessagesMap[ConnectorMessageColId(a.toActionId(), a.dialogId)]
                                    ?: emptyList(),
                            )
                        }
                        ActionReport(
                            a.playerId,
                            a.recipientId,
                            a.date,
                            a.toMessage(),
                            a.state.targetConnectorType,
                            a.state.userInterface ?: textChat,
                            a.state.testEvent,
                            a.toActionId(),
                            a.state.intent,
                            a.applicationId,
                            a.metadata,
                            a.annotation,
                        )
                    }
                }
                .toList(),
            stories
                .flatMap { it.actions }
                .firstOrNull { it.state.userInterface != null }
                ?.state?.userInterface
                ?: textChat,
            _id,
            rating = rating,
            review = review,
        )
    }

    data class DialogStateMongoWrapper(
        var currentIntent: Intent?,
        @JsonDeserialize(contentAs = EntityStateValueWrapper::class)
        val entityValues: Map<String, EntityStateValueWrapper>,
        @JsonDeserialize(contentAs = AnyValueWrapper::class)
        val context: Map<String, AnyValueWrapper?>,
        var userLocation: UserLocation?,
        var nextActionState: NextUserActionState?,
    ) {
        constructor(state: DialogState) : this(
            state.currentIntent,
            state.entityValues.mapValues { EntityStateValueWrapper(it.value) },
            state.context.map { e -> e.key to AnyValueWrapper(e.value) }.toMap(),
            state.userLocation,
            state.nextActionState,
        )

        fun toState(actionsMap: Map<Id<Action>, Action>): DialogState {
            return DialogState(
                currentIntent,
                entityValues.mapValues { it.value.toEntityStateValue(actionsMap) }.toMutableMap(),
                context.filter { it.value != null && it.value!!.value != null }.mapValues { it.value!!.value!! }
                    .toMutableMap(),
                userLocation,
                nextActionState,
            )
        }
    }

    data class EntityStateValueWrapper(
        val value: EntityValue?,
        val lastUpdate: Instant = now(),
        val id: Id<EntityStateValue> = newId(),
    ) {
        constructor(value: EntityStateValue) : this(
            value.value,
            value.lastUpdate,
            value.stateValueId ?: newId(),
        )

        fun toEntityStateValue(actionsMap: Map<Id<Action>, Action>): EntityStateValue {
            return EntityStateValue(
                value,
                mutableListOf(),
                lastUpdate,
                id,
                actionsMap,
            )
        }
    }

    @Data(internal = true)
    @JacksonData(internal = true)
    data class StoryMongoWrapper(
        val storyDefinitionId: String,
        var currentIntent: Intent?,
        val currentStep: String?,
        val actions: List<ActionMongoWrapper>,
    ) {
        constructor(story: Story) : this(
            story.definition.id,
            story.starterIntent,
            story.currentStep?.name,
            story.actions.map { getActionWrapper(it) },
        )

        fun toStory(
            dialogId: Id<Dialog>,
            storyDefinitionProvider: (String) -> StoryDefinition,
        ): Story {
            return Story(
                storyDefinitionProvider.invoke(storyDefinitionId),
                currentIntent ?: Intent.unknown,
                currentStep,
                actions.map { it.toAction(dialogId) }.toMutableList(),
            )
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(value = SendSentenceMongoWrapper::class, name = "sentence"),
        JsonSubTypes.Type(value = SendSentenceWithFootnotesMongoWrapper::class, name = "sentenceWithFootnotes"),
        JsonSubTypes.Type(value = SendChoiceMongoWrapper::class, name = "choice"),
        JsonSubTypes.Type(value = SendAttachmentMongoWrapper::class, name = "attachment"),
        JsonSubTypes.Type(value = SendLocationMongoWrapper::class, name = "location"),
        JsonSubTypes.Type(value = SendDebugMongoWrapper::class, name = "debug"),
    )
    @Data(internal = true)
    abstract class ActionMongoWrapper {
        lateinit var id: Id<Action>
        lateinit var date: Instant
        lateinit var state: EventState
        lateinit var botMetadata: ActionMetadata
        lateinit var playerId: PlayerId
        lateinit var recipientId: PlayerId
        lateinit var applicationId: String
        var annotation: BotAnnotation? = null

        fun assignFrom(action: Action) {
            id = action.toActionId()
            date = action.date
            state = action.state
            botMetadata = action.metadata
            playerId = action.playerId
            recipientId = action.recipientId
            applicationId = action.applicationId
            annotation = action.annotation
        }

        abstract fun toAction(dialogId: Id<Dialog>): Action
    }

    @JsonTypeName(value = "sentence")
    data class SendSentenceMongoWrapper(
        val text: String?,
        val customMessage: Boolean = false,
        val nlpStats: Boolean = false,
    ) : ActionMongoWrapper() {
        constructor(sentence: SendSentence) :
            this(
                sentence.stringText?.let {
                    checkMaxLengthAllowed(it)
                },
                (sentence as? SendSentenceNotYetLoaded)?.hasCustomMessage ?: sentence.messages.isNotEmpty(),
                (sentence as? SendSentenceNotYetLoaded)?.hasNlpStats ?: sentence.nlpStats != null,
            ) {
            assignFrom(sentence)
        }

        override fun toAction(dialogId: Id<Dialog>): Action {
            return if (customMessage || nlpStats || annotation != null) {
                SendSentenceNotYetLoaded(
                    dialogId,
                    playerId,
                    applicationId,
                    recipientId,
                    text,
                    id,
                    date,
                    state,
                    botMetadata,
                    customMessage,
                    nlpStats,
                    annotation,
                )
            } else {
                SendSentence(
                    playerId,
                    applicationId,
                    recipientId,
                    text,
                    mutableListOf(),
                    id,
                    date,
                    state,
                    botMetadata,
                )
            }
        }
    }

    @JsonTypeName(value = "sentenceWithFootnotes")
    data class SendSentenceWithFootnotesMongoWrapper(
        val text: String,
        val footnotes: MutableList<Footnote>,
    ) : ActionMongoWrapper() {
        constructor(sentence: SendSentenceWithFootnotes) :
            this(
                sentence.text.toString(),
                sentence.footnotes,
            ) {
            assignFrom(sentence)
        }

        override fun toAction(dialogId: Id<Dialog>): Action {
            return SendSentenceWithFootnotes(
                playerId,
                applicationId,
                recipientId,
                text,
                footnotes,
                id,
                date,
                state,
                botMetadata,
                annotation,
            )
        }
    }

    @JsonTypeName(value = "debug")
    data class SendDebugMongoWrapper(
        val text: String,
        val data: Any?,
    ) : ActionMongoWrapper() {
        constructor(debug: SendDebug) :
            this(
                debug.text,
                transformData(debug.data),
            ) {
            assignFrom(debug)
        }

        override fun toAction(dialogId: Id<Dialog>): Action {
            return SendDebug(
                playerId,
                applicationId,
                recipientId,
                text,
                transformData(data),
                id,
                date,
                state,
                botMetadata,
            )
        }
    }

    @JsonTypeName(value = "choice")
    class SendChoiceMongoWrapper private constructor(
        val intentName: String,
        val parameters: Map<String, String>,
    ) : ActionMongoWrapper() {
        constructor(choice: SendChoice) :
            this(
                choice.intentName,
                if (choice.state.testEvent) choice.parameters else obfuscate(choice.parameters),
            ) {
            assignFrom(choice)
        }

        override fun toAction(dialogId: Id<Dialog>): Action {
            return SendChoice(
                playerId,
                applicationId,
                recipientId,
                intentName,
                parameters,
                id,
                date,
                state,
                botMetadata,
            )
        }
    }

    @JsonTypeName(value = "attachment")
    class SendAttachmentMongoWrapper(
        val url: String,
        @JsonProperty("attachment_type")
        @JsonAlias("type")
        val type: SendAttachment.AttachmentType,
    ) : ActionMongoWrapper() {
        constructor(attachment: SendAttachment) : this(attachment.url, attachment.type) {
            assignFrom(attachment)
        }

        override fun toAction(dialogId: Id<Dialog>): Action {
            return SendAttachment(
                playerId,
                applicationId,
                recipientId,
                url,
                type,
                id,
                date,
                state,
                botMetadata,
            )
        }
    }

    @JsonTypeName(value = "location")
    class SendLocationMongoWrapper(val location: UserLocation?) : ActionMongoWrapper() {
        constructor(location: SendLocation) : this(location.location) {
            assignFrom(location)
        }

        override fun toAction(dialogId: Id<Dialog>): Action {
            return SendLocation(
                playerId,
                applicationId,
                recipientId,
                location,
                id,
                date,
                state,
                botMetadata,
            )
        }
    }
}

@Data(internal = true)
@JacksonData(internal = true)
data class ParseRequestSatisfactionStatCol(
    val rating: Double,
    val count: Int = 1,
)
