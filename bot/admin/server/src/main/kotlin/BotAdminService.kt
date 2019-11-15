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

package ai.tock.bot.admin

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType.builtin
import ai.tock.bot.admin.answer.AnswerConfigurationType.script
import ai.tock.bot.admin.answer.BuiltInAnswerConfiguration
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
import ai.tock.bot.admin.kotlin.compiler.KotlinFile
import ai.tock.bot.admin.kotlin.compiler.client.KotlinCompilerClient
import ai.tock.bot.admin.model.BotAnswerConfiguration
import ai.tock.bot.admin.model.BotBuiltinAnswerConfiguration
import ai.tock.bot.admin.model.BotScriptAnswerConfiguration
import ai.tock.bot.admin.model.BotSimpleAnswerConfiguration
import ai.tock.bot.admin.model.BotStoryDefinitionConfiguration
import ai.tock.bot.admin.model.BotStoryDefinitionConfigurationMandatoryEntity
import ai.tock.bot.admin.model.BotStoryDefinitionConfigurationStep
import ai.tock.bot.admin.model.CreateI18nLabelRequest
import ai.tock.bot.admin.model.CreateStoryRequest
import ai.tock.bot.admin.model.DialogFlowRequest
import ai.tock.bot.admin.model.DialogsSearchQuery
import ai.tock.bot.admin.model.StorySearchRequest
import ai.tock.bot.admin.model.UserSearchQuery
import ai.tock.bot.admin.model.UserSearchQueryResult
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationMandatoryEntity
import ai.tock.bot.admin.story.StoryDefinitionConfigurationStep
import ai.tock.bot.admin.user.UserReportDAO
import ai.tock.bot.engine.dialog.DialogFlowDAO
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.feature.FeatureState
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.admin.model.SentenceReport
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.service.applicationDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.shared.Dice
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.UnauthorizedException
import ai.tock.shared.vertx.WebVerticle.Companion.badRequest
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nLabel
import ai.tock.translator.Translator
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.time.Instant
import java.util.Locale

/**
 *
 */
object BotAdminService {

    private val logger = KotlinLogging.logger {}

    private val userReportDAO: UserReportDAO by injector.instance()
    internal val dialogReportDAO: DialogReportDAO by injector.instance()
    private val applicationConfigurationDAO: BotApplicationConfigurationDAO by injector.instance()
    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO by injector.instance()
    private val featureDAO: FeatureDAO by injector.instance()
    private val dialogFlowDAO: DialogFlowDAO get() = injector.provide()
    private val front = FrontClient

    fun getBots(namespace: String, botId: String): List<BotConfiguration> {
        return applicationConfigurationDAO.getBotConfigurationsByNamespaceAndBotId(namespace, botId)
    }

    fun save(conf: BotConfiguration) {
        applicationConfigurationDAO.save(conf)
    }

    fun getBotConfiguration(
        botApplicationConfigurationId: Id<BotApplicationConfiguration>,
        namespace: String
    ): BotApplicationConfiguration {
        val conf = applicationConfigurationDAO.getConfigurationById(botApplicationConfigurationId)
        if (conf?.namespace != namespace) {
            throw UnauthorizedException()
        }
        return conf
    }

    fun searchUsers(query: UserSearchQuery): UserSearchQueryResult {
        return UserSearchQueryResult(userReportDAO.search(query.toUserReportQuery()))
    }

    fun search(query: DialogsSearchQuery): DialogReportQueryResult {
        return dialogReportDAO.search(query.toDialogReportQuery())
    }

    fun deleteApplicationConfiguration(conf: BotApplicationConfiguration) {
        applicationConfigurationDAO.delete(conf)
        //delete rest connector if found
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
        return if (app == null) emptyList() else applicationConfigurationDAO.getConfigurationsByNamespaceAndNlpModel(namespace, app.name)
    }

    fun saveApplicationConfiguration(conf: BotApplicationConfiguration) {
        applicationConfigurationDAO.save(conf)
        if (applicationConfigurationDAO.getBotConfigurationsByNamespaceAndNameAndBotId(conf.namespace, conf.name, conf.botId) == null) {
            applicationConfigurationDAO.save(
                BotConfiguration(
                    conf.name,
                    conf.botId,
                    conf.namespace,
                    conf.nlpModel
                ))
        }
    }

    fun loadSentencesFromIntent(namespace: String, nlpModel: String, intentName: String, locale: Locale): List<SentenceReport> {
        val nlpApplication = front.getApplicationByNamespaceAndName(namespace, nlpModel)
        val intent = front.getIntentByNamespaceAndName(namespace, intentName)
        return if (intent != null) {
            val query = SentencesQuery(
                nlpApplication!!._id,
                locale,
                size = 3,
                intentId = intent._id
            )
            front.search(query)
                .sentences
                .map { s ->
                    SentenceReport(s)
                }
        } else {
            logger.warn { "unknown intent: ${namespace} ${intentName} - skipped" }
            emptyList()
        }
    }

