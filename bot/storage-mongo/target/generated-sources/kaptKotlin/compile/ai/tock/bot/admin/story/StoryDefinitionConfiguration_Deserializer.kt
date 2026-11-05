package ai.tock.bot.admin.story

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.DedicatedAnswerConfiguration
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.StoryTag
import ai.tock.nlp.api.client.model.NlpIntentQualifier
import java.util.Locale
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class StoryDefinitionConfiguration_Deserializer : ValueDeserializer<StoryDefinitionConfiguration>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(StoryDefinitionConfiguration::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): StoryDefinitionConfiguration {
    with(p) {
      var _storyId_: String? = null
      var _storyId_set: Boolean = false
      var _botId_: String? = null
      var _botId_set: Boolean = false
      var _intent_: IntentWithoutNamespace? = null
      var _intent_set: Boolean = false
      var _currentType_: AnswerConfigurationType? = null
      var _currentType_set: Boolean = false
      var _answers_: List<AnswerConfiguration>? = null
      var _answers_set: Boolean = false
      var _version_: Int? = null
      var _version_set: Boolean = false
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _mandatoryEntities_: List<StoryDefinitionConfigurationMandatoryEntity>? = null
      var _mandatoryEntities_set: Boolean = false
      var _steps_: List<StoryDefinitionConfigurationStep>? = null
      var _steps_set: Boolean = false
      var _name_: String? = null
      var _name_set: Boolean = false
      var _category_: String? = null
      var _category_set: Boolean = false
      var _description_: String? = null
      var _description_set: Boolean = false
      var _userSentence_: String? = null
      var _userSentence_set: Boolean = false
      var _userSentenceLocale_: Locale? = null
      var _userSentenceLocale_set: Boolean = false
      var _configurationName_: String? = null
      var _configurationName_set: Boolean = false
      var _features_: List<StoryDefinitionConfigurationFeature>? = null
      var _features_set: Boolean = false
      var __id_: Id<StoryDefinitionConfiguration>? = null
      var __id_set: Boolean = false
      var _tags_: Set<StoryTag>? = null
      var _tags_set: Boolean = false
      var _configuredAnswers_: List<DedicatedAnswerConfiguration>? = null
      var _configuredAnswers_set: Boolean = false
      var _configuredSteps_: List<StoryDefinitionConfigurationByBotStep>? = null
      var _configuredSteps_set: Boolean = false
      var _nextIntentsQualifiers_: List<NlpIntentQualifier>? = null
      var _nextIntentsQualifiers_set: Boolean = false
      var _metricStory_: Boolean? = null
      var _metricStory_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "storyId" -> {
            _storyId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _storyId_set = true
          }
          "botId" -> {
            _botId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _botId_set = true
          }
          "intent" -> {
            _intent_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(IntentWithoutNamespace::class.javaObjectType)
            _intent_set = true
          }
          "currentType" -> {
            _currentType_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(AnswerConfigurationType::class.javaObjectType)
            _currentType_set = true
          }
          "answers" -> {
            _answers_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_answers__reference)
            _answers_set = true
          }
          "version" -> {
            _version_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _version_set = true
          }
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "mandatoryEntities" -> {
            _mandatoryEntities_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_mandatoryEntities__reference)
            _mandatoryEntities_set = true
          }
          "steps" -> {
            _steps_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_steps__reference)
            _steps_set = true
          }
          "name" -> {
            _name_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _name_set = true
          }
          "category" -> {
            _category_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _category_set = true
          }
          "description" -> {
            _description_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _description_set = true
          }
          "userSentence" -> {
            _userSentence_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _userSentence_set = true
          }
          "userSentenceLocale" -> {
            _userSentenceLocale_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Locale::class.javaObjectType)
            _userSentenceLocale_set = true
          }
          "configurationName" -> {
            _configurationName_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _configurationName_set = true
          }
          "features" -> {
            _features_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_features__reference)
            _features_set = true
          }
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(__id__reference)
            __id_set = true
          }
          "tags" -> {
            _tags_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_tags__reference)
            _tags_set = true
          }
          "configuredAnswers" -> {
            _configuredAnswers_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_configuredAnswers__reference)
            _configuredAnswers_set = true
          }
          "configuredSteps" -> {
            _configuredSteps_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_configuredSteps__reference)
            _configuredSteps_set = true
          }
          "nextIntentsQualifiers" -> {
            _nextIntentsQualifiers_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_nextIntentsQualifiers__reference)
            _nextIntentsQualifiers_set = true
          }
          "metricStory" -> {
            _metricStory_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _metricStory_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_storyId_set && _botId_set && _intent_set && _currentType_set && _answers_set && _version_set && _namespace_set && _mandatoryEntities_set && _steps_set && _name_set && _category_set && _description_set && _userSentence_set && _userSentenceLocale_set && _configurationName_set && _features_set && __id_set && _tags_set && _configuredAnswers_set && _configuredSteps_set && _nextIntentsQualifiers_set && _metricStory_set) {
        StoryDefinitionConfiguration(storyId = _storyId_!!, botId = _botId_!!, intent = _intent_!!, currentType = _currentType_!!, answers = _answers_!!, version = _version_!!, namespace = _namespace_!!, mandatoryEntities = _mandatoryEntities_!!, steps = _steps_!!, name = _name_!!, category = _category_!!, description = _description_!!, userSentence = _userSentence_!!, userSentenceLocale = _userSentenceLocale_, configurationName = _configurationName_, features = _features_!!, _id = __id_!!, tags = _tags_!!, configuredAnswers = _configuredAnswers_!!, configuredSteps = _configuredSteps_!!, nextIntentsQualifiers = _nextIntentsQualifiers_!!, metricStory = _metricStory_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_storyId_set) map_[parameters.getValue("storyId")] = _storyId_
        if (_botId_set) map_[parameters.getValue("botId")] = _botId_
        if (_intent_set) map_[parameters.getValue("intent")] = _intent_
        if (_currentType_set) map_[parameters.getValue("currentType")] = _currentType_
        if (_answers_set) map_[parameters.getValue("answers")] = _answers_
        if (_version_set) map_[parameters.getValue("version")] = _version_
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_mandatoryEntities_set) map_[parameters.getValue("mandatoryEntities")] = _mandatoryEntities_
        if (_steps_set) map_[parameters.getValue("steps")] = _steps_
        if (_name_set) map_[parameters.getValue("name")] = _name_
        if (_category_set) map_[parameters.getValue("category")] = _category_
        if (_description_set) map_[parameters.getValue("description")] = _description_
        if (_userSentence_set) map_[parameters.getValue("userSentence")] = _userSentence_
        if (_userSentenceLocale_set) map_[parameters.getValue("userSentenceLocale")] = _userSentenceLocale_
        if (_configurationName_set) map_[parameters.getValue("configurationName")] = _configurationName_
        if (_features_set) map_[parameters.getValue("features")] = _features_
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_tags_set) map_[parameters.getValue("tags")] = _tags_
        if (_configuredAnswers_set) map_[parameters.getValue("configuredAnswers")] = _configuredAnswers_
        if (_configuredSteps_set) map_[parameters.getValue("configuredSteps")] = _configuredSteps_
        if (_nextIntentsQualifiers_set) map_[parameters.getValue("nextIntentsQualifiers")] = _nextIntentsQualifiers_
        if (_metricStory_set) map_[parameters.getValue("metricStory")] = _metricStory_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<StoryDefinitionConfiguration> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { StoryDefinitionConfiguration::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "storyId" to primaryConstructor.findParameterByName("storyId")!!,
            "botId" to primaryConstructor.findParameterByName("botId")!!,
            "intent" to primaryConstructor.findParameterByName("intent")!!,
            "currentType" to primaryConstructor.findParameterByName("currentType")!!,
            "answers" to primaryConstructor.findParameterByName("answers")!!,
            "version" to primaryConstructor.findParameterByName("version")!!,
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "mandatoryEntities" to primaryConstructor.findParameterByName("mandatoryEntities")!!,
            "steps" to primaryConstructor.findParameterByName("steps")!!,
            "name" to primaryConstructor.findParameterByName("name")!!,
            "category" to primaryConstructor.findParameterByName("category")!!,
            "description" to primaryConstructor.findParameterByName("description")!!,
            "userSentence" to primaryConstructor.findParameterByName("userSentence")!!,
            "userSentenceLocale" to primaryConstructor.findParameterByName("userSentenceLocale")!!,
            "configurationName" to primaryConstructor.findParameterByName("configurationName")!!,
            "features" to primaryConstructor.findParameterByName("features")!!,
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "tags" to primaryConstructor.findParameterByName("tags")!!,
            "configuredAnswers" to primaryConstructor.findParameterByName("configuredAnswers")!!,
            "configuredSteps" to primaryConstructor.findParameterByName("configuredSteps")!!,
            "nextIntentsQualifiers" to primaryConstructor.findParameterByName("nextIntentsQualifiers")!!,
            "metricStory" to primaryConstructor.findParameterByName("metricStory")!!,
          )
        }

    private val _answers__reference: TypeReference<List<AnswerConfiguration>> =
        object : TypeReference<List<AnswerConfiguration>>() {}

    private val _mandatoryEntities__reference:
        TypeReference<List<StoryDefinitionConfigurationMandatoryEntity>> =
        object : TypeReference<List<StoryDefinitionConfigurationMandatoryEntity>>() {}

    private val _steps__reference: TypeReference<List<StoryDefinitionConfigurationStep>> =
        object : TypeReference<List<StoryDefinitionConfigurationStep>>() {}

    private val _features__reference: TypeReference<List<StoryDefinitionConfigurationFeature>> =
        object : TypeReference<List<StoryDefinitionConfigurationFeature>>() {}

    private val __id__reference: TypeReference<Id<StoryDefinitionConfiguration>> =
        object : TypeReference<Id<StoryDefinitionConfiguration>>() {}

    private val _tags__reference: TypeReference<Set<StoryTag>> =
        object : TypeReference<Set<StoryTag>>() {}

    private val _configuredAnswers__reference: TypeReference<List<DedicatedAnswerConfiguration>> =
        object : TypeReference<List<DedicatedAnswerConfiguration>>() {}

    private val _configuredSteps__reference:
        TypeReference<List<StoryDefinitionConfigurationByBotStep>> =
        object : TypeReference<List<StoryDefinitionConfigurationByBotStep>>() {}

    private val _nextIntentsQualifiers__reference: TypeReference<List<NlpIntentQualifier>> =
        object : TypeReference<List<NlpIntentQualifier>>() {}
  }
}
