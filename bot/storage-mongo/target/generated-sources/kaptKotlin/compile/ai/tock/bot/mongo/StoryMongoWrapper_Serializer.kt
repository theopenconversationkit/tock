package ai.tock.bot.mongo

import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class StoryMongoWrapper_Serializer : StdSerializer<DialogCol.StoryMongoWrapper>(DialogCol.StoryMongoWrapper::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(DialogCol.StoryMongoWrapper::class.java, this)

  override fun serialize(
    `value`: DialogCol.StoryMongoWrapper,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("storyDefinitionId")
    val _storyDefinitionId_ = value.storyDefinitionId
    gen.writeString(_storyDefinitionId_)
    gen.writeName("currentIntent")
    val _currentIntent_ = value.currentIntent
    if (_currentIntent_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _currentIntent_)
    }
    gen.writeName("currentStep")
    val _currentStep_ = value.currentStep
    if (_currentStep_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_currentStep_)
    }
    gen.writeName("actions")
    val _actions_ = value.actions
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(DialogCol.ActionMongoWrapper::class.javaObjectType)), true).serialize(_actions_, gen, serializers)
    gen.writeEndObject()
  }
}
