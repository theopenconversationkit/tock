package ai.tock.shared.cache.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class MongoCacheData_Serializer :
        StdSerializer<MongoCacheData>(MongoCacheData::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(MongoCacheData::class.java, this)

    override fun serialize(
        value: MongoCacheData,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("id")
        val _id_ = value.id
        serializers.defaultSerializeValue(_id_, gen)
        gen.writeFieldName("type")
        val _type_ = value.type
        gen.writeString(_type_)
        gen.writeFieldName("s")
        val _s_ = value.s
        if(_s_ == null) { gen.writeNull() } else {
                gen.writeString(_s_)
                }
        gen.writeFieldName("b")
        val _b_ = value.b
        if(_b_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_b_, gen)
                }
        gen.writeFieldName("a")
        val _a_ = value.a
        if(_a_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_a_, gen)
                }
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeEndObject()
    }
}
