package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ScenarioVersionCol_Serializer :
        StdSerializer<ScenarioVersionCol>(ScenarioVersionCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(ScenarioVersionCol::class.java, this)

    override fun serialize(
        value: ScenarioVersionCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("version")
        val _version_ = value.version
        serializers.defaultSerializeValue(_version_, gen)
        gen.writeFieldName("name")
        val _name_ = value.name
        gen.writeString(_name_)
        gen.writeFieldName("category")
        val _category_ = value.category
        if(_category_ == null) { gen.writeNull() } else {
                gen.writeString(_category_)
                }
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
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        gen.writeString(_applicationId_)
        gen.writeFieldName("creationDate")
        val _creationDate_ = value.creationDate
        if(_creationDate_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_creationDate_, gen)
                }
        gen.writeFieldName("updateDate")
        val _updateDate_ = value.updateDate
        if(_updateDate_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_updateDate_, gen)
                }
        gen.writeFieldName("description")
        val _description_ = value.description
        if(_description_ == null) { gen.writeNull() } else {
                gen.writeString(_description_)
                }
        gen.writeFieldName("data")
        val _data_ = value.data
        if(_data_ == null) { gen.writeNull() } else {
                gen.writeString(_data_)
                }
        gen.writeFieldName("state")
        val _state_ = value.state
        serializers.defaultSerializeValue(_state_, gen)
        gen.writeEndObject()
    }
}
