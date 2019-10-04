package ai.tock.bot.mongo

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
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.engine.user.PlayerId::class.java)
                ),
                true,
                null
                )
                .serialize(_playerIds_, gen, serializers)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("state")
        val _state_ = value.state
        serializers.defaultSerializeValue(_state_, gen)
        gen.writeFieldName("stories")
        val _stories_ = value.stories
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.mongo.DialogCol.StoryMongoWrapper::class.java)
                ),
                true,
                null
                )
                .serialize(_stories_, gen, serializers)
        gen.writeFieldName("applicationIds")
        val _applicationIds_ = value.applicationIds
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java)
                ),
                true,
                null
                )
                .serialize(_applicationIds_, gen, serializers)
        gen.writeFieldName("lastUpdateDate")
        val _lastUpdateDate_ = value.lastUpdateDate
        serializers.defaultSerializeValue(_lastUpdateDate_, gen)
        gen.writeFieldName("groupId")
        val _groupId_ = value.groupId
        if(_groupId_ == null) { gen.writeNull() } else {
                gen.writeString(_groupId_)
                }
        gen.writeFieldName("test")
        val _test_ = value.test
        gen.writeBoolean(_test_)
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        if(_namespace_ == null) { gen.writeNull() } else {
                gen.writeString(_namespace_)
                }
        gen.writeEndObject()
    }
}
