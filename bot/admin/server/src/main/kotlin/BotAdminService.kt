/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.admin

import ai.tock.bot.admin.FaqAdminService.FAQ_CATEGORY
import ai.tock.bot.admin.annotation.*
import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType.builtin
import ai.tock.bot.admin.answer.AnswerConfigurationType.script
import ai.tock.bot.admin.answer.BuiltInAnswerConfiguration
import ai.tock.bot.admin.answer.DedicatedAnswerConfiguration
import ai.tock.bot.admin.answer.ScriptAnswerConfiguration
import ai.tock.bot.admin.answer.ScriptAnswerVersionedConfiguration
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.admin.bot.BotVersion
import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfigurationDAO
import ai.tock.bot.admin.bot.observability.BotObservabilityConfigurationDAO
import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.admin.bot.rag.BotRAGConfigurationDAO
import ai.tock.bot.admin.bot.sentencegeneration.BotSentenceGenerationConfigurationDAO
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfigurationDAO
import ai.tock.bot.admin.dialog.*
import ai.tock.bot.admin.kotlin.compiler.KotlinFile
import ai.tock.bot.admin.kotlin.compiler.client.KotlinCompilerClient
import ai.tock.bot.admin.model.*
import ai.tock.bot.admin.service.ObservabilityService
import ai.tock.bot.admin.service.RAGService
import ai.tock.bot.admin.story.*
import ai.tock.bot.admin.story.dump.*
import ai.tock.bot.admin.user.UserReportDAO
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.config.SatisfactionIntent
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.DialogFlowDAO
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.feature.FeatureState
import ai.tock.bot.engine.user.PlayerType
import ai.tock.genai.orchestratorcore.models.observability.LangfuseObservabilitySetting
import ai.tock.genai.orchestratorcore.utils.SecurityUtils
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.core.Intent
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.service.applicationDAO
import ai.tock.nlp.front.shared.config.*
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import ai.tock.shared.*
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.security.UserLogin
import ai.tock.shared.security.key.HasSecretKey
import ai.tock.shared.security.key.SecretKey
import ai.tock.shared.vertx.WebVerticle.Companion.badRequest
import ai.tock.translator.*
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import java.util.*

object BotAdminService {

    private val logger = KotlinLogging.logger {}

    private val userReportDAO: UserReportDAO get() = injector.provide()
    internal val dialogReportDAO: DialogReportDAO get() = injector.provide()
    private val applicationConfigurationDAO: BotApplicationConfigurationDAO get() = injector.provide()
    private val ragConfigurationDAO: BotRAGConfigurationDAO get() = injector.provide()
    private val sentenceGenerationConfigurationDAO: BotSentenceGenerationConfigurationDAO get() = injector.provide()
    private val observabilityConfigurationDAO: BotObservabilityConfigurationDAO get() = injector.provide()
    private val documentProcessorConfigurationDAO: BotDocumentCompressorConfigurationDAO get() = injector.provide()
    private val vectorStoreConfigurationDAO: BotVectorStoreConfigurationDAO get() = injector.provide()
    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO get() = injector.provide()
    private val featureDAO: FeatureDAO get() = injector.provide()
    private val dialogFlowDAO: DialogFlowDAO get() = injector.provide()
    private val front = FrontClient
    private val i18n: I18nDAO by injector.instance()


    private class BotStoryDefinitionConfigurationDumpController(
        override val targetNamespace: String,
        override val botId: String,
        val story: StoryDefinitionConfigurationDump,
        val application: ApplicationDefinition,
        val mainLocale: Locale,
        val user: UserLogin
    ) : StoryDefinitionConfigurationDumpController {

        override fun keepFeature(feature: StoryDefinitionConfigurationFeatureDump): Boolean =
            feature.botApplicationConfigurationId == null ||
                    getBotConfigurationById(feature.botApplicationConfigurationId!!)?.namespace == targetNamespace

        override fun buildScript(
            script: ScriptAnswerVersionedConfigurationDump,
            compile: Boolean
        ): ScriptAnswerVersionedConfiguration {
            return if (compile && !KotlinCompilerClient.compilerDisabled) {
                val fileName = "T${Dice.newId()}.kt"
                val result = KotlinCompilerClient.compile(KotlinFile(script.script, fileName))
                if (result?.compilationResult == null) {
                    badRequest("Compilation error: ${result?.errors?.joinToString()}")
                } else {
                    val c = result.compilationResult!!
                    ScriptAnswerVersionedConfiguration(
                        script = script.script,
                        compiledCode = c.files.map { it.key.substring(0, it.key.length - ".class".length) to it.value },
                        version = BotVersion.getCurrentBotVersion(botId),
                        mainClassName = c.mainClass
                    )
                }
            } else {
                ScriptAnswerVersionedConfiguration(
                    script = script.script,
                    compiledCode = emptyList(),
                    version = script.version,
                    mainClassName = "",
                    date = script.date
                )
            }
        }

        override fun checkIntent(intent: IntentWithoutNamespace?): IntentWithoutNamespace? {
            if (intent != null) {
                createOrGetIntent(
                    targetNamespace,
                    intent.name,
                    application._id,
                    story.category
                )
                return intent
            }
            return null
        }
    }

    fun addCommentToAnnotation(
        dialogId: String,
        actionId: String,
        eventDTO: BotAnnotationEventDTO,
        user: String
    ): BotAnnotationEvent {

        if (eventDTO.type != BotAnnotationEventType.COMMENT) {
            throw IllegalArgumentException("Only COMMENT events are allowed")
        }

        require(eventDTO.comment != null) { "Comment is required for COMMENT event type" }

        val annotation = dialogReportDAO.getAnnotationByActionId(dialogId, actionId)
            ?: throw IllegalStateException("Annotation not found")

        val event = BotAnnotationEventComment(
            eventId = newId(),
            creationDate = Instant.now(),
            lastUpdateDate = Instant.now(),
            user = user,
            comment = eventDTO.comment!!
        )

        dialogReportDAO.addAnnotationEvent(dialogId, actionId, event)

        return event
    }

