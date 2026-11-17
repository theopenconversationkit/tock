package ai.tock.bot.mongo

import kotlin.String
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ClientIdCol_Serializer : StdSerializer<ClientIdCol>(ClientIdCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ClientIdCol::class.java, this)

  override fun serialize(
    `value`: ClientIdCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("userIds")
    val _userIds_ = value.userIds
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(String::class.javaObjectType)), true).serialize(_userIds_, gen, serializers)
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeEndObject()
  }
}
