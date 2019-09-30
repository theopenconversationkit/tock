package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class UserLock_Serializer :
        StdSerializer<MongoUserLock.UserLock>(MongoUserLock.UserLock::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(MongoUserLock.UserLock::class.java, this)

    override fun serialize(
        value: MongoUserLock.UserLock,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("locked")
        val _locked_ = value.locked
        gen.writeBoolean(_locked_)
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeEndObject()
    }
}
