@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.admin.story

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.DedicatedAnswerConfiguration
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.IntentWithoutNamespace_
import ai.tock.bot.definition.Intent_
import ai.tock.bot.definition.StoryTag
import ai.tock.nlp.api.client.model.NlpIntentQualifier
import java.util.Locale
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<StoryDefinitionConfiguration, Id<StoryDefinitionConfiguration>?>
  get() = StoryDefinitionConfiguration::_id

private val __Answers: KProperty1<StoryDefinitionConfiguration, List<AnswerConfiguration>?>
  get() = StoryDefinitionConfiguration::answers

private val __BotId: KProperty1<StoryDefinitionConfiguration, String?>
  get() = StoryDefinitionConfiguration::botId

private val __Category: KProperty1<StoryDefinitionConfiguration, String?>
  get() = StoryDefinitionConfiguration::category

private val __ConfigurationName: KProperty1<StoryDefinitionConfiguration, String?>
  get() = StoryDefinitionConfiguration::configurationName

private val __ConfiguredAnswers:
    KProperty1<StoryDefinitionConfiguration, List<DedicatedAnswerConfiguration>?>
  get() = StoryDefinitionConfiguration::configuredAnswers

private val __ConfiguredSteps:
    KProperty1<StoryDefinitionConfiguration, List<StoryDefinitionConfigurationByBotStep>?>
  get() = StoryDefinitionConfiguration::configuredSteps

private val __CurrentType: KProperty1<StoryDefinitionConfiguration, AnswerConfigurationType?>
  get() = StoryDefinitionConfiguration::currentType

private val __Description: KProperty1<StoryDefinitionConfiguration, String?>
  get() = StoryDefinitionConfiguration::description

private val __Features:
    KProperty1<StoryDefinitionConfiguration, List<StoryDefinitionConfigurationFeature>?>
  get() = StoryDefinitionConfiguration::features

private val __Intent: KProperty1<StoryDefinitionConfiguration, IntentWithoutNamespace?>
  get() = StoryDefinitionConfiguration::intent

private val __MainIntent: KProperty1<StoryDefinitionConfiguration, Intent?>
  get() = StoryDefinitionConfiguration::class.declaredMemberProperties.first { it.name == "mainIntent" } as KProperty1<StoryDefinitionConfiguration, Intent?>

private val __MandatoryEntities:
    KProperty1<StoryDefinitionConfiguration, List<StoryDefinitionConfigurationMandatoryEntity>?>
  get() = StoryDefinitionConfiguration::mandatoryEntities

private val __MetricStory: KProperty1<StoryDefinitionConfiguration, Boolean?>
  get() = StoryDefinitionConfiguration::metricStory

private val __Name: KProperty1<StoryDefinitionConfiguration, String?>
  get() = StoryDefinitionConfiguration::name

private val __Namespace: KProperty1<StoryDefinitionConfiguration, String?>
  get() = StoryDefinitionConfiguration::namespace

private val __NextIntentsQualifiers:
    KProperty1<StoryDefinitionConfiguration, List<NlpIntentQualifier>?>
  get() = StoryDefinitionConfiguration::nextIntentsQualifiers

private val __Steps:
    KProperty1<StoryDefinitionConfiguration, List<StoryDefinitionConfigurationStep>?>
  get() = StoryDefinitionConfiguration::steps

private val __StoryId: KProperty1<StoryDefinitionConfiguration, String?>
  get() = StoryDefinitionConfiguration::storyId

private val __Tags: KProperty1<StoryDefinitionConfiguration, Set<StoryTag>?>
  get() = StoryDefinitionConfiguration::tags

private val __UserSentence: KProperty1<StoryDefinitionConfiguration, String?>
  get() = StoryDefinitionConfiguration::userSentence

private val __UserSentenceLocale: KProperty1<StoryDefinitionConfiguration, Locale?>
  get() = StoryDefinitionConfiguration::userSentenceLocale

private val __Version: KProperty1<StoryDefinitionConfiguration, Int?>
  get() = StoryDefinitionConfiguration::version

