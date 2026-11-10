package ai.tock.bot.admin.story

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.DedicatedAnswerConfiguration
import ai.tock.bot.definition.StoryTag
import ai.tock.nlp.api.client.model.NlpIntentQualifier
import kotlin.collections.List
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class StoryDefinitionConfiguration_Serializer : StdSerializer<StoryDefinitionConfiguration>(StoryDefinitionConfiguration::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(StoryDefinitionConfiguration::class.java, this)

  override fun serialize(
    `value`: StoryDefinitionConfiguration,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("storyId")
    val _storyId_ = value.storyId
    gen.writeString(_storyId_)
    gen.writeName("botId")
    val _botId_ = value.botId
    gen.writeString(_botId_)
    gen.writeName("intent")
    val _intent_ = value.intent
    serializers.writeValue(gen, _intent_)
    gen.writeName("currentType")
    val _currentType_ = value.currentType
    serializers.writeValue(gen, _currentType_)
    gen.writeName("answers")
    val _answers_ = value.answers
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(AnswerConfiguration::class.javaObjectType)), true).serialize(_answers_, gen, serializers)
    gen.writeName("version")
    val _version_ = value.version
    gen.writeNumber(_version_)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("mandatoryEntities")
    val _mandatoryEntities_ = value.mandatoryEntities
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(StoryDefinitionConfigurationMandatoryEntity::class.javaObjectType)), true).serialize(_mandatoryEntities_, gen, serializers)
    gen.writeName("steps")
    val _steps_ = value.steps
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(StoryDefinitionConfigurationStep::class.javaObjectType)), true).serialize(_steps_, gen, serializers)
    gen.writeName("name")
    val _name_ = value.name
    gen.writeString(_name_)
    gen.writeName("category")
    val _category_ = value.category
    gen.writeString(_category_)
    gen.writeName("description")
    val _description_ = value.description
    gen.writeString(_description_)
    gen.writeName("userSentence")
    val _userSentence_ = value.userSentence
    gen.writeString(_userSentence_)
    gen.writeName("userSentenceLocale")
    val _userSentenceLocale_ = value.userSentenceLocale
    if (_userSentenceLocale_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _userSentenceLocale_)
    }
    gen.writeName("configurationName")
    val _configurationName_ = value.configurationName
    if (_configurationName_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_configurationName_)
    }
    gen.writeName("features")
    val _features_ = value.features
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(StoryDefinitionConfigurationFeature::class.javaObjectType)), true).serialize(_features_, gen, serializers)
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("tags")
    val _tags_ = value.tags
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(StoryTag::class.javaObjectType)), true).serialize(_tags_, gen, serializers)
    gen.writeName("configuredAnswers")
    val _configuredAnswers_ = value.configuredAnswers
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(DedicatedAnswerConfiguration::class.javaObjectType)), true).serialize(_configuredAnswers_, gen, serializers)
    gen.writeName("configuredSteps")
    val _configuredSteps_ = value.configuredSteps
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(StoryDefinitionConfigurationByBotStep::class.javaObjectType)), true).serialize(_configuredSteps_, gen, serializers)
    gen.writeName("nextIntentsQualifiers")
    val _nextIntentsQualifiers_ = value.nextIntentsQualifiers
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(NlpIntentQualifier::class.javaObjectType)), true).serialize(_nextIntentsQualifiers_, gen, serializers)
    gen.writeName("metricStory")
    val _metricStory_ = value.metricStory
    gen.writeBoolean(_metricStory_)
    gen.writeEndObject()
  }
}