    fun updateAnnotationEvent(
        dialogId: String,
        actionId: String,
        eventId: String,
        eventDTO: BotAnnotationEventDTO,
        user: String
    ): BotAnnotationEvent {
        val existingEvent = dialogReportDAO.getAnnotationEvent(dialogId, actionId, eventId)
            ?: throw IllegalArgumentException("Event not found")

        if (existingEvent.type != BotAnnotationEventType.COMMENT) {
            throw IllegalArgumentException("Only comment events can be updated")
        }

        if (eventDTO.type != BotAnnotationEventType.COMMENT) {
            throw IllegalArgumentException("Event type must be COMMENT")
        }

        require(eventDTO.comment != null) { "Comment must be provided" }

        val annotation = dialogReportDAO.getAnnotationByActionId(dialogId, actionId)
            ?: throw IllegalStateException("Annotation not found")

        val existingCommentEvent = existingEvent as BotAnnotationEventComment
        val updatedEvent = existingCommentEvent.copy(
            comment = eventDTO.comment!!,
            lastUpdateDate = Instant.now()
        )

        dialogReportDAO.updateAnnotationEvent(dialogId, actionId, eventId, updatedEvent)

        return updatedEvent
    }

    fun deleteAnnotationEvent(
        dialogId: String,
        actionId: String,
        annotationId: String,
        eventId: String,
        user: String
    ) {
        val existingEvent = dialogReportDAO.getAnnotationEvent(dialogId, actionId, eventId)
            ?: throw IllegalArgumentException("Event not found")

        if (existingEvent.type != BotAnnotationEventType.COMMENT) {
            throw IllegalArgumentException("Only comment events can be deleted")
        }

        val annotation = dialogReportDAO.getAnnotationByActionId(dialogId, actionId)
            ?: throw IllegalStateException("Annotation not found")


        dialogReportDAO.deleteAnnotationEvent(dialogId, actionId, eventId)
    }

    fun updateAnnotation(
        dialogId: String,
        actionId: String,
        annotationId: String,
        updatedAnnotationDTO: BotAnnotationUpdateDTO,
        user: String
    ): BotAnnotation {
        val existingAnnotation = dialogReportDAO.getAnnotation(dialogId, actionId, annotationId)
            ?: throw IllegalStateException("Annotation not found")

        val events = mutableListOf<BotAnnotationEvent>()

        updatedAnnotationDTO.state?.let { newState ->
            if (existingAnnotation.state != newState) {
                events.add(
                    BotAnnotationEventState(
                        eventId = newId(),
                        creationDate = Instant.now(),
                        lastUpdateDate = Instant.now(),
                        user = user,
                        before = existingAnnotation.state.name,
                        after = newState.name
                    )
                )
                existingAnnotation.state = newState
            }
        }

        updatedAnnotationDTO.reason?.let { newReason ->
            if (existingAnnotation.reason != newReason) {
                events.add(
                    BotAnnotationEventReason(
                        eventId = newId(),
                        creationDate = Instant.now(),
                        lastUpdateDate = Instant.now(),
                        user = user,
                        before = existingAnnotation.reason?.name,
                        after = newReason.name
                    )
                )
                existingAnnotation.reason = newReason
            }
        }

        updatedAnnotationDTO.groundTruth?.let { newGroundTruth ->
            if (existingAnnotation.groundTruth != newGroundTruth) {
                events.add(
                    BotAnnotationEventGroundTruth(
                        eventId = newId(),
                        creationDate = Instant.now(),
                        lastUpdateDate = Instant.now(),
                        user = user,
                        before = existingAnnotation.groundTruth,
                        after = newGroundTruth
                    )
                )
                existingAnnotation.groundTruth = newGroundTruth
            }
        }

        updatedAnnotationDTO.description?.let { newDescription ->
            if (existingAnnotation.description != newDescription) {
                events.add(
                    BotAnnotationEventDescription(
                        eventId = newId(),
                        creationDate = Instant.now(),
                        lastUpdateDate = Instant.now(),
                        user = user,
                        before = existingAnnotation.description,
                        after = newDescription
                    )
                )
                existingAnnotation.description = newDescription
            }
        }

        existingAnnotation.lastUpdateDate = Instant.now()

        existingAnnotation.events.addAll(events)

        dialogReportDAO.updateAnnotation(dialogId, actionId, existingAnnotation)

        return existingAnnotation
    }

    fun createAnnotation(
        dialogId: String,
        actionId: String,
        annotationDTO: BotAnnotationDTO,
        user: String
    ): BotAnnotation {

        if (dialogReportDAO.annotationExists(dialogId, actionId)) {
            throw IllegalStateException("Une annotation existe déjà pour cette action.")
        }

        val annotation = BotAnnotation(
            state = annotationDTO.state,
            reason = annotationDTO.reason,
            description = annotationDTO.description,
            groundTruth = annotationDTO.groundTruth,
            actionId = actionId,
            dialogId = dialogId,
            events = mutableListOf(),
            lastUpdateDate = Instant.now()
        )

        dialogReportDAO.updateAnnotation(dialogId, actionId, annotation)
        return annotation
    }

    fun createOrGetIntent(
        namespace: String,
        intentName: String,
        applicationId: Id<ApplicationDefinition>,
        intentCategory: String
    ): IntentDefinition =
        AdminService.createOrGetIntent(
            namespace,
            IntentDefinition(
                intentName,
                namespace,
                setOf(applicationId),
                emptySet(),
                category = intentCategory
            )
        )!!

    fun getBots(namespace: String, botId: String): List<BotConfiguration> {
        return applicationConfigurationDAO.getBotConfigurationsByNamespaceAndBotId(namespace, botId)
    }

    fun save(conf: BotConfiguration) {
        val locales = conf.supportedLocales.takeUnless { it.isEmpty() }
            ?: applicationDAO.getApplicationByNamespaceAndName(conf.namespace, conf.nlpModel)?.supportedLocales
            ?: emptySet()
        applicationConfigurationDAO.save(conf.copy(supportedLocales = locales))
    }

    fun searchUsers(query: UserSearchQuery): UserSearchQueryResult {
        return UserSearchQueryResult(userReportDAO.search(query.toUserReportQuery()))
    }