public class StoryDefinitionConfiguration_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, StoryDefinitionConfiguration?>,
) : KPropertyPath<T, StoryDefinitionConfiguration?>(previous, property) {
  public val _id: KPropertyPath<T, Id<StoryDefinitionConfiguration>?>
    get() = KPropertyPath(this, ___id)

  public val answers: KCollectionSimplePropertyPath<T, AnswerConfiguration?>
    get() = KCollectionSimplePropertyPath(this, __Answers)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val category: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Category)

  public val configurationName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ConfigurationName)

  public val configuredAnswers: KCollectionSimplePropertyPath<T, DedicatedAnswerConfiguration?>
    get() = KCollectionSimplePropertyPath(this, __ConfiguredAnswers)

  public val configuredSteps:
      KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationByBotStep?>
    get() = KCollectionSimplePropertyPath(this, __ConfiguredSteps)

  public val currentType: KPropertyPath<T, AnswerConfigurationType?>
    get() = KPropertyPath(this, __CurrentType)

  public val description: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Description)

  public val features: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationFeature?>
    get() = KCollectionSimplePropertyPath(this, __Features)

  public val intent: IntentWithoutNamespace_<T>
    get() = IntentWithoutNamespace_(this, __Intent)

  public val mainIntent: Intent_<T>
    get() = Intent_(this, __MainIntent)

  public val mandatoryEntities:
      KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationMandatoryEntity?>
    get() = KCollectionSimplePropertyPath(this, __MandatoryEntities)

  public val metricStory: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __MetricStory)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nextIntentsQualifiers: KCollectionSimplePropertyPath<T, NlpIntentQualifier?>
    get() = KCollectionSimplePropertyPath(this, __NextIntentsQualifiers)

  public val steps: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationStep?>
    get() = KCollectionSimplePropertyPath(this, __Steps)

  public val storyId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryId)

  public val tags: KCollectionSimplePropertyPath<T, StoryTag?>
    get() = KCollectionSimplePropertyPath(this, __Tags)

  public val userSentence: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __UserSentence)

  public val userSentenceLocale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __UserSentenceLocale)

  public val version: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Version)

  public companion object {
    public val _id: KProperty1<StoryDefinitionConfiguration, Id<StoryDefinitionConfiguration>?>
      get() = ___id

    public val Answers:
        KCollectionSimplePropertyPath<StoryDefinitionConfiguration, AnswerConfiguration?>
      get() = KCollectionSimplePropertyPath(null, __Answers)

    public val BotId: KProperty1<StoryDefinitionConfiguration, String?>
      get() = __BotId

    public val Category: KProperty1<StoryDefinitionConfiguration, String?>
      get() = __Category

    public val ConfigurationName: KProperty1<StoryDefinitionConfiguration, String?>
      get() = __ConfigurationName

    public val ConfiguredAnswers:
        KCollectionSimplePropertyPath<StoryDefinitionConfiguration, DedicatedAnswerConfiguration?>
      get() = KCollectionSimplePropertyPath(null, __ConfiguredAnswers)

    public val ConfiguredSteps:
        KCollectionSimplePropertyPath<StoryDefinitionConfiguration, StoryDefinitionConfigurationByBotStep?>
      get() = KCollectionSimplePropertyPath(null, __ConfiguredSteps)

    public val CurrentType: KProperty1<StoryDefinitionConfiguration, AnswerConfigurationType?>
      get() = __CurrentType

    public val Description: KProperty1<StoryDefinitionConfiguration, String?>
      get() = __Description

    public val Features:
        KCollectionSimplePropertyPath<StoryDefinitionConfiguration, StoryDefinitionConfigurationFeature?>
      get() = KCollectionSimplePropertyPath(null, __Features)

    public val Intent: IntentWithoutNamespace_<StoryDefinitionConfiguration>
      get() = IntentWithoutNamespace_(null, __Intent)

    public val MainIntent: Intent_<StoryDefinitionConfiguration>
      get() = Intent_(null, __MainIntent)

    public val MandatoryEntities:
        KCollectionSimplePropertyPath<StoryDefinitionConfiguration, StoryDefinitionConfigurationMandatoryEntity?>
      get() = KCollectionSimplePropertyPath(null, __MandatoryEntities)

    public val MetricStory: KProperty1<StoryDefinitionConfiguration, Boolean?>
      get() = __MetricStory

    public val Name: KProperty1<StoryDefinitionConfiguration, String?>
      get() = __Name

    public val Namespace: KProperty1<StoryDefinitionConfiguration, String?>
      get() = __Namespace

    public val NextIntentsQualifiers:
        KCollectionSimplePropertyPath<StoryDefinitionConfiguration, NlpIntentQualifier?>
      get() = KCollectionSimplePropertyPath(null, __NextIntentsQualifiers)

    public val Steps:
        KCollectionSimplePropertyPath<StoryDefinitionConfiguration, StoryDefinitionConfigurationStep?>
      get() = KCollectionSimplePropertyPath(null, __Steps)

    public val StoryId: KProperty1<StoryDefinitionConfiguration, String?>
      get() = __StoryId

    public val Tags: KCollectionSimplePropertyPath<StoryDefinitionConfiguration, StoryTag?>
      get() = KCollectionSimplePropertyPath(null, __Tags)

    public val UserSentence: KProperty1<StoryDefinitionConfiguration, String?>
      get() = __UserSentence

    public val UserSentenceLocale: KProperty1<StoryDefinitionConfiguration, Locale?>
      get() = __UserSentenceLocale

    public val Version: KProperty1<StoryDefinitionConfiguration, Int?>
      get() = __Version
  }
}

