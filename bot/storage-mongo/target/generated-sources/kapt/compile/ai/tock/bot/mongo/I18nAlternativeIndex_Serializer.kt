package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class I18nAlternativeIndex_Serializer :
        StdSerializer<I18nAlternativeIndex>(I18nAlternativeIndex::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(I18nAlternativeIndex::class.java, this)

    override fun serialize(
        value: I18nAlternativeIndex,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("labelId")
        val _labelId_ = value.labelId
        serializers.defaultSerializeValue(_labelId_, gen)
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("locale")
        val _locale_ = value.locale
        serializers.defaultSerializeValue(_locale_, gen)
        gen.writeFieldName("interfaceType")
        val _interfaceType_ = value.interfaceType
        serializers.defaultSerializeValue(_interfaceType_, gen)
        gen.writeFieldName("connectorId")
        val _connectorId_ = value.connectorId
        if(_connectorId_ == null) { gen.writeNull() } else {
                gen.writeString(_connectorId_)
                }
        gen.writeFieldName("contextId")
        val _contextId_ = value.contextId
        gen.writeString(_contextId_)
        gen.writeFieldName("index")
        val _index_ = value.index
        gen.writeNumber(_index_)
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeEndObject()
    }
}
