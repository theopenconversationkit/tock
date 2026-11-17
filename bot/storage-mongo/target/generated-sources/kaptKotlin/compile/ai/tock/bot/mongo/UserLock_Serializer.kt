package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class UserLock_Serializer : StdSerializer<MongoUserLock.UserLock>(MongoUserLock.UserLock::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(MongoUserLock.UserLock::class.java, this)

  override fun serialize(
    `value`: MongoUserLock.UserLock,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("locked")
    val _locked_ = value.locked
    gen.writeBoolean(_locked_)
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeName("lockId")
    val _lockId_ = value.lockId
    if (_lockId_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_lockId_)
    }
    gen.writeEndObject()
  }
}