public class StoryDefinitionConfiguration_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<StoryDefinitionConfiguration?>?>,
) : KCollectionPropertyPath<T, StoryDefinitionConfiguration?, StoryDefinitionConfiguration_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<StoryDefinitionConfiguration>?>
    get() = KPropertyPath(this, ___id)

  public val answers: KCollectionSimplePropertyPath<T, AnswerConfiguration?>
    get() = KCollectionSimplePropertyPath(this, __Answers)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val category: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Category)

  public val configurationName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ConfigurationName)

  public val configuredAnswers: KCollectionSimplePropertyPath<T, DedicatedAnswerConfiguration?>
    get() = KCollectionSimplePropertyPath(this, __ConfiguredAnswers)

  public val configuredSteps:
      KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationByBotStep?>
    get() = KCollectionSimplePropertyPath(this, __ConfiguredSteps)

  public val currentType: KPropertyPath<T, AnswerConfigurationType?>
    get() = KPropertyPath(this, __CurrentType)

  public val description: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Description)

  public val features: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationFeature?>
    get() = KCollectionSimplePropertyPath(this, __Features)

  public val intent: IntentWithoutNamespace_<T>
    get() = IntentWithoutNamespace_(this, __Intent)

  public val mainIntent: Intent_<T>
    get() = Intent_(this, __MainIntent)

  public val mandatoryEntities:
      KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationMandatoryEntity?>
    get() = KCollectionSimplePropertyPath(this, __MandatoryEntities)

  public val metricStory: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __MetricStory)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nextIntentsQualifiers: KCollectionSimplePropertyPath<T, NlpIntentQualifier?>
    get() = KCollectionSimplePropertyPath(this, __NextIntentsQualifiers)

  public val steps: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationStep?>
    get() = KCollectionSimplePropertyPath(this, __Steps)

  public val storyId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryId)

  public val tags: KCollectionSimplePropertyPath<T, StoryTag?>
    get() = KCollectionSimplePropertyPath(this, __Tags)

  public val userSentence: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __UserSentence)

  public val userSentenceLocale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __UserSentenceLocale)

  public val version: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Version)

  override fun memberWithAdditionalPath(additionalPath: String): StoryDefinitionConfiguration_<T> = StoryDefinitionConfiguration_(this, customProperty(this, additionalPath))
}

public class StoryDefinitionConfiguration_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, StoryDefinitionConfiguration?>?>,
) : KMapPropertyPath<T, K, StoryDefinitionConfiguration?, StoryDefinitionConfiguration_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<StoryDefinitionConfiguration>?>
    get() = KPropertyPath(this, ___id)

  public val answers: KCollectionSimplePropertyPath<T, AnswerConfiguration?>
    get() = KCollectionSimplePropertyPath(this, __Answers)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val category: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Category)

  public val configurationName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ConfigurationName)

  public val configuredAnswers: KCollectionSimplePropertyPath<T, DedicatedAnswerConfiguration?>
    get() = KCollectionSimplePropertyPath(this, __ConfiguredAnswers)

  public val configuredSteps:
      KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationByBotStep?>
    get() = KCollectionSimplePropertyPath(this, __ConfiguredSteps)

  public val currentType: KPropertyPath<T, AnswerConfigurationType?>
    get() = KPropertyPath(this, __CurrentType)

  public val description: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Description)

  public val features: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationFeature?>
    get() = KCollectionSimplePropertyPath(this, __Features)

  public val intent: IntentWithoutNamespace_<T>
    get() = IntentWithoutNamespace_(this, __Intent)

  public val mainIntent: Intent_<T>
    get() = Intent_(this, __MainIntent)

  public val mandatoryEntities:
      KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationMandatoryEntity?>
    get() = KCollectionSimplePropertyPath(this, __MandatoryEntities)

  public val metricStory: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __MetricStory)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nextIntentsQualifiers: KCollectionSimplePropertyPath<T, NlpIntentQualifier?>
    get() = KCollectionSimplePropertyPath(this, __NextIntentsQualifiers)

  public val steps: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationStep?>
    get() = KCollectionSimplePropertyPath(this, __Steps)

  public val storyId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryId)

  public val tags: KCollectionSimplePropertyPath<T, StoryTag?>
    get() = KCollectionSimplePropertyPath(this, __Tags)

  public val userSentence: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __UserSentence)

  public val userSentenceLocale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __UserSentenceLocale)

  public val version: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Version)

  override fun memberWithAdditionalPath(additionalPath: String): StoryDefinitionConfiguration_<T> = StoryDefinitionConfiguration_(this, customProperty(this, additionalPath))
}
