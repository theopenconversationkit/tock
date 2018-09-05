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

package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import fr.vsct.tock.bot.admin.dialog.ActionReport
import fr.vsct.tock.bot.admin.dialog.DialogReport
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionMetadata
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendLocation
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.ArchivedEntityValue
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.DialogState
import fr.vsct.tock.bot.engine.dialog.EntityStateValue
import fr.vsct.tock.bot.engine.dialog.EntityValue
import fr.vsct.tock.bot.engine.dialog.EventState
import fr.vsct.tock.bot.engine.dialog.NextUserActionState
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserLocation
import fr.vsct.tock.shared.jackson.AnyValueWrapper
import fr.vsct.tock.shared.security.StringObfuscatorService.obfuscate
import fr.vsct.tock.translator.UserInterfaceType.textChat
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.JacksonData
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
    val lastUpdateDate: Instant = now()
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
        userTimeline.applicationIds
    )

    fun toDialog(storyDefinitionProvider: (String) -> StoryDefinition): Dialog {
        return stories.map { it.toStory(_id, storyDefinitionProvider) }.let { stories ->
            Dialog(
                playerIds,
                _id,
                state.toState(stories.flatMap { it.actions }.map { it.toActionId() to it }.toMap()),
                stories.toMutableList()
            )
        }
    }

    fun toDialogReport(): DialogReport {
        return DialogReport(
            stories.flatMap { it.actions }
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
                },
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
        val history: List<ArchivedEntityValueWrapper>,
        val lastUpdate: Instant = now()
    ) {

        constructor(value: EntityStateValue) : this(
            value.value,
            value.history.map { ArchivedEntityValueWrapper(it) },
            value.lastUpdate
        )

        fun toEntityStateValue(actionsMap: Map<Id<Action>, Action>): EntityStateValue {
            return EntityStateValue(
                value,
                history.map { it.toArchivedEntityValue(actionsMap) }.toMutableList(),
                lastUpdate
            )
        }
    }

    class ArchivedEntityValueWrapper(
        val entityValue: EntityValue?,
        val actionId: Id<Action>?,
        val date: Instant = Instant.now()
    ) {

        constructor(value: ArchivedEntityValue) : this(value.entityValue, value.action?.toActionId(), value.date)

        fun toArchivedEntityValue(actionsMap: Map<Id<Action>, Action>): ArchivedEntityValue {
            return ArchivedEntityValue(
                entityValue,
                actionsMap.get(actionId ?: ""),
                date
            )
        }
    }

    @Data(internal = true)
    class StoryMongoWrapper(
        val storyDefinitionId: String,
        var currentIntent: Intent?,
        val currentStep: String?,
        val actions: List<ActionMongoWrapper>
    ) {

        constructor(story: Story) : this(
            story.definition.id,
            story.starterIntent,
            story.currentStep,
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
    class SendSentenceMongoWrapper(
        val text: String?,
        val customMessage: Boolean = false
    ) : ActionMongoWrapper() {

        constructor(sentence: SendSentence) :
                this(
                    if (sentence.state.testEvent) sentence.stringText else obfuscate(sentence.stringText),
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
        val parameters: Map<String, String>
    ) : ActionMongoWrapper() {

        constructor(choice: SendChoice) : this(choice.intentName, choice.parameters) {
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


