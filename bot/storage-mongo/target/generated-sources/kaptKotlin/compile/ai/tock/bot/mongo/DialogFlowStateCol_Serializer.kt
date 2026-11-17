package ai.tock.bot.mongo

import kotlin.String
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class DialogFlowStateCol_Serializer : StdSerializer<DialogFlowStateCol>(DialogFlowStateCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(DialogFlowStateCol::class.java, this)

  override fun serialize(
    `value`: DialogFlowStateCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("botId")
    val _botId_ = value.botId
    gen.writeString(_botId_)
    gen.writeName("storyDefinitionId")
    val _storyDefinitionId_ = value.storyDefinitionId
    gen.writeString(_storyDefinitionId_)
    gen.writeName("intent")
    val _intent_ = value.intent
    gen.writeString(_intent_)
    gen.writeName("step")
    val _step_ = value.step
    if (_step_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_step_)
    }
    gen.writeName("entities")
    val _entities_ = value.entities
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(String::class.javaObjectType)), true).serialize(_entities_, gen, serializers)
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("storyType")
    val _storyType_ = value.storyType
    if (_storyType_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _storyType_)
    }
    gen.writeName("storyName")
    val _storyName_ = value.storyName
    gen.writeString(_storyName_)
    gen.writeEndObject()
  }
}
