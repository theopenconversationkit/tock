package fr.vsct.tock.nlp.core

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class DictionaryData_Serializer :
        StdSerializer<DictionaryData>(DictionaryData::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(DictionaryData::class.java, this)

    override fun serialize(
        value: DictionaryData,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("entityName")
        val _entityName_ = value.entityName
        gen.writeString(_entityName_)
        gen.writeFieldName("values")
        val _values_ = value.values
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(fr.vsct.tock.nlp.core.PredefinedValue::class.java)
                ),
                true,
                null
                )
                .serialize(_values_, gen, serializers)
        gen.writeFieldName("onlyValues")
        val _onlyValues_ = value.onlyValues
        gen.writeBoolean(_onlyValues_)
        gen.writeFieldName("minDistance")
        val _minDistance_ = value.minDistance
        gen.writeNumber(_minDistance_)
        gen.writeFieldName("textSearch")
        val _textSearch_ = value.textSearch
        gen.writeBoolean(_textSearch_)
        gen.writeEndObject()
    }
}
