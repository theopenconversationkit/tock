package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ArchivedEntityValuesCol_Serializer :
        StdSerializer<ArchivedEntityValuesCol>(ArchivedEntityValuesCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(ArchivedEntityValuesCol::class.java, this)

    override fun serialize(
        value: ArchivedEntityValuesCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("values")
        val _values_ = value.values
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.mongo.ArchivedEntityValuesCol.ArchivedEntityValueWrapper::class.java)
                ),
                true,
                null
                )
                .serialize(_values_, gen, serializers)
        gen.writeFieldName("lastUpdateDate")
        val _lastUpdateDate_ = value.lastUpdateDate
        serializers.defaultSerializeValue(_lastUpdateDate_, gen)
        gen.writeEndObject()
    }
}