    fun search(query: DialogsSearchQuery): DialogReportQueryResult {
        return dialogReportDAO.search(query.toDialogReportQuery())
            .run {
                val searchResult = if (query.intentsToHide.isEmpty()) this else this.copy(
                    dialogs = filterIntentFromDialogActions(
                        this.dialogs,
                        query.intentsToHide
                    )
                )
                if (query.skipObfuscation) {
                    searchResult
                } else {
                    searchResult.copy(
                        dialogs = searchResult.dialogs.map { d ->
                            var obfuscatedDialog = false
                            val actions = d.actions.map {
                                val obfuscatedMessage = it.message.obfuscate()
                                obfuscatedDialog = obfuscatedDialog || it.message != obfuscatedMessage
                                it.copy(message = obfuscatedMessage)
                            }
                            val reviewCommentAction = actions.findLast { it.intent == SatisfactionIntent.REVIEW_COMMENT.id }
                            val reviewMessage = reviewCommentAction?.message?.toPrettyString() ?: d.review
                            d.copy(
                                actions = actions,
                                obfuscated = obfuscatedDialog,
                                review = reviewMessage
                            )
                        }
                    )
                }

                // Add nlp stats
                searchResult.copy(
                    nlpStats = dialogReportDAO.getNlpStats(searchResult.dialogs.map { it.id }, query.namespace)
                )
            }
    }

    fun getIntentsInDialogs(namespace: String,nlpModel : String) : Set<String>{
        return dialogReportDAO.intents(namespace,nlpModel)
    }

    fun getDialogObfuscatedById(id: Id<Dialog>, intentsToHide: Set<String>): DialogReport? {
        val dialog = dialogReportDAO.getDialog(id)
        return if (dialog == null) null else filterIntentFromDialogActions(listOf(dialog), intentsToHide).firstOrNull()
    }


    private fun filterIntentFromDialogActions(dialogs: List<DialogReport>, intentsToHide: Set<String>): List<DialogReport> {
        return dialogs.map { dialog ->
            val groupedLists = dialog.actions.groupBy { it.playerId.type }
            var list = dialog.actions.toMutableList()
            groupedLists[PlayerType.user]?.forEach { action ->
                if (intentsToHide.contains(action.intent)) {
                    val index = dialog.actions.indexOf(action)
                    val items = dialog.actions.subList(
                        index,
                        if (groupedLists[PlayerType.user]?.indexOf(action) == (groupedLists[PlayerType.user]?.size?.minus(
                                1
                            ))
                        ) dialog.actions.size else dialog.actions.indexOf(
                            groupedLists[PlayerType.user]?.indexOf(action)?.plus(1)
                                ?.let { groupedLists[PlayerType.user]?.get(it) })
                    )
                    list = list.filterNot { it in items }.toMutableList()
                }
            }
            dialog.copy(actions = list)
        }
    }

    fun searchRating(query: DialogsSearchQuery): RatingReportQueryResult? {
        return dialogReportDAO.findBotDialogStats(query.toDialogReportQuery())
    }

    fun deleteApplicationConfiguration(conf: BotApplicationConfiguration) {
        applicationConfigurationDAO.delete(conf)
        // delete rest connector if found
        applicationConfigurationDAO.getConfigurationByTargetId(conf._id)
            ?.also { applicationConfigurationDAO.delete(it) }
    }

    fun getBotConfigurationById(id: Id<BotApplicationConfiguration>): BotApplicationConfiguration? {
        return applicationConfigurationDAO.getConfigurationById(id)
    }

    fun getBotConfigurationByApplicationIdAndBotId(
        namespace: String,
        applicationId: String,
        botId: String
    ): BotApplicationConfiguration? {
        return applicationConfigurationDAO.getConfigurationByApplicationIdAndBotId(namespace, applicationId, botId)
    }

    fun getBotConfigurationsByNamespaceAndBotId(namespace: String, botId: String): List<BotApplicationConfiguration> {
        return applicationConfigurationDAO.getConfigurationsByNamespaceAndBotId(namespace, botId)
    }

    fun getBotConfigurationsByNamespaceAndNlpModel(
        namespace: String,
        applicationName: String
    ): List<BotApplicationConfiguration> {
        val app = applicationDAO.getApplicationByNamespaceAndName(namespace, applicationName)
        return if (app == null) emptyList() else applicationConfigurationDAO.getConfigurationsByNamespaceAndNlpModel(
            namespace,
            app.name
        )
    }

    fun saveApplicationConfiguration(conf: BotApplicationConfiguration) {
        applicationConfigurationDAO.save(conf)
        if (applicationConfigurationDAO.getBotConfigurationsByNamespaceAndNameAndBotId(
                conf.namespace,
                conf.name,
                conf.botId
            ) == null
        ) {
            val applicationDefinition = applicationDAO.getApplicationByNamespaceAndName(
                namespace = conf.namespace, name = conf.nlpModel
            ) ?: error("no application definition found for $conf")
            applicationConfigurationDAO.save(
                BotConfiguration(
                    name = conf.name,
                    botId = conf.botId,
                    namespace = conf.namespace,
                    nlpModel = conf.nlpModel,
                    supportedLocales = applicationDefinition.supportedLocales
                )
            )
        }
    }

    fun searchStories(request: StorySearchRequest): List<StoryDefinitionConfigurationSummaryExtended> =
        storyDefinitionDAO.searchStoryDefinitionSummariesExtended(request.toSummaryRequest())

    fun searchSummaryStories(request: SummaryStorySearchRequest): List<StoryDefinitionConfigurationSummaryMinimumMetrics> =
        storyDefinitionDAO.searchStoryDefinitionSummaries(request.toSummaryRequest())

    fun loadStories(request: StorySearchRequest): List<BotStoryDefinitionConfiguration> =
        findStories(request.namespace, request.applicationName).map {
            BotStoryDefinitionConfiguration(it, request.currentLanguage, true)
        }

    private fun findStories(namespace: String, applicationName: String): List<StoryDefinitionConfiguration> {
        val botConf =
            getBotConfigurationsByNamespaceAndNlpModel(namespace, applicationName).firstOrNull()
        return if (botConf == null) {
            emptyList()
        } else {
            storyDefinitionDAO
                .getStoryDefinitionsByNamespaceAndBotId(namespace, botConf.botId)
        }
    }

    fun exportStories(namespace: String, applicationName: String): List<StoryDefinitionConfigurationDump> =
        findStories(namespace, applicationName).map { StoryDefinitionConfigurationDump(it) }

