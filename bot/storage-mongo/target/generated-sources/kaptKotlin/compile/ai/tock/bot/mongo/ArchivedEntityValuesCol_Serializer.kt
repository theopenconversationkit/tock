package ai.tock.bot.mongo

import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ArchivedEntityValuesCol_Serializer : StdSerializer<ArchivedEntityValuesCol>(ArchivedEntityValuesCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ArchivedEntityValuesCol::class.java, this)

  override fun serialize(
    `value`: ArchivedEntityValuesCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("values")
    val _values_ = value.values
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(ArchivedEntityValuesCol.ArchivedEntityValueWrapper::class.javaObjectType)), true).serialize(_values_, gen, serializers)
    gen.writeName("lastUpdateDate")
    val _lastUpdateDate_ = value.lastUpdateDate
    serializers.writeValue(gen, _lastUpdateDate_)
    gen.writeEndObject()
  }
}
