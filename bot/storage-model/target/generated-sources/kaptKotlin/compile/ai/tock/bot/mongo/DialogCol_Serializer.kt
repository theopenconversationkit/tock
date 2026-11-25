package ai.tock.bot.mongo

import ai.tock.bot.engine.user.PlayerId
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class DialogCol_Serializer : StdSerializer<DialogCol>(DialogCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(DialogCol::class.java, this)

  override fun serialize(
    `value`: DialogCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("playerIds")
    val _playerIds_ = value.playerIds
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(PlayerId::class.javaObjectType)), true).serialize(_playerIds_, gen, serializers)
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("state")
    val _state_ = value.state
    serializers.writeValue(gen, _state_)
    gen.writeName("stories")
    val _stories_ = value.stories
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(DialogCol.StoryMongoWrapper::class.javaObjectType)), true).serialize(_stories_, gen, serializers)
    gen.writeName("applicationIds")
    val _applicationIds_ = value.applicationIds
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(String::class.javaObjectType)), true).serialize(_applicationIds_, gen, serializers)
    gen.writeName("lastUpdateDate")
    val _lastUpdateDate_ = value.lastUpdateDate
    serializers.writeValue(gen, _lastUpdateDate_)
    gen.writeName("groupId")
    val _groupId_ = value.groupId
    if (_groupId_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_groupId_)
    }
    gen.writeName("test")
    val _test_ = value.test
    gen.writeBoolean(_test_)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    if (_namespace_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_namespace_)
    }
    gen.writeName("rating")
    val _rating_ = value.rating
    if (_rating_ == null) {
      gen.writeNull()
    } else {
      gen.writeNumber(_rating_)
    }
    gen.writeName("review")
    val _review_ = value.review
    if (_review_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_review_)
    }
    gen.writeEndObject()
  }
}