    fun exportStory(
        namespace: String,
        applicationName: String,
        storyDefinitionId: String
    ): StoryDefinitionConfigurationDump? {
        val botConf =
            getBotConfigurationsByNamespaceAndNlpModel(namespace, applicationName).firstOrNull()
        return if (botConf != null) {
            val story =
                storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
                    namespace = namespace,
                    botId = botConf.botId,
                    storyId = storyDefinitionId
                )
            story?.let { StoryDefinitionConfigurationDump(it) }
        } else null
    }

    fun findStory(namespace: String, storyDefinitionId: String): BotStoryDefinitionConfiguration? {
        val story = storyDefinitionDAO.getStoryDefinitionById(storyDefinitionId.toId())
        return loadStory(namespace, story)
    }

    fun findRuntimeStorySettings(namespace: String, botId: String): List<BotStoryDefinitionConfiguration> {
        val stories = storyDefinitionDAO.getRuntimeStorySettings(namespace, botId)
        return stories.mapNotNull { story -> loadStory(namespace, story) }
    }

    fun findStoryDefinitionsByNamespaceAndBotIdWithFileAttached(
        namespace: String,
        botId: String
    ): List<BotStoryDefinitionConfiguration> {
        val stories = storyDefinitionDAO.getStoryDefinitionsByNamespaceAndBotIdWithFileAttached(namespace, botId)
        return stories.mapNotNull { story -> loadStory(namespace, story) }

    }


    private fun loadStory(namespace: String, conf: StoryDefinitionConfiguration?): BotStoryDefinitionConfiguration? {
        if (conf?.namespace == namespace) {
            val botConf = getBotConfigurationsByNamespaceAndBotId(namespace, conf.botId).firstOrNull()
            if (botConf != null) {
                val applicationDefinition = applicationDAO.getApplicationByNamespaceAndName(namespace, botConf.nlpModel)
                return BotStoryDefinitionConfiguration(
                    conf,
                    applicationDefinition?.supportedLocales?.firstOrNull() ?: defaultLocale
                )
            }
        }
        return null
    }

    fun importStories(
        namespace: String,
        botId: String,
        locale: Locale,
        dump: StoryDefinitionConfigurationDumpImport,
        user: UserLogin,
    ) {
        val botConf = getBotConfigurationsByNamespaceAndBotId(namespace, botId).firstOrNull()

        if (botConf == null) {
            badRequest("No bot configuration is defined yet")
        } else {
            val application = front.getApplicationByNamespaceAndName(namespace, botConf.nlpModel)!!
            val ragConfiguration = ragConfigurationDAO.findByNamespaceAndBotId(namespace, botConf.botId)

            dump.stories.forEach {
                try {
                    val controller =
                        BotStoryDefinitionConfigurationDumpController(namespace, botId, it, application, locale, user)
                    val storyConf = it.toStoryDefinitionConfiguration(controller)
                    importStory(namespace, storyConf, botConf, ragConfiguration, controller, dump.mode)
                } catch (e: Exception) {
                    logger.error("import error with story $it", e)
                }
            }
        }
    }

    private fun importStory(
        namespace: String,
        storyToImport: StoryDefinitionConfiguration,
        botConf: BotApplicationConfiguration,
        ragConfiguration: BotRAGConfiguration?,
        controller: BotStoryDefinitionConfigurationDumpController,
        importMode: StoriesImportMode
    ) {
        var storyToSave = manageExistingStory(botConf, storyToImport)

        // Manage the import of an unknown story, taking into account the RAG configuration
        if(ragConfiguration?.enabled == true && Intent.UNKNOWN_INTENT.name.withoutNamespace() == storyToImport.intent.name) {
            if(importMode == StoriesImportMode.RAG_OFF){
                ragConfigurationDAO.findByNamespaceAndBotId(namespace, botConf.botId)
                    ?.let {
                        ragConfigurationDAO.save(it.copy(enabled = false))
                    }
            } else if(importMode == StoriesImportMode.RAG_ON){
                storyToSave = storyToSave.copy(features = prepareEndingFeatures(storyToSave, false))
            }
        }

        // save the story
        storyDefinitionDAO.save(storyToSave)

        saveSentences(botConf, storyToImport, controller)
    }

    /**
     * Manage the existing Story that match the intent name or the storyId, of the imported story
     * @param botConf the [BotApplicationConfiguration]
     * @param storyToImport the [StoryDefinitionConfiguration]
     */
    private fun manageExistingStory(botConf: BotApplicationConfiguration, storyToImport: StoryDefinitionConfiguration): StoryDefinitionConfiguration {
        val existingStory1 = storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndIntent(
            botConf.namespace,
            botConf.botId,
            storyToImport.intent.name
        )

        val existingStory2 = storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
            botConf.namespace,
            botConf.botId,
            storyToImport.storyId
        )?.also {
            if (existingStory1 != null) {
                storyDefinitionDAO.delete(it)
            }
        }

        return storyToImport.copy(_id = existingStory1?._id ?: existingStory2?._id ?: storyToImport._id)
    }

    private fun saveSentences(botConf: BotApplicationConfiguration, storyToImport: StoryDefinitionConfiguration, controller: BotStoryDefinitionConfigurationDumpController) {
        val mainIntent = createOrGetIntent(
            botConf.namespace,
            storyToImport.intent.name,
            controller.application._id,
            storyToImport.category
        )
        if (storyToImport.userSentence.isNotBlank()) {
            saveSentence(
                storyToImport.userSentence,
                storyToImport.userSentenceLocale ?: controller.mainLocale,
                controller.application._id,
                mainIntent._id,
                controller.user
            )
        }

        // save all intents of steps
        storyToImport.steps.forEach { saveUserSentenceOfStep(controller.application, it, controller.user) }
    }

    /**
     * Update and get the story features
     * @param story the [StoryDefinitionConfiguration]
     * @param enabled the feature state
     */
    private fun prepareEndingFeatures(
        story: StoryDefinitionConfiguration, enabled: Boolean
    ): List<StoryDefinitionConfigurationFeature> {
        val features = mutableListOf<StoryDefinitionConfigurationFeature>()
        features.addAll(story.features)
        features.removeIf { feature -> feature.enabled != null }
        features.add(StoryDefinitionConfigurationFeature(null, enabled, null, null))
        return features
    }

    fun findConfiguredStoryByBotIdAndIntent(
        namespace: String,
        botId: String,
        intent: String
    ): BotStoryDefinitionConfiguration? {
        return storyDefinitionDAO.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(namespace, botId, intent)
            ?.let {
                loadStory(namespace, it)
            }
    }

    fun findConfiguredStoriesByBotIdAndIntent(
        namespace: String,
        botId: String,
        intentNames: List<String>
    ): List<StoryDefinitionConfiguration> {
        return storyDefinitionDAO.getConfiguredStoriesDefinitionByNamespaceAndBotIdAndIntent(
            namespace,
            botId,
            intentNames
        )
    }

    fun deleteStory(namespace: String, storyDefinitionId: String): Boolean {
        val story = storyDefinitionDAO.getStoryDefinitionById(storyDefinitionId.toId())
        if (story != null) {
            val botConf = getBotConfigurationsByNamespaceAndBotId(namespace, story.botId).firstOrNull()
            if (botConf != null) {
                storyDefinitionDAO.delete(story)
            }
        }
        return false
    }

    fun createStory(
        namespace: String,
        request: CreateStoryRequest,
        user: UserLogin
    ): IntentDefinition? {

        val botConf =
            getBotConfigurationsByNamespaceAndBotId(namespace, request.story.botId).firstOrNull()
        return if (botConf != null) {
            val nlpApplication = front.getApplicationByNamespaceAndName(namespace, botConf.nlpModel)!!
            val intentDefinition =
                createOrGetIntent(
                    namespace,
                    request.story.intent.name,
                    nlpApplication._id,
                    request.story.category
                )

            // create the story
            saveStory(namespace, request.story, user)
            request.firstSentences.filter { it.isNotBlank() }.forEach {
                saveSentence(it, request.language, nlpApplication._id, intentDefinition._id, user)
            }
            intentDefinition
        } else {
            null
        }
    }

    private fun simpleAnswer(
        answer: BotSimpleAnswerConfiguration
    ): SimpleAnswerConfiguration {

        return SimpleAnswerConfiguration(
            answer.answers.map { it.toConfiguration() }
        )
    }

    private fun String.toScriptConfiguration(
        botId: String,
        oldAnswer: ScriptAnswerConfiguration?
    ): ScriptAnswerConfiguration? = this
        .toNewScriptConfiguration(botId, oldAnswer?.current?.script, oldAnswer?.scriptVersions) ?: oldAnswer

    private fun String.toNewScriptConfiguration(
        botId: String,
        oldScript: String?,
        oldScriptVersions: List<ScriptAnswerVersionedConfiguration>?,
    ): ScriptAnswerConfiguration? {
        if (!KotlinCompilerClient.compilerDisabled && oldScript != this) {
            val fileName = "T${Dice.newId()}.kt"
            val result = KotlinCompilerClient.compile(KotlinFile(this, fileName))
            if (result?.compilationResult == null) {
                badRequest("Compilation error: ${result?.errors?.joinToString()}")
            } else {
                val c = result.compilationResult!!
                val newScript = ScriptAnswerVersionedConfiguration(
                    this,
                    c.files.map { it.key.substring(0, it.key.length - ".class".length) to it.value },
                    BotVersion.getCurrentBotVersion(botId),
                    c.mainClass
                )
                return ScriptAnswerConfiguration(
                    (oldScriptVersions ?: emptyList()) + newScript,
                    newScript
                )
            }
        } else {
            return null
        }
    }

    private fun BotAnswerConfiguration.toConfiguration(
        botId: String,
        answers: List<AnswerConfiguration>?
    ): AnswerConfiguration? =
        when (this) {
            is BotSimpleAnswerConfiguration -> simpleAnswer(this)
            is BotScriptAnswerConfiguration ->
                current.script.toScriptConfiguration(
                    botId,
                    answers?.find { it.answerType == script } as? ScriptAnswerConfiguration
                )

            is BotBuiltinAnswerConfiguration -> BuiltInAnswerConfiguration(storyHandlerClassName)
            else -> error("unsupported type $this")
        }

    private fun BotAnswerConfiguration.toAnswerConfiguration(
        botId: String,
        oldStory: StoryDefinitionConfiguration?
    ): AnswerConfiguration? =
        toConfiguration(botId, oldStory?.answers)

    private fun BotStoryDefinitionConfigurationMandatoryEntity.toEntityConfiguration(
        app: ApplicationDefinition,
        botId: String,
        oldStory: StoryDefinitionConfiguration?
    ): StoryDefinitionConfigurationMandatoryEntity =
        StoryDefinitionConfigurationMandatoryEntity(
            role,
            entityType,
            intent,
            answers.mapNotNull { botAnswerConfiguration ->
                botAnswerConfiguration.toConfiguration(
                    botId,
                    oldStory?.mandatoryEntities?.find { it.role == role }?.answers
                )
            },
            currentType
        ).apply {
            // if entity is null, it means that entity has not been modified
            if (entity != null) {
                // check that the intent & entity exist
                var newIntent = front.getIntentByNamespaceAndName(app.namespace, intent.name)
                val existingEntity = newIntent?.findEntity(role)
                val entityTypeName = entity.entityTypeName
                if (existingEntity == null) {
                    if (front.getEntityTypeByName(entityTypeName) == null) {
                        front.save(EntityTypeDefinition(entityTypeName))
                    }
                }
                if (newIntent == null) {
                    newIntent = IntentDefinition(
                        intent.name,
                        app.namespace,
                        setOf(app._id),
                        setOf(EntityDefinition(entityTypeName, role)),
                        label = intentDefinition?.label,
                        category = intentDefinition?.category,
                        description = intentDefinition?.description
                    )
                    front.save(newIntent)
                } else if (existingEntity == null) {
                    front.save(
                        newIntent.copy(
                            applications = newIntent.applications + app._id,
                            entities = newIntent.entities + EntityDefinition(entityTypeName, role)
                        )
                    )
                }
            }
        }

    private fun BotStoryDefinitionConfigurationStep.toStepConfiguration(
        app: ApplicationDefinition,
        botId: String,
        oldStory: StoryDefinitionConfiguration?
    ): StoryDefinitionConfigurationStep =
        StoryDefinitionConfigurationStep(
            name.takeIf { it.startsWith("##") }
                ?: "##${Dice.newId()}_${intent?.name}_${(entity?.value ?: entity?.entityRole)?.let { "_$it" }}_$level",
            intent?.takeIf { it.name.isNotBlank() },
            targetIntent?.takeIf { it.name.isNotBlank() },
            answers.mapNotNull { botAnswerConfiguration ->
                botAnswerConfiguration.toConfiguration(
                    botId,
                    oldStory?.steps?.find { it.name == name }?.answers
                )
            },
            currentType,
            userSentence.defaultLabel ?: "",
            I18nLabelValue(userSentence),
            children.map { it.toStepConfiguration(app, botId, oldStory) },
            level,
            entity,
            metrics
        ).apply {
            updateIntentDefinition(intentDefinition, intent, app)
            updateIntentDefinition(targetIntentDefinition, targetIntent, app)
        }

    private fun updateIntentDefinition(
        intentDefinition: IntentDefinition?,
        intent: IntentWithoutNamespace?,
        app: ApplicationDefinition
    ): IntentDefinition? {
        // if intentDefinition is null, we don't need to update intent
        if (intentDefinition != null) {
            // check that the intent exists
            val intentName = intent?.name
            if (intentName != null) {
                var newIntent = front.getIntentByNamespaceAndName(app.namespace, intentName)
                if (newIntent == null) {
                    newIntent = IntentDefinition(
                        intentName,
                        app.namespace,
                        setOf(app._id),
                        emptySet(),
                        label = intentDefinition.label,
                        category = intentDefinition.category,
                        description = intentDefinition.description
                    )
                    front.save(newIntent)
                } else if (!newIntent.applications.contains(app._id)) {
                    front.save(newIntent.copy(applications = newIntent.applications + app._id))
                }
                return newIntent
            }
        }

        return null
    }

    private fun mergeStory(
        oldStory: StoryDefinitionConfiguration,
        story: BotStoryDefinitionConfiguration,
        application: ApplicationDefinition,
        botId: String
    ): StoryDefinitionConfiguration {
        return oldStory.copy(
            name = story.name,
            description = story.description,
            category = story.category,
            currentType = story.currentType,
            intent = story.intent,
            answers = story.answers.mapNotNull { it.toAnswerConfiguration(botId, oldStory) },
            mandatoryEntities = story.mandatoryEntities.map {
                it.toEntityConfiguration(
                    application,
                    botId,
                    oldStory
                )
            },
            steps = story.steps.map { it.toStepConfiguration(application, botId, oldStory) },
            userSentence = story.userSentence,
            userSentenceLocale = story.userSentenceLocale,
            configurationName = story.configurationName,
            features = story.features,
            tags = story.tags,
            configuredAnswers = story.configuredAnswers.map {
                it.toConfiguredAnswer(botId, oldStory)
            },
            configuredSteps = story.configuredSteps.mapSteps(application, botId, oldStory),
            nextIntentsQualifiers = story.nextIntentsQualifiers,
            metricStory = story.metricStory,
        )
    }

    /**
     * Checks and save the story
     * @param namespace
     * @param : story : botStoryDefinitionConfiguration
     * @param: user : userLogin
     * @param: createdIntent : intent can be created out of the method
     */
    fun saveStory(
        namespace: String,
        story: BotStoryDefinitionConfiguration,
        user: UserLogin,
        createdIntent: IntentDefinition? = null
    ): BotStoryDefinitionConfiguration? {

        // Manage unknown story when RAG is enabled
        manageUnknownStory(story)

        if (!story.validateMetrics()) {
            badRequest("Story is not valid : Metric story must have at least one step that handles at least one metric.")
        }

        // Two stories (built-in or configured) should not have the same _id
        // There should be max one built-in (resp. configured) story for given namespace+bot+intent (or namespace+bot+storyId)
        // It can be updated if storyId remains the same, fails otherwise

        val storyWithSameId = storyDefinitionDAO.getStoryDefinitionById(story._id)
        storyWithSameId?.let {
            val existingType = it.currentType
            if (story.currentType == builtin && existingType != builtin) {
                badRequest("Story ${it.name} ($existingType) already exists with the ID")
            }
        }

        val botConf = getBotConfigurationsByNamespaceAndBotId(namespace, story.botId).firstOrNull()
        return if (botConf != null) {

            val application = front.getApplicationByNamespaceAndName(namespace, botConf.nlpModel)!!
            val storyWithSameNsBotAndName =
                storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
                    namespace,
                    botConf.botId,
                    story.storyId
                )?.also { logger.debug { "Found story with same namespace, type and name: $it" } }
            val storyWithSameNsBotAndIntent =
                storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                    namespace,
                    botConf.botId,
                    story.intent.name
                )?.also { logger.debug { "Found story with same namespace, type and intent: $it" } }

            storyWithSameNsBotAndIntent.let {
                if (it == null || it.currentType == builtin) {
                    // intent change
                    if (storyWithSameId?._id != null && createdIntent == null) {
                        createOrGetIntent(
                            namespace,
                            story.intent.name,
                            application._id,
                            story.category
                        )
                    }
                } else {
                    if (story._id != it._id) {
                        badRequest("Story ${it.name} (${it.currentType}) already exists for intent ${story.intent.name}")
                    }
                }
            }
            if (storyWithSameNsBotAndName != null && storyWithSameNsBotAndName._id != storyWithSameId?._id
            ) {
                if (storyWithSameNsBotAndName.currentType != builtin) {
                    badRequest("Story ${story.name} (${story.currentType}) already exists")
                }
            }

            val newStory = when {
                storyWithSameId != null -> {
                    mergeStory(storyWithSameId, story, application, botConf.botId)
                }

                storyWithSameNsBotAndIntent != null -> {
                    mergeStory(storyWithSameNsBotAndIntent, story, application, botConf.botId)
                }

                storyWithSameNsBotAndName != null -> {
                    mergeStory(storyWithSameNsBotAndName, story, application, botConf.botId)
                }

                else -> {
                    StoryDefinitionConfiguration(
                        storyId = story.storyId,
                        botId = story.botId,
                        intent = story.intent,
                        currentType = story.currentType,
                        answers = story.answers.mapNotNull { it.toAnswerConfiguration(botConf.botId, null) },
                        version = 0,
                        namespace = namespace,
                        mandatoryEntities = story.mandatoryEntities.map {
                            it.toEntityConfiguration(
                                application,
                                botConf.botId,
                                storyWithSameId
                            )
                        },
                        steps = story.steps.map { it.toStepConfiguration(application, botConf.botId, null) },
                        name = story.name,
                        category = story.category,
                        description = story.description,
                        userSentence = story.userSentence,
                        userSentenceLocale = story.userSentenceLocale,
                        configurationName = story.configurationName,
                        features = story.features,
                        tags = story.tags,
                        configuredAnswers = story.configuredAnswers.map {
                            it.toConfiguredAnswer(
                                botConf.botId,
                                null
                            )
                        },
                        configuredSteps = story.configuredSteps.mapSteps(application, botConf.botId, null),
                        nextIntentsQualifiers = story.nextIntentsQualifiers,
                        metricStory = story.metricStory
                    )
                }
            }

            logger.debug { "Saving story: $newStory" }
            storyDefinitionDAO.save(newStory)

            //avoid double calls for shared intent story if it is a FAQ because it is already done before
            if (story.userSentence.isNotBlank() && story.category != FAQ_CATEGORY) {
                val intent = createOrGetIntent(
                    namespace,
                    story.intent.name,
                    application._id,
                    story.category
                )
                saveSentence(
                    story.userSentence,
                    story.userSentenceLocale,
                    application._id,
                    createdIntent?._id ?: intent._id,
                    user
                )
            }

            // save all intents of steps
            val storySteps = newStory.steps + newStory.configuredSteps.flatMap { it.steps }
            storySteps.forEach { saveUserSentenceOfStep(application, it, user) }

            BotStoryDefinitionConfiguration(newStory, story.userSentenceLocale)
        } else {
            null
        }
    }

    /**
     * Manage unknown story when RAG is enabled
     */
    private fun manageUnknownStory(story: BotStoryDefinitionConfiguration) {
        if(Intent.UNKNOWN_INTENT.name.withoutNamespace() == story.intent.name.withoutNamespace()) {
            ragConfigurationDAO.findByNamespaceAndBotId(story.namespace, story.botId)?.let {
                if (it.enabled) {
                    badRequest("It is not allowed to create or update unknown story when RAG is enabled.")
                }
            }
        }
    }

    private fun List<BotConfiguredSteps>.mapSteps(
        app: ApplicationDefinition,
        botId: String,
        oldStory: StoryDefinitionConfiguration?
    ): List<StoryDefinitionConfigurationByBotStep> =
        map {
            StoryDefinitionConfigurationByBotStep(
                it.botConfiguration,
                it.steps.map { step ->
                    step.toStepConfiguration(app, botId, oldStory)
                }
            )
        }

    private fun BotConfiguredAnswer.toConfiguredAnswer(botId: String, oldStory: StoryDefinitionConfiguration?):
            DedicatedAnswerConfiguration {
        val oldConf = oldStory?.configuredAnswers?.find { it.botConfiguration == botConfiguration }
        return DedicatedAnswerConfiguration(
            botConfiguration,
            currentType,
            answers.mapNotNull { it.toConfiguration(botId, oldConf?.answers) }
        )
    }

    fun saveSentence(
        text: String,
        locale: Locale,
        applicationId: Id<ApplicationDefinition>,
        intentId: Id<IntentDefinition>,
        user: UserLogin
    ) {

        if (
            front.search(
                SentencesQuery(
                    applicationId = applicationId,
                    language = locale,
                    search = text,
                    onlyExactMatch = true,
                    intentId = intentId,
                    status = setOf(validated, model)
                )
            ).total == 0L
        ) {
            front.save(
                ClassifiedSentence(
                    text = text,
                    language = locale,
                    applicationId = applicationId,
                    creationDate = Instant.now(),
                    updateDate = Instant.now(),
                    status = validated,
                    classification = Classification(intentId, emptyList()),
                    lastIntentProbability = 1.0,
                    lastEntityProbability = 1.0,
                    qualifier = user
                )
            )
        }
    }

    private fun saveUserSentenceOfStep(
        application: ApplicationDefinition,
        step: StoryDefinitionConfigurationStep,
        user: UserLogin
    ) {

        val label = step.userSentenceLabel?.let { Translator.getLabel(it.key) }
        if (label != null && step.intent != null) {
            application.supportedLocales.forEach { locale ->
                val text = label.findLabel(locale)?.label
                    ?: label.findLabel(defaultLocale)?.label
                if (text != null) {
                    val intent = front.getIntentByNamespaceAndName(application.namespace, step.intent!!.name)
                    if (intent != null) {
                        saveSentence(text, locale, application._id, intent._id, user)
                    }
                }
            }
        }

        step.children.forEach { saveUserSentenceOfStep(application, it, user) }
    }

    fun createI18nRequest(namespace: String, request: CreateI18nLabelRequest): I18nLabel {
        val labelKey =
            I18nKeyProvider
                .simpleKeyProvider(namespace, request.category)
                .i18n(request.label)
        return Translator.create(labelKey, request.locale)
    }

    fun getFeatures(botId: String, namespace: String): List<FeatureState> {
        return featureDAO.getFeatures(botId, namespace).sortedBy { it.category + it.name }
    }

    fun toggleFeature(botId: String, namespace: String, feature: Feature) {
        if (featureDAO.isEnabled(botId, namespace, feature.category, feature.name, feature.applicationId)) {
            featureDAO.disable(botId, namespace, feature.category, feature.name, feature.applicationId)
        } else {
            featureDAO.enable(
                botId,
                namespace,
                feature.category,
                feature.name,
                feature.startDate,
                feature.endDate,
                feature.applicationId,
            )
        }
    }

    fun updateDateAndEnableFeature(botId: String, namespace: String, feature: Feature) {
        featureDAO.enable(
            botId,
            namespace,
            feature.category,
            feature.name,
            feature.startDate,
            feature.endDate,
            feature.applicationId,
            feature.graduation
        )
    }

    fun addFeature(botId: String, namespace: String, feature: Feature) {
        featureDAO.addFeature(
            botId = botId,
            namespace = namespace,
            enabled = feature.enabled,
            category = feature.category,
            name = feature.name,
            startDate = feature.startDate,
            endDate = feature.endDate,
            applicationId = feature.applicationId,
            graduation = feature.graduation
        )
    }

    fun deleteFeature(botId: String, namespace: String, category: String, name: String, applicationId: String?) {
        featureDAO.deleteFeature(botId, namespace, category, name, applicationId)
    }

    fun loadDialogFlow(request: DialogFlowRequest): ApplicationDialogFlowData {
        val namespace = request.namespace
        val botId = request.botId
        val applicationIds = loadApplicationIds(request)
        logger.debug { "Loading Bot Flow for ${applicationIds.size} configurations: $applicationIds..." }
        return dialogFlowDAO.loadApplicationData(namespace, botId, applicationIds, request.from, request.to)
    }

    private fun loadApplicationIds(request: DialogFlowRequest): Set<Id<BotApplicationConfiguration>> {
        return loadApplications(request).map { it._id }.toSet()
    }

    private fun loadApplications(request: DialogFlowRequest): Set<BotApplicationConfiguration> {
        val namespace = request.namespace
        val botId = request.botId
        val configurationName = request.botConfigurationName
        val tests = request.includeTestConfigurations
        return if (request.botConfigurationId != null) {
            if (tests && configurationName != null) {
                val configurations = applicationConfigurationDAO.getConfigurationsByBotNamespaceAndConfigurationName(
                    namespace = namespace,
                    botId = botId,
                    configurationName = configurationName
                )
                val actualConfiguration = configurations.find { it._id == request.botConfigurationId }
                val testConfiguration =
                    configurations.find { it.applicationId == "test-${actualConfiguration?.applicationId}" }
                listOfNotNull(actualConfiguration, testConfiguration).toSet()
            } else
                listOfNotNull(applicationConfigurationDAO.getConfigurationById(request.botConfigurationId)).toSet()
        } else if (configurationName != null) {
            applicationConfigurationDAO
                .getConfigurationsByBotNamespaceAndConfigurationName(namespace, botId, configurationName)
                .filter { tests || it.connectorType != ConnectorType.rest }
                .toSet()
        } else {
            applicationConfigurationDAO
                .getConfigurationsByNamespaceAndBotId(namespace, botId)
                .filter { tests || it.connectorType != ConnectorType.rest }
                .toSet()
        }
    }

    fun deleteApplication(app: ApplicationDefinition) {
        applicationConfigurationDAO.getConfigurationsByNamespaceAndNlpModel(
            app.namespace, app.name
        ).forEach {
            applicationConfigurationDAO.delete(it)
        }
        applicationConfigurationDAO.getBotConfigurationsByNamespaceAndBotId(
            app.namespace, app.name
        ).forEach {
            applicationConfigurationDAO.delete(it)
        }

        // delete stories and faqDefinitions
        storyDefinitionDAO.getStoryDefinitionsByNamespaceAndBotId(
            app.namespace, app.name
        ).forEach { story ->
            storyDefinitionDAO.delete(story)
        }

        // delete the RAG configuration
        ragConfigurationDAO.findByNamespaceAndBotId(app.namespace, app.name)?.let { config ->
            ragConfigurationDAO.delete(config._id)
            config.llmSetting.apiKey?.let { SecurityUtils.deleteSecret(it) }
            config.emSetting.apiKey?.let { SecurityUtils.deleteSecret(it) }
        }

        // delete the Sentence Generation configuration
        sentenceGenerationConfigurationDAO.findByNamespaceAndBotId(app.namespace, app.name)?.let { config ->
            sentenceGenerationConfigurationDAO.delete(config._id)
            config.llmSetting.apiKey?.let { SecurityUtils.deleteSecret(it) }
        }

        // delete the Observability configuration
        observabilityConfigurationDAO.findByNamespaceAndBotId(app.namespace, app.name)?.let { config ->
            observabilityConfigurationDAO.delete(config._id)
            (config.setting as? HasSecretKey<SecretKey>)?.secretKey?.let { secret ->
                SecurityUtils.deleteSecret(secret)
            }
        }

        // delete the Document Compressor configuration
        documentProcessorConfigurationDAO.findByNamespaceAndBotId(app.namespace, app.name)?.let {
            documentProcessorConfigurationDAO.delete(it._id)
        }

        // delete the Vector Store configuration
        vectorStoreConfigurationDAO.findByNamespaceAndBotId(app.namespace, app.name)?.let { config ->
            vectorStoreConfigurationDAO.delete(config._id)
            SecurityUtils.deleteSecret(config.setting.password)
        }
    }

    fun changeSupportedLocales(newApp: ApplicationDefinition) {
        applicationConfigurationDAO.getBotConfigurationsByNamespaceAndBotId(
            newApp.namespace, newApp.name
        ).forEach {
            applicationConfigurationDAO.save(it.copy(supportedLocales = newApp.supportedLocales))
        }
    }

    fun changeApplicationName(existingApp: ApplicationDefinition, newApp: ApplicationDefinition) {
        applicationConfigurationDAO.getConfigurationsByNamespaceAndNlpModel(
            existingApp.namespace, existingApp.name
        ).forEach {
            applicationConfigurationDAO.save(it.copy(botId = newApp.name, nlpModel = newApp.name))
        }
        applicationConfigurationDAO.getBotConfigurationsByNamespaceAndBotId(
            existingApp.namespace, existingApp.name
        ).forEach {
            applicationConfigurationDAO.save(it.copy(botId = newApp.name))
        }
        // stories
        storyDefinitionDAO.getStoryDefinitionsByNamespaceAndBotId(
            existingApp.namespace, existingApp.name
        ).forEach {
            storyDefinitionDAO.save(it.copy(botId = newApp.name))
        }
    }

    fun importLabels(labels: List<I18nLabel>, organization: String) : Int {
        return labels
            .filter { it.i18n.any { i18n -> i18n.validated } }
            .map {
                it.copy(
                    _id = it._id.toString().replaceFirst(it.namespace, organization).toId(),
                    namespace = organization
                )
            }.apply {
                i18n.save(this)
            }
            .size
    }
}
