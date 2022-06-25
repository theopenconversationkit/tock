package ai.tock.bot.admin.story

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class StoryDefinitionConfiguration_Serializer :
        StdSerializer<StoryDefinitionConfiguration>(StoryDefinitionConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(StoryDefinitionConfiguration::class.java,
            this)

    override fun serialize(
        value: StoryDefinitionConfiguration,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("storyId")
        val _storyId_ = value.storyId
        gen.writeString(_storyId_)
        gen.writeFieldName("botId")
        val _botId_ = value.botId
        gen.writeString(_botId_)
        gen.writeFieldName("intent")
        val _intent_ = value.intent
        serializers.defaultSerializeValue(_intent_, gen)
        gen.writeFieldName("currentType")
        val _currentType_ = value.currentType
        serializers.defaultSerializeValue(_currentType_, gen)
        gen.writeFieldName("answers")
        val _answers_ = value.answers
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.admin.answer.AnswerConfiguration::class.java)
                ),
                true,
                null
                )
                .serialize(_answers_, gen, serializers)
        gen.writeFieldName("version")
        val _version_ = value.version
        gen.writeNumber(_version_)
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("mandatoryEntities")
        val _mandatoryEntities_ = value.mandatoryEntities
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.admin.story.StoryDefinitionConfigurationMandatoryEntity::class.java)
                ),
                true,
                null
                )
                .serialize(_mandatoryEntities_, gen, serializers)
        gen.writeFieldName("steps")
        val _steps_ = value.steps
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.admin.story.StoryDefinitionConfigurationStep::class.java)
                ),
                true,
                null
                )
                .serialize(_steps_, gen, serializers)
        gen.writeFieldName("name")
        val _name_ = value.name
        gen.writeString(_name_)
        gen.writeFieldName("category")
        val _category_ = value.category
        gen.writeString(_category_)
        gen.writeFieldName("description")
        val _description_ = value.description
        gen.writeString(_description_)
        gen.writeFieldName("userSentence")
        val _userSentence_ = value.userSentence
        gen.writeString(_userSentence_)
        gen.writeFieldName("userSentenceLocale")
        val _userSentenceLocale_ = value.userSentenceLocale
        if(_userSentenceLocale_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_userSentenceLocale_, gen)
                }
        gen.writeFieldName("configurationName")
        val _configurationName_ = value.configurationName
        if(_configurationName_ == null) { gen.writeNull() } else {
                gen.writeString(_configurationName_)
                }
        gen.writeFieldName("features")
        val _features_ = value.features
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature::class.java)
                ),
                true,
                null
                )
                .serialize(_features_, gen, serializers)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("tags")
        val _tags_ = value.tags
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.definition.StoryTag::class.java)
                ),
                true,
                null
                )
                .serialize(_tags_, gen, serializers)
        gen.writeFieldName("configuredAnswers")
        val _configuredAnswers_ = value.configuredAnswers
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.admin.answer.DedicatedAnswerConfiguration::class.java)
                ),
                true,
                null
                )
                .serialize(_configuredAnswers_, gen, serializers)
        gen.writeFieldName("configuredSteps")
        val _configuredSteps_ = value.configuredSteps
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.admin.story.StoryDefinitionConfigurationByBotStep::class.java)
                ),
                true,
                null
                )
                .serialize(_configuredSteps_, gen, serializers)
        gen.writeFieldName("nextIntentsQualifiers")
        val _nextIntentsQualifiers_ = value.nextIntentsQualifiers
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.nlp.api.client.model.NlpIntentQualifier::class.java)
                ),
                true,
                null
                )
                .serialize(_nextIntentsQualifiers_, gen, serializers)
        gen.writeEndObject()
    }
}
