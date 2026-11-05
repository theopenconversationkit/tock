package ai.tock.nlp.front.storage.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ParseRequestLogStatResult_Serializer : StdSerializer<ParseRequestLogMongoDAO.ParseRequestLogStatResult>(ParseRequestLogMongoDAO.ParseRequestLogStatResult::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ParseRequestLogMongoDAO.ParseRequestLogStatResult::class.java, this)

  override fun serialize(
    `value`: ParseRequestLogMongoDAO.ParseRequestLogStatResult,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("error")
    val _error_ = value.error
    gen.writeNumber(_error_)
    gen.writeName("count")
    val _count_ = value.count
    gen.writeNumber(_count_)
    gen.writeName("duration")
    val _duration_ = value.duration
    gen.writeNumber(_duration_)
    gen.writeName("intentProbability")
    val _intentProbability_ = value.intentProbability
    gen.writeNumber(_intentProbability_)
    gen.writeName("entitiesProbability")
    val _entitiesProbability_ = value.entitiesProbability
    gen.writeNumber(_entitiesProbability_)
    gen.writeEndObject()
  }
}
