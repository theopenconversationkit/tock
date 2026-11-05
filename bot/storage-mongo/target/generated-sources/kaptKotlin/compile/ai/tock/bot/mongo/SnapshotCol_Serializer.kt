package ai.tock.bot.mongo

import ai.tock.bot.engine.dialog.Snapshot
import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class SnapshotCol_Serializer : StdSerializer<SnapshotCol>(SnapshotCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(SnapshotCol::class.java, this)

  override fun serialize(
    `value`: SnapshotCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("snapshots")
    val _snapshots_ = value.snapshots
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(Snapshot::class.javaObjectType)), true).serialize(_snapshots_, gen, serializers)
    gen.writeName("lastUpdateDate")
    val _lastUpdateDate_ = value.lastUpdateDate
    serializers.writeValue(gen, _lastUpdateDate_)
    gen.writeEndObject()
  }
}
