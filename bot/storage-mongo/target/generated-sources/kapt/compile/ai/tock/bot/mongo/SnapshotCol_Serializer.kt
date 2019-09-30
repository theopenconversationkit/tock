package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class SnapshotCol_Serializer : StdSerializer<SnapshotCol>(SnapshotCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(SnapshotCol::class.java, this)

    override fun serialize(
        value: SnapshotCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("snapshots")
        val _snapshots_ = value.snapshots
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.engine.dialog.Snapshot::class.java)
                ),
                true,
                null
                )
                .serialize(_snapshots_, gen, serializers)
        gen.writeFieldName("lastUpdateDate")
        val _lastUpdateDate_ = value.lastUpdateDate
        serializers.defaultSerializeValue(_lastUpdateDate_, gen)
        gen.writeEndObject()
    }
}
