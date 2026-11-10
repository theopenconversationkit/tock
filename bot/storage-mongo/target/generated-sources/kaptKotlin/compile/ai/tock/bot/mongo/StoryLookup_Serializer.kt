package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class StoryLookup_Serializer : StdSerializer<StoryLookup>(StoryLookup::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(StoryLookup::class.java, this)

  override fun serialize(
    `value`: StoryLookup,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("story")
    val _story_ = value.story
    serializers.writeValue(gen, _story_)
    gen.writeEndObject()
  }
}
