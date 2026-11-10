package ai.tock.bot.mongo

import ai.tock.shared.jackson.AnyValueWrapper
import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ConnectorMessageCol_Serializer : StdSerializer<ConnectorMessageCol>(ConnectorMessageCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ConnectorMessageCol::class.java, this)

  override fun serialize(
    `value`: ConnectorMessageCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("messages")
    val _messages_ = value.messages
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(AnyValueWrapper::class.javaObjectType)), true).serialize(_messages_, gen, serializers)
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeEndObject()
  }
}
