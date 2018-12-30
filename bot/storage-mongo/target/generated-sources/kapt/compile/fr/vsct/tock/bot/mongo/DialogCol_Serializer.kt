package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class DialogCol_Serializer : StdSerializer<DialogCol>(DialogCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(DialogCol::class.java, this)

    override fun serialize(
        value: DialogCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("playerIds")
        val _playerIds_ = value.playerIds
        serializers.defaultSerializeValue(_playerIds_, gen)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("state")
        val _state_ = value.state
        serializers.defaultSerializeValue(_state_, gen)
        gen.writeFieldName("stories")
        val _stories_ = value.stories
        serializers.defaultSerializeValue(_stories_, gen)
        gen.writeFieldName("applicationIds")
        val _applicationIds_ = value.applicationIds
        serializers.defaultSerializeValue(_applicationIds_, gen)
        gen.writeFieldName("lastUpdateDate")
        val _lastUpdateDate_ = value.lastUpdateDate
        serializers.defaultSerializeValue(_lastUpdateDate_, gen)
        gen.writeFieldName("groupId")
        val _groupId_ = value.groupId
        if(_groupId_ == null) { gen.writeNull() } else {gen.writeString(_groupId_)}
        gen.writeEndObject()
    }
}
