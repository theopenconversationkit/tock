package ai.tock.nlp.front.shared.user

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class UserNamespace_Serializer : StdSerializer<UserNamespace>(UserNamespace::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(UserNamespace::class.java, this)

  override fun serialize(
    `value`: UserNamespace,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("login")
    val _login_ = value.login
    gen.writeString(_login_)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("owner")
    val _owner_ = value.owner
    gen.writeBoolean(_owner_)
    gen.writeName("current")
    val _current_ = value.current
    gen.writeBoolean(_current_)
    gen.writeName("label")
    val _label_ = value.label
    if (_label_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_label_)
    }
    gen.writeEndObject()
  }
}
