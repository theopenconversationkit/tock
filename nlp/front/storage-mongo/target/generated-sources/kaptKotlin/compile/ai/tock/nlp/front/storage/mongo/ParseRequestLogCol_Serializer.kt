package ai.tock.nlp.front.storage.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ParseRequestLogCol_Serializer : StdSerializer<ParseRequestLogMongoDAO.ParseRequestLogCol>(ParseRequestLogMongoDAO.ParseRequestLogCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ParseRequestLogMongoDAO.ParseRequestLogCol::class.java, this)

  override fun serialize(
    `value`: ParseRequestLogMongoDAO.ParseRequestLogCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("text")
    val _text_ = value.text
    gen.writeString(_text_)
    gen.writeName("applicationId")
    val _applicationId_ = value.applicationId
    serializers.writeValue(gen, _applicationId_)
    gen.writeName("query")
    val _query_ = value.query
    serializers.writeValue(gen, _query_)
    gen.writeName("result")
    val _result_ = value.result
    if (_result_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _result_)
    }
    gen.writeName("durationInMS")
    val _durationInMS_ = value.durationInMS
    gen.writeNumber(_durationInMS_)
    gen.writeName("error")
    val _error_ = value.error
    gen.writeBoolean(_error_)
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeEndObject()
  }
}
