package ai.tock.bot.mongo

import kotlin.String
import kotlin.collections.MutableSet
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class UserTimelineCol_Serializer : StdSerializer<UserTimelineCol>(UserTimelineCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(UserTimelineCol::class.java, this)

  override fun serialize(
    `value`: UserTimelineCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("playerId")
    val _playerId_ = value.playerId
    serializers.writeValue(gen, _playerId_)
    gen.writeName("userPreferences")
    val _userPreferences_ = value.userPreferences
    serializers.writeValue(gen, _userPreferences_)
    gen.writeName("userState")
    val _userState_ = value.userState
    serializers.writeValue(gen, _userState_)
    gen.writeName("temporaryIds")
    val _temporaryIds_ = value.temporaryIds
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(MutableSet::class.java, serializers.typeFactory.constructType(String::class.javaObjectType)), true).serialize(_temporaryIds_, gen, serializers)
    gen.writeName("applicationIds")
    val _applicationIds_ = value.applicationIds
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(MutableSet::class.java, serializers.typeFactory.constructType(String::class.javaObjectType)), true).serialize(_applicationIds_, gen, serializers)
    gen.writeName("lastActionText")
    val _lastActionText_ = value.lastActionText
    if (_lastActionText_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_lastActionText_)
    }
    gen.writeName("lastUpdateDate")
    val _lastUpdateDate_ = value.lastUpdateDate
    serializers.writeValue(gen, _lastUpdateDate_)
    gen.writeName("lastUserActionDate")
    val _lastUserActionDate_ = value.lastUserActionDate
    serializers.writeValue(gen, _lastUserActionDate_)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    if (_namespace_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_namespace_)
    }
    gen.writeName("creationDate")
    val _creationDate_ = value.creationDate
    serializers.writeValue(gen, _creationDate_)
    gen.writeEndObject()
  }
}
