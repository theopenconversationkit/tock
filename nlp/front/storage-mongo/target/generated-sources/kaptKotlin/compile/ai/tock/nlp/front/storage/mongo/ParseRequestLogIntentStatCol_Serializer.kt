package ai.tock.nlp.front.storage.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ParseRequestLogIntentStatCol_Serializer : StdSerializer<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>(ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::class.java, this)

  override fun serialize(
    `value`: ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("applicationId")
    val _applicationId_ = value.applicationId
    serializers.writeValue(gen, _applicationId_)
    gen.writeName("language")
    val _language_ = value.language
    serializers.writeValue(gen, _language_)
    gen.writeName("intent1")
    val _intent1_ = value.intent1
    gen.writeString(_intent1_)
    gen.writeName("intent2")
    val _intent2_ = value.intent2
    gen.writeString(_intent2_)
    gen.writeName("averageDiff")
    val _averageDiff_ = value.averageDiff
    gen.writeNumber(_averageDiff_)
    gen.writeName("count")
    val _count_ = value.count
    gen.writeNumber(_count_)
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeEndObject()
  }
}
