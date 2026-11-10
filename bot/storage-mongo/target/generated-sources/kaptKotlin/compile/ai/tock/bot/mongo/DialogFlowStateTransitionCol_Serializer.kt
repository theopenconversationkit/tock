package ai.tock.bot.mongo

import kotlin.String
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class DialogFlowStateTransitionCol_Serializer : StdSerializer<DialogFlowStateTransitionCol>(DialogFlowStateTransitionCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(DialogFlowStateTransitionCol::class.java, this)

  override fun serialize(
    `value`: DialogFlowStateTransitionCol,
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
    gen.writeName("previousStateId")
    val _previousStateId_ = value.previousStateId
    if (_previousStateId_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _previousStateId_)
    }
    gen.writeName("nextStateId")
    val _nextStateId_ = value.nextStateId
    serializers.writeValue(gen, _nextStateId_)
    gen.writeName("intent")
    val _intent_ = value.intent
    if (_intent_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_intent_)
    }
    gen.writeName("step")
    val _step_ = value.step
    if (_step_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_step_)
    }
    gen.writeName("newEntities")
    val _newEntities_ = value.newEntities
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(String::class.javaObjectType)), true).serialize(_newEntities_, gen, serializers)
    gen.writeName("type")
    val _type_ = value.type
    serializers.writeValue(gen, _type_)
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeEndObject()
  }
}
