package ai.tock.translator

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class I18nLabel_Serializer : StdSerializer<I18nLabel>(I18nLabel::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(I18nLabel::class.java, this)

    override fun serialize(
        value: I18nLabel,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("category")
        val _category_ = value.category
        gen.writeString(_category_)
        gen.writeFieldName("i18n")
        val _i18n_ = value.i18n
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                java.util.LinkedHashSet::class.java,
                serializers.config.typeFactory.constructType(ai.tock.translator.I18nLocalizedLabel::class.java)
                ),
                true,
                null
                )
                .serialize(_i18n_, gen, serializers)
        gen.writeFieldName("defaultLabel")
        val _defaultLabel_ = value.defaultLabel
        if(_defaultLabel_ == null) { gen.writeNull() } else {
                gen.writeString(_defaultLabel_)
                }
        gen.writeFieldName("defaultLocale")
        val _defaultLocale_ = value.defaultLocale
        serializers.defaultSerializeValue(_defaultLocale_, gen)
        gen.writeFieldName("version")
        val _version_ = value.version
        gen.writeNumber(_version_)
        gen.writeEndObject()
    }
}
