package ai.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class FaqDefinition_Serializer : StdSerializer<FaqDefinition>(FaqDefinition::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(FaqDefinition::class.java, this)

    override fun serialize(
        value: FaqDefinition,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("intentId")
        val _intentId_ = value.intentId
        serializers.defaultSerializeValue(_intentId_, gen)
        gen.writeFieldName("i18nId")
        val _i18nId_ = value.i18nId
        serializers.defaultSerializeValue(_i18nId_, gen)
        gen.writeFieldName("tags")
        val _tags_ = value.tags
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java)
                ),
                true,
                null
                )
                .serialize(_tags_, gen, serializers)
        gen.writeFieldName("enabled")
        val _enabled_ = value.enabled
        gen.writeBoolean(_enabled_)
        gen.writeFieldName("creationDate")
        val _creationDate_ = value.creationDate
        serializers.defaultSerializeValue(_creationDate_, gen)
        gen.writeFieldName("updateDate")
        val _updateDate_ = value.updateDate
        serializers.defaultSerializeValue(_updateDate_, gen)
        gen.writeEndObject()
    }
}
