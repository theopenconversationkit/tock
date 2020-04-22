/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import ai.tock.bot.admin.dialog.ActionReport
import ai.tock.bot.admin.dialog.DialogReport
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendLocation
import ai.tock.bot.engine.action.SendSentence
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
import ai.tock.translator.UserInterfaceType.textChat
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
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
    val namespace: String? = null
) {

    companion object {
        private fun getActionWrapper(action: Action): ActionMongoWrapper {
            return when (action) {
                is SendSentence -> SendSentenceMongoWrapper(action)
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
        namespace = userTimeline.namespace
    )

    fun toDialog(storyDefinitionProvider: (String) -> StoryDefinition): Dialog {
        return stories.map { it.toStory(_id, storyDefinitionProvider) }.let { stories ->
            Dialog(
                playerIds,
                _id,
                state.toState(stories.flatMap { it.actions }.map { it.toActionId() to it }.toMap()),
                stories.toMutableList(),
                groupId = groupId
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
                .map {
                    ActionReport(
                        it.playerId,
                        it.recipientId,
                        it.date,
                        it.toMessage(),
                        it.state.targetConnectorType,
                        it.state.userInterface ?: textChat,
                        it.state.testEvent,
                        it.toActionId()
                    )
                }
                .toList(),
            stories
                .flatMap { it.actions }
                .firstOrNull { it.state.userInterface != null }
                ?.state?.userInterface
                ?: textChat,
            _id
        )
    }

    data class DialogStateMongoWrapper(
        var currentIntent: Intent?,
        @JsonDeserialize(contentAs = EntityStateValueWrapper::class)
        val entityValues: Map<String, EntityStateValueWrapper>,
        @JsonDeserialize(contentAs = AnyValueWrapper::class)
        val context: Map<String, AnyValueWrapper?>,
        var userLocation: UserLocation?,
        var nextActionState: NextUserActionState?
    ) {


        constructor(state: DialogState) : this(
            state.currentIntent,
            state.entityValues.mapValues { EntityStateValueWrapper(it.value) },
            state.context.map { e -> e.key to AnyValueWrapper(e.value) }.toMap(),
            state.userLocation,
            state.nextActionState
        )

        fun toState(actionsMap: Map<Id<Action>, Action>): DialogState {
            return DialogState(
                currentIntent,
                entityValues.mapValues { it.value.toEntityStateValue(actionsMap) }.toMutableMap(),
                context.filter { it.value != null && it.value!!.value != null }.mapValues { it.value!!.value!! }.toMutableMap(),
                userLocation,
                nextActionState
            )
        }

    }

    data class EntityStateValueWrapper(
        val value: EntityValue?,
        val lastUpdate: Instant = now(),
        val id: Id<EntityStateValue> = newId()
    ) {

        constructor(value: EntityStateValue) : this(
            value.value,
            value.lastUpdate,
            value.stateValueId ?: newId()
        )

        fun toEntityStateValue(actionsMap: Map<Id<Action>, Action>): EntityStateValue {
            return EntityStateValue(
                value,
                mutableListOf(),
                lastUpdate,
                id,
                actionsMap
            )
        }
    }

    @Data(internal = true)
    @JacksonData(internal = true)
    data class StoryMongoWrapper(
        val storyDefinitionId: String,
        var currentIntent: Intent?,
        val currentStep: String?,
        val actions: List<ActionMongoWrapper>
    ) {

        constructor(story: Story) : this(
            story.definition.id,
            story.starterIntent,
            story.currentStep?.name,
            story.actions.map { getActionWrapper(it) })

        fun toStory(dialogId: Id<Dialog>, storyDefinitionProvider: (String) -> StoryDefinition): Story {
            return Story(
                storyDefinitionProvider.invoke(storyDefinitionId),
                currentIntent ?: Intent.unknown,
                currentStep,
                actions.map { it.toAction(dialogId) }.toMutableList()
            )
        }


    }


    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(value = SendSentenceMongoWrapper::class, name = "sentence"),
        JsonSubTypes.Type(value = SendChoiceMongoWrapper::class, name = "choice"),
        JsonSubTypes.Type(value = SendAttachmentMongoWrapper::class, name = "attachment"),
        JsonSubTypes.Type(value = SendLocationMongoWrapper::class, name = "location")
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


        fun assignFrom(action: Action) {
            id = action.toActionId()
            date = action.date
            state = action.state
            botMetadata = action.metadata
            playerId = action.playerId
            recipientId = action.recipientId
            applicationId = action.applicationId
        }

        abstract fun toAction(dialogId: Id<Dialog>): Action
    }

    @JsonTypeName(value = "sentence")
    data class SendSentenceMongoWrapper(
        val text: String?,
        val customMessage: Boolean = false
    ) : ActionMongoWrapper() {

        constructor(sentence: SendSentence) :
            this(
                sentence.stringText?.let {
                    val text = checkMaxLengthAllowed(it)
                    //TODO obfuscate only when viewing - see #862
                    if (sentence.state.testEvent) text else obfuscate(text)
                },
                sentence is SendSentenceWithNotLoadedMessage || sentence.messages.isNotEmpty()
            ) {
            assignFrom(sentence)
        }

        override fun toAction(dialogId: Id<Dialog>): Action {
            return if (customMessage) {
                SendSentenceWithNotLoadedMessage(
                    dialogId,
                    playerId,
                    applicationId,
                    recipientId,
                    text,
                    id,
                    date,
                    state,
                    botMetadata
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
                    botMetadata
                )
            }
        }
    }

    @JsonTypeName(value = "choice")
    class SendChoiceMongoWrapper(
        val intentName: String,
        parameters: Map<String, String>
    ) : ActionMongoWrapper() {
        val parameters = obfuscate(parameters)

        constructor(choice: SendChoice) :
            this(
                choice.intentName,
                if (choice.state.testEvent) choice.parameters else obfuscate(choice.parameters)
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
                botMetadata
            )
        }
    }

    @JsonTypeName(value = "attachment")
    class SendAttachmentMongoWrapper(
        val url: String,
        @JsonProperty("attachment_type")
        @JsonAlias("type")
        val type: SendAttachment.AttachmentType
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
                botMetadata
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
                botMetadata
            )
        }
    }


}


