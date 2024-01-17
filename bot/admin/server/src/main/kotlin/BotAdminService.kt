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
import ai.tock.bot.admin.dialog.ApplicationDialogFlowData
import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.admin.dialog.DialogReportQueryResult
import ai.tock.bot.admin.dialog.RatingReportQueryResult
import ai.tock.bot.admin.dialog.DialogReport
import ai.tock.bot.admin.kotlin.compiler.KotlinFile
import ai.tock.bot.admin.kotlin.compiler.client.KotlinCompilerClient
import ai.tock.bot.admin.model.BotAnswerConfiguration
import ai.tock.bot.admin.model.BotBuiltinAnswerConfiguration
import ai.tock.bot.admin.model.BotConfiguredAnswer
import ai.tock.bot.admin.model.BotConfiguredSteps
import ai.tock.bot.admin.model.BotScriptAnswerConfiguration
import ai.tock.bot.admin.model.BotSimpleAnswerConfiguration
import ai.tock.bot.admin.model.BotStoryDefinitionConfiguration
import ai.tock.bot.admin.model.BotStoryDefinitionConfigurationMandatoryEntity
import ai.tock.bot.admin.model.BotStoryDefinitionConfigurationStep
import ai.tock.bot.admin.model.CreateI18nLabelRequest
import ai.tock.bot.admin.model.CreateStoryRequest
import ai.tock.bot.admin.model.DialogFlowRequest
import ai.tock.bot.admin.model.DialogsSearchQuery
import ai.tock.bot.admin.model.Feature
import ai.tock.bot.admin.model.StorySearchRequest
import ai.tock.bot.admin.model.SummaryStorySearchRequest
import ai.tock.bot.admin.model.UserSearchQuery
import ai.tock.bot.admin.model.UserSearchQueryResult
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationByBotStep
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationMandatoryEntity
import ai.tock.bot.admin.story.StoryDefinitionConfigurationStep
import ai.tock.bot.admin.story.StoryDefinitionConfigurationSummaryExtended
import ai.tock.bot.admin.story.StoryDefinitionConfigurationSummaryMinimumMetrics
import ai.tock.bot.admin.story.dump.ScriptAnswerVersionedConfigurationDump
import ai.tock.bot.admin.story.dump.StoryDefinitionConfigurationDump
import ai.tock.bot.admin.story.dump.StoryDefinitionConfigurationDumpController
import ai.tock.bot.admin.story.dump.StoryDefinitionConfigurationFeatureDump
import ai.tock.bot.admin.user.UserReportDAO
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.engine.config.SatisfactionIntent
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.DialogFlowDAO
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.feature.FeatureState
import ai.tock.bot.engine.user.PlayerType
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.service.applicationDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.shared.Dice
import ai.tock.shared.defaultLocale
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.UserLogin
import ai.tock.shared.vertx.WebVerticle.Companion.badRequest
import ai.tock.translator.*
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.time.Instant
import java.util.Locale

object BotAdminService {

    private val logger = KotlinLogging.logger {}

    private val userReportDAO: UserReportDAO get() = injector.provide()
    internal val dialogReportDAO: DialogReportDAO get() = injector.provide()
    private val applicationConfigurationDAO: BotApplicationConfigurationDAO get() = injector.provide()
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
        stories: List<StoryDefinitionConfigurationDump>,
        user: UserLogin
    ) {
        val botConf = getBotConfigurationsByNamespaceAndBotId(namespace, botId).firstOrNull()

        if (botConf == null) {
            badRequest("No bot configuration is defined yet")
        } else {
            val application = front.getApplicationByNamespaceAndName(namespace, botConf.nlpModel)!!
            stories.forEach {
                try {
                    val controller =
                        BotStoryDefinitionConfigurationDumpController(namespace, botId, it, application, locale, user)
                    val storyConf = it.toStoryDefinitionConfiguration(controller)
                    importStory(namespace, storyConf, botConf, controller)
                } catch (e: Exception) {
                    logger.error("import error with story $it", e)
                }
            }
        }
    }

    private fun importStory(
        namespace: String,
        story: StoryDefinitionConfiguration,
        botConf: BotApplicationConfiguration,
        controller: BotStoryDefinitionConfigurationDumpController
    ) {
        val existingStory1 = storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndIntent(
            namespace,
            botConf.botId,
            story.intent.name
        )

        val existingStory2 = storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
            namespace,
            botConf.botId,
            story.storyId
        )?.also {
            if (existingStory1 != null) {
                storyDefinitionDAO.delete(it)
            }
        }

        storyDefinitionDAO.save(story.copy(_id = existingStory1?._id ?: existingStory2?._id ?: story._id))

        val mainIntent = createOrGetIntent(
            namespace,
            story.intent.name,
            controller.application._id,
            story.category
        )
        if (story.userSentence.isNotBlank()) {
            saveSentence(
                story.userSentence,
                story.userSentenceLocale ?: controller.mainLocale,
                controller.application._id,
                mainIntent._id,
                controller.user
            )
        }

        // save all intents of steps
        story.steps.forEach { saveUserSentenceOfStep(controller.application, it, controller.user) }
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

    private fun BotAnswerConfiguration.toStoryConfiguration(
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
            answers = story.answers.mapNotNull { it.toStoryConfiguration(botId, oldStory) },
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
                        answers = story.answers.mapNotNull { it.toStoryConfiguration(botConf.botId, null) },
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
        return featureDAO.getFeatures(botId, namespace)
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
                feature.applicationId
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
            feature.applicationId
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
            applicationId = feature.applicationId
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
