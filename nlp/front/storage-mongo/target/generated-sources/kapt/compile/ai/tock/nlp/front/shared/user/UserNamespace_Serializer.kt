package ai.tock.nlp.front.shared.user

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class UserNamespace_Serializer : StdSerializer<UserNamespace>(UserNamespace::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(UserNamespace::class.java, this)

    override fun serialize(
        value: UserNamespace,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("login")
        val _login_ = value.login
        gen.writeString(_login_)
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("owner")
        val _owner_ = value.owner
        gen.writeBoolean(_owner_)
        gen.writeFieldName("current")
        val _current_ = value.current
        gen.writeBoolean(_current_)
        gen.writeEndObject()
    }
}
