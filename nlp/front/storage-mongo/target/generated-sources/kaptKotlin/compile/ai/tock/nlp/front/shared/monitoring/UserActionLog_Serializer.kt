package ai.tock.nlp.front.shared.monitoring

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class UserActionLog_Serializer : StdSerializer<UserActionLog>(UserActionLog::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(UserActionLog::class.java, this)

  override fun serialize(
    `value`: UserActionLog,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("applicationId")
    val _applicationId_ = value.applicationId
    if (_applicationId_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _applicationId_)
    }
    gen.writeName("login")
    val _login_ = value.login
    gen.writeString(_login_)
    gen.writeName("actionType")
    val _actionType_ = value.actionType
    gen.writeString(_actionType_)
    gen.writeName("newData")
    val _newData_ = value.newData
    if (_newData_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _newData_)
    }
    gen.writeName("error")
    val _error_ = value.error
    gen.writeBoolean(_error_)
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeEndObject()
  }
}
