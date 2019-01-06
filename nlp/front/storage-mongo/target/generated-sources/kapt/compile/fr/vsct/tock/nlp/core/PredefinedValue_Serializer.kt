package fr.vsct.tock.nlp.core

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class PredefinedValue_Serializer :
        StdSerializer<PredefinedValue>(PredefinedValue::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(PredefinedValue::class.java, this)

    override fun serialize(
        value: PredefinedValue,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("value")
        val _value_ = value.value
        gen.writeString(_value_)
        gen.writeFieldName("labels")
        val _labels_ = value.labels
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                        serializers.config.typeFactory.constructType(java.util.Locale::class.java)
                , serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                        serializers.config.typeFactory.constructType(kotlin.String::class.java)
                )

                )
                ,
                true,
                null
                )
                .serialize(_labels_, gen, serializers)
        gen.writeEndObject()
    }
}
