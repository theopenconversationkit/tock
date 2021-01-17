package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class UserTimelineCol_Serializer :
        StdSerializer<UserTimelineCol>(UserTimelineCol::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(UserTimelineCol::class.java, this)

    override fun serialize(
        value: UserTimelineCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("playerId")
        val _playerId_ = value.playerId
        serializers.defaultSerializeValue(_playerId_, gen)
        gen.writeFieldName("userPreferences")
        val _userPreferences_ = value.userPreferences
        serializers.defaultSerializeValue(_userPreferences_, gen)
        gen.writeFieldName("userState")
        val _userState_ = value.userState
        serializers.defaultSerializeValue(_userState_, gen)
        gen.writeFieldName("temporaryIds")
        val _temporaryIds_ = value.temporaryIds
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java)
                ),
                true,
                null
                )
                .serialize(_temporaryIds_, gen, serializers)
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
        gen.writeFieldName("lastActionText")
        val _lastActionText_ = value.lastActionText
        if(_lastActionText_ == null) { gen.writeNull() } else {
                gen.writeString(_lastActionText_)
                }
        gen.writeFieldName("lastUpdateDate")
        val _lastUpdateDate_ = value.lastUpdateDate
        serializers.defaultSerializeValue(_lastUpdateDate_, gen)
        gen.writeFieldName("lastUserActionDate")
        val _lastUserActionDate_ = value.lastUserActionDate
        serializers.defaultSerializeValue(_lastUserActionDate_, gen)
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        if(_namespace_ == null) { gen.writeNull() } else {
                gen.writeString(_namespace_)
                }
        gen.writeFieldName("creationDate")
        val _creationDate_ = value.creationDate
        serializers.defaultSerializeValue(_creationDate_, gen)
        gen.writeEndObject()
    }
}
