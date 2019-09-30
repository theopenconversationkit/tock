package ai.tock.translator

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class I18nLabelStat_Serializer : StdSerializer<I18nLabelStat>(I18nLabelStat::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(I18nLabelStat::class.java, this)

    override fun serialize(
        value: I18nLabelStat,
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
        gen.writeFieldName("count")
        val _count_ = value.count
        gen.writeNumber(_count_)
        gen.writeFieldName("lastUpdate")
        val _lastUpdate_ = value.lastUpdate
        serializers.defaultSerializeValue(_lastUpdate_, gen)
        gen.writeEndObject()
    }
}
