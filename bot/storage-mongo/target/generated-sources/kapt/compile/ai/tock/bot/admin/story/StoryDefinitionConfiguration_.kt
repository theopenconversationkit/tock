package ai.tock.bot.admin.story

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.DedicatedAnswerConfiguration
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.IntentWithoutNamespace_
import ai.tock.bot.definition.StoryTag
import java.util.Locale
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __StoryId: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::storyId
private val __BotId: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::botId
private val __Intent: KProperty1<StoryDefinitionConfiguration, IntentWithoutNamespace?>
    get() = StoryDefinitionConfiguration::intent
private val __CurrentType: KProperty1<StoryDefinitionConfiguration, AnswerConfigurationType?>
    get() = StoryDefinitionConfiguration::currentType
private val __Answers: KProperty1<StoryDefinitionConfiguration, List<AnswerConfiguration>?>
    get() = StoryDefinitionConfiguration::answers
private val __Version: KProperty1<StoryDefinitionConfiguration, Int?>
    get() = StoryDefinitionConfiguration::version
private val __Namespace: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::namespace
private val __MandatoryEntities: KProperty1<StoryDefinitionConfiguration,
        List<StoryDefinitionConfigurationMandatoryEntity>?>
    get() = StoryDefinitionConfiguration::mandatoryEntities
private val __Steps: KProperty1<StoryDefinitionConfiguration,
        List<StoryDefinitionConfigurationStep>?>
    get() = StoryDefinitionConfiguration::steps
private val __Name: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::name
private val __Category: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::category
private val __Description: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::description
private val __UserSentence: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::userSentence
private val __UserSentenceLocale: KProperty1<StoryDefinitionConfiguration, Locale?>
    get() = StoryDefinitionConfiguration::userSentenceLocale
private val __ConfigurationName: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::configurationName
private val __Features: KProperty1<StoryDefinitionConfiguration,
        List<StoryDefinitionConfigurationFeature>?>
    get() = StoryDefinitionConfiguration::features
private val ___id: KProperty1<StoryDefinitionConfiguration, Id<StoryDefinitionConfiguration>?>
    get() = StoryDefinitionConfiguration::_id
private val __Tags: KProperty1<StoryDefinitionConfiguration, Set<StoryTag>?>
    get() = StoryDefinitionConfiguration::tags
private val __ConfiguredAnswers: KProperty1<StoryDefinitionConfiguration,
        List<DedicatedAnswerConfiguration>?>
    get() = StoryDefinitionConfiguration::configuredAnswers
private val __ConfiguredSteps: KProperty1<StoryDefinitionConfiguration,
        List<StoryDefinitionConfigurationByBotStep>?>
    get() = StoryDefinitionConfiguration::configuredSteps
class StoryDefinitionConfiguration_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        StoryDefinitionConfiguration?>) : KPropertyPath<T,
        StoryDefinitionConfiguration?>(previous,property) {
    val storyId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryId)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val intent: IntentWithoutNamespace_<T>
        get() = IntentWithoutNamespace_(this,StoryDefinitionConfiguration::intent)

    val currentType: KPropertyPath<T, AnswerConfigurationType?>
        get() = KPropertyPath(this,__CurrentType)

    val answers: KCollectionSimplePropertyPath<T, AnswerConfiguration?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::answers)

    val version: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Version)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val mandatoryEntities: KCollectionSimplePropertyPath<T,
            StoryDefinitionConfigurationMandatoryEntity?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::mandatoryEntities)

    val steps: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationStep?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::steps)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val userSentence: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__UserSentence)

    val userSentenceLocale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__UserSentenceLocale)

    val configurationName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ConfigurationName)

    val features: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationFeature?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::features)

    val _id: KPropertyPath<T, Id<StoryDefinitionConfiguration>?>
        get() = KPropertyPath(this,___id)

    val tags: KCollectionSimplePropertyPath<T, StoryTag?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::tags)

    val configuredAnswers: KCollectionSimplePropertyPath<T, DedicatedAnswerConfiguration?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::configuredAnswers)

    val configuredSteps: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationByBotStep?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::configuredSteps)

    companion object {
        val StoryId: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __StoryId
        val BotId: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __BotId
        val Intent: IntentWithoutNamespace_<StoryDefinitionConfiguration>
            get() = IntentWithoutNamespace_(null,__Intent)
        val CurrentType: KProperty1<StoryDefinitionConfiguration, AnswerConfigurationType?>
            get() = __CurrentType
        val Answers: KCollectionSimplePropertyPath<StoryDefinitionConfiguration,
                AnswerConfiguration?>
            get() = KCollectionSimplePropertyPath(null, __Answers)
        val Version: KProperty1<StoryDefinitionConfiguration, Int?>
            get() = __Version
        val Namespace: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __Namespace
        val MandatoryEntities: KCollectionSimplePropertyPath<StoryDefinitionConfiguration,
                StoryDefinitionConfigurationMandatoryEntity?>
            get() = KCollectionSimplePropertyPath(null, __MandatoryEntities)
        val Steps: KCollectionSimplePropertyPath<StoryDefinitionConfiguration,
                StoryDefinitionConfigurationStep?>
            get() = KCollectionSimplePropertyPath(null, __Steps)
        val Name: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __Name
        val Category: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __Category
        val Description: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __Description
        val UserSentence: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __UserSentence
        val UserSentenceLocale: KProperty1<StoryDefinitionConfiguration, Locale?>
            get() = __UserSentenceLocale
        val ConfigurationName: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __ConfigurationName
        val Features: KCollectionSimplePropertyPath<StoryDefinitionConfiguration,
                StoryDefinitionConfigurationFeature?>
            get() = KCollectionSimplePropertyPath(null, __Features)
        val _id: KProperty1<StoryDefinitionConfiguration, Id<StoryDefinitionConfiguration>?>
            get() = ___id
        val Tags: KCollectionSimplePropertyPath<StoryDefinitionConfiguration, StoryTag?>
            get() = KCollectionSimplePropertyPath(null, __Tags)
        val ConfiguredAnswers: KCollectionSimplePropertyPath<StoryDefinitionConfiguration,
                DedicatedAnswerConfiguration?>
            get() = KCollectionSimplePropertyPath(null, __ConfiguredAnswers)
        val ConfiguredSteps: KCollectionSimplePropertyPath<StoryDefinitionConfiguration,
                StoryDefinitionConfigurationByBotStep?>
            get() = KCollectionSimplePropertyPath(null, __ConfiguredSteps)}
}