    fun loadStories(request: StorySearchRequest): List<BotStoryDefinitionConfiguration> {
        val botConf =
            getBotConfigurationsByNamespaceAndNlpModel(request.namespace, request.applicationName).firstOrNull()
        return if (botConf == null) {
            emptyList()
        } else {
            storyDefinitionDAO
                .getStoryDefinitionsByNamespaceAndBotId(request.namespace, botConf.botId)
                .map {
                    BotStoryDefinitionConfiguration(it)
                }
        }
    }

    fun findStory(namespace: String, storyDefinitionId: String): BotStoryDefinitionConfiguration? {
        val story = storyDefinitionDAO.getStoryDefinitionById(storyDefinitionId.toId())
        if (story != null) {
            val botConf = getBotConfigurationsByNamespaceAndBotId(namespace, story.botId).firstOrNull()
            if (botConf != null) {
                return BotStoryDefinitionConfiguration(story)
            }
        }
        return null
    }

    fun findStoryByBotIdAndIntent(namespace: String, botId: String, intent: String): BotStoryDefinitionConfiguration? {
        return storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndIntent(namespace, botId, intent)
            ?.let { BotStoryDefinitionConfiguration(it) }
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
        request: CreateStoryRequest
    ): IntentDefinition? {

        val botConf =
            getBotConfigurationsByNamespaceAndBotId(namespace, request.story.botId).firstOrNull()
        return if (botConf != null) {
            val nlpApplication = front.getApplicationByNamespaceAndName(namespace, botConf.nlpModel)!!
            val intentDefinition =
                AdminService.createOrGetIntent(
                    namespace,
                    IntentDefinition(
                        request.story.intent.name,
                        namespace,
                        setOf(nlpApplication._id),
                        emptySet(),
                        category = request.story.category
                    )
                )!!

            //create the story
            saveStory(namespace, request.story)
            request.firstSentences.filter { it.isNotBlank() }.forEach {
                front.save(
                    ClassifiedSentence(
                        it,
                        request.language,
                        nlpApplication._id,
                        Instant.now(),
                        Instant.now(),
                        ClassifiedSentenceStatus.validated,
                        Classification(intentDefinition._id, emptyList()),
                        1.0,
                        1.0
                    )
                )
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

    private fun scriptAnswer(
        botId: String,
        oldAnswer: ScriptAnswerConfiguration?,
        answer: BotScriptAnswerConfiguration
    ): ScriptAnswerConfiguration? {

        val script = answer.current.script
        if (!KotlinCompilerClient.compilerDisabled && oldAnswer?.current?.script != script) {
            val fileName = "T${Dice.newId()}.kt"
            val result = KotlinCompilerClient.compile(KotlinFile(script, fileName))
            if (result?.compilationResult == null) {
                throw badRequest("Compilation error: ${result?.errors?.joinToString()}")
            } else {
                val c = result.compilationResult!!
                val newScript = ScriptAnswerVersionedConfiguration(
                    script,
                    c.files.map { it.key.substring(0, it.key.length - ".class".length) to it.value },
                    BotVersion.getCurrentBotVersion(botId),
                    c.mainClass
                )
                return ScriptAnswerConfiguration(
                    (oldAnswer?.scriptVersions ?: emptyList()) + newScript,
                    newScript
                )
            }
        } else {
            return oldAnswer
        }
    }

    private fun BotAnswerConfiguration.toConfiguration(
        botId: String,
        answers: List<AnswerConfiguration>?
    ): AnswerConfiguration? =
        when (this) {
            is BotSimpleAnswerConfiguration -> simpleAnswer(this)
            is BotScriptAnswerConfiguration ->
                scriptAnswer(
                    botId,
                    answers?.find { it.answerType == script } as? ScriptAnswerConfiguration,
                    this
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
            answers.mapNotNull { it.toConfiguration(botId, oldStory?.mandatoryEntities?.find { it.role == role }?.answers) },
            currentType
        ).apply {
            //if entity is null, it means that entity has not been modified
            if (entity != null) {
                //check that the intent & entity exist
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
                    front.save(newIntent.copy(
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
            name,
            intent?.takeIf { it.name.isNotBlank() },
            targetIntent?.takeIf { it.name.isNotBlank() },
            answers.mapNotNull { it.toConfiguration(botId, oldStory?.steps?.find { it.name == name }?.answers) },
            currentType,
            userSentence,
            children.map { it.toStepConfiguration(app, botId, oldStory) },
            level
        ).apply {
            //if intentDefinition is null, we don't need to update intent
            if (intentDefinition != null) {
                //check that the intent exists
                val intentName = intent?.name
                var newIntent = intentName?.let { front.getIntentByNamespaceAndName(app.namespace, intentName) }
                if (newIntent == null && intentName != null) {
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
                }
            }
            if (targetIntentDefinition != null) {
                //check that the intent exists
                val intentName = targetIntent?.name
                var newIntent = intentName?.let { front.getIntentByNamespaceAndName(app.namespace, intentName) }
                if (newIntent == null && intentName != null) {
                    newIntent = IntentDefinition(
                        intentName,
                        app.namespace,
                        setOf(app._id),
                        emptySet(),
                        label = targetIntentDefinition.label,
                        category = targetIntentDefinition.category,
                        description = targetIntentDefinition.description
                    )
                    front.save(newIntent)
                }
            }
        }

    fun saveStory(
        namespace: String,
        story: BotStoryDefinitionConfiguration
    ): BotStoryDefinitionConfiguration? {

        val storyDefinition = storyDefinitionDAO.getStoryDefinitionById(story._id)
        val botConf = getBotConfigurationsByNamespaceAndBotId(namespace, story.botId).firstOrNull()
        return if (botConf != null) {

            val application = front.getApplicationByNamespaceAndName(namespace, botConf.nlpModel)!!

            storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                namespace,
                botConf.botId,
                story.intent.name
            ).let {
                if (it == null || it.currentType == builtin) {
                    if (it?.currentType == builtin) {
                        storyDefinitionDAO.delete(it)
                    }

                    //intent change
                    if (storyDefinition?._id != null) {
                        AdminService.createOrGetIntent(
                            namespace,
                            IntentDefinition(
                                story.intent.name,
                                namespace,
                                setOf(application._id),
                                emptySet(),
                                category = story.category
                            )
                        )
                    }
                } else {
                    if (story._id != it._id) {
                        badRequest("Story already exists for the intent ${story.intent.name} : ${it.name}")
                    }
                }
            }
            if (storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
                    namespace,
                    botConf.botId,
                    story.storyId
                )?._id != storyDefinition?._id
            ) {
                badRequest("Story ${story.storyId} already exists")
            }

            val newStory = if (storyDefinition != null) {
                storyDefinition.copy(
                    name = story.name,
                    description = story.description,
                    category = story.category,
                    currentType = story.currentType,
                    intent = story.intent,
                    answers = story.answers.mapNotNull { it.toStoryConfiguration(botConf.botId, storyDefinition) },
                    mandatoryEntities = story.mandatoryEntities.map { it.toEntityConfiguration(application, botConf.botId, storyDefinition) },
                    steps = story.steps.map { it.toStepConfiguration(application, botConf.botId, storyDefinition) },
                    userSentence = story.userSentence,
                    configurationName = story.configurationName,
                    features = story.features
                )
            } else {
                StoryDefinitionConfiguration(
                    story.storyId,
                    story.botId,
                    story.intent,
                    story.currentType,
                    story.answers.mapNotNull { it.toStoryConfiguration(botConf.botId, storyDefinition) },
                    0,
                    namespace,
                    story.mandatoryEntities.map { it.toEntityConfiguration(application, botConf.botId, storyDefinition) },
                    story.steps.map { it.toStepConfiguration(application, botConf.botId, storyDefinition) },
                    story.name,
                    story.category,
                    story.description,
                    story.userSentence,
                    story.configurationName,
                    story.features
                )
            }

            storyDefinitionDAO.save(newStory)
            BotStoryDefinitionConfiguration(newStory)
        } else {
            null
        }
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

    fun toggleFeature(botId: String, namespace: String, category: String, name: String) {
        if (featureDAO.isEnabled(botId, namespace, category, name)) {
            featureDAO.disable(botId, namespace, category, name)
        } else {
            featureDAO.enable(botId, namespace, category, name)
        }
    }

    fun addFeature(botId: String, namespace: String, enabled: Boolean, category: String, name: String) {
        featureDAO.addFeature(botId, namespace, enabled, category, name)
    }

    fun deleteFeature(botId: String, namespace: String, category: String, name: String) {
        featureDAO.deleteFeature(botId, namespace, category, name)
    }

    fun loadDialogFlow(request: DialogFlowRequest): ApplicationDialogFlowData {
        val namespace = request.namespace
        val applicationIds = if (request.botConfigurationId != null) {
            setOf(request.botConfigurationId)
        } else {
            applicationConfigurationDAO
                .getConfigurationsByNamespaceAndConfigurationName(namespace, request.botConfigurationName)
                .map { it._id }
                .toSet()
        }
        return dialogFlowDAO.loadApplicationData(namespace, request.botId, applicationIds)
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

        //delete stories
        storyDefinitionDAO.getStoryDefinitionsByNamespaceAndBotId(
            app.namespace, app.name
        ).forEach {
            storyDefinitionDAO.delete(it)
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
        //stories
        storyDefinitionDAO.getStoryDefinitionsByNamespaceAndBotId(
            existingApp.namespace, existingApp.name
        ).forEach {
            storyDefinitionDAO.save(it.copy(botId = newApp.name))
        }
    }

}