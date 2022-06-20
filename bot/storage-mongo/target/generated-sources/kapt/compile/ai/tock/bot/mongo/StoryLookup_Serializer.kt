package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class StoryLookup_Serializer : StdSerializer<StoryLookup>(StoryLookup::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(StoryLookup::class.java, this)

    override fun serialize(
        value: StoryLookup,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("story")
        val _story_ = value.story
        serializers.defaultSerializeValue(_story_, gen)
        gen.writeEndObject()
    }
}