class StoryDefinitionConfiguration_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<StoryDefinitionConfiguration>?>) : KCollectionPropertyPath<T,
        StoryDefinitionConfiguration?, StoryDefinitionConfiguration_<T>>(previous,property) {
    val storyId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryId)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val intent: IntentWithoutNamespace_<T>
        get() = IntentWithoutNamespace_(this,StoryDefinitionConfiguration::intent)

    val currentType: KPropertyPath<T, AnswerConfigurationType?>
        get() = KPropertyPath(this,__CurrentType)

    val answers: KCollectionSimplePropertyPath<T, AnswerConfiguration?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::answers)

    val version: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Version)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val mandatoryEntities: KCollectionSimplePropertyPath<T,
            StoryDefinitionConfigurationMandatoryEntity?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::mandatoryEntities)

    val steps: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationStep?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::steps)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val userSentence: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__UserSentence)

    val userSentenceLocale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__UserSentenceLocale)

    val configurationName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ConfigurationName)

    val features: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationFeature?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::features)

    val _id: KPropertyPath<T, Id<StoryDefinitionConfiguration>?>
        get() = KPropertyPath(this,___id)

    val tags: KCollectionSimplePropertyPath<T, StoryTag?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::tags)

    val configuredAnswers: KCollectionSimplePropertyPath<T, DedicatedAnswerConfiguration?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::configuredAnswers)

    val configuredSteps: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationByBotStep?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::configuredSteps)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): StoryDefinitionConfiguration_<T>
            = StoryDefinitionConfiguration_(this, customProperty(this, additionalPath))}

class StoryDefinitionConfiguration_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, StoryDefinitionConfiguration>?>) : KMapPropertyPath<T, K,
        StoryDefinitionConfiguration?, StoryDefinitionConfiguration_<T>>(previous,property) {
    val storyId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryId)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val intent: IntentWithoutNamespace_<T>
        get() = IntentWithoutNamespace_(this,StoryDefinitionConfiguration::intent)

    val currentType: KPropertyPath<T, AnswerConfigurationType?>
        get() = KPropertyPath(this,__CurrentType)

    val answers: KCollectionSimplePropertyPath<T, AnswerConfiguration?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::answers)

    val version: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Version)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val mandatoryEntities: KCollectionSimplePropertyPath<T,
            StoryDefinitionConfigurationMandatoryEntity?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::mandatoryEntities)

    val steps: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationStep?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::steps)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val userSentence: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__UserSentence)

    val userSentenceLocale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__UserSentenceLocale)

    val configurationName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ConfigurationName)

    val features: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationFeature?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::features)

    val _id: KPropertyPath<T, Id<StoryDefinitionConfiguration>?>
        get() = KPropertyPath(this,___id)

    val tags: KCollectionSimplePropertyPath<T, StoryTag?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::tags)

    val configuredAnswers: KCollectionSimplePropertyPath<T, DedicatedAnswerConfiguration?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::configuredAnswers)

    val configuredSteps: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationByBotStep?>
        get() = KCollectionSimplePropertyPath(this,StoryDefinitionConfiguration::configuredSteps)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): StoryDefinitionConfiguration_<T>
            = StoryDefinitionConfiguration_(this, customProperty(this, additionalPath))}
