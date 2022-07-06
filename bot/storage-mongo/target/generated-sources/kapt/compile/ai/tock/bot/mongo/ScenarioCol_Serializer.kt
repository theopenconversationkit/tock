package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ScenarioCol_Serializer : StdSerializer<ScenarioCol>(ScenarioCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(ScenarioCol::class.java, this)

    override fun serialize(
        value: ScenarioCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
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
        gen.writeFieldName("createDate")
        val _createDate_ = value.createDate
        if(_createDate_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_createDate_, gen)
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
        gen.writeString(_state_)
        gen.writeEndObject()
    }
}
