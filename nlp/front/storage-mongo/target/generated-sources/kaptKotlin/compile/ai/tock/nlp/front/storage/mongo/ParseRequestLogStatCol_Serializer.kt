package ai.tock.nlp.front.storage.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ParseRequestLogStatCol_Serializer : StdSerializer<ParseRequestLogMongoDAO.ParseRequestLogStatCol>(ParseRequestLogMongoDAO.ParseRequestLogStatCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ParseRequestLogMongoDAO.ParseRequestLogStatCol::class.java, this)

  override fun serialize(
    `value`: ParseRequestLogMongoDAO.ParseRequestLogStatCol,
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
    gen.writeName("language")
    val _language_ = value.language
    serializers.writeValue(gen, _language_)
    gen.writeName("intent")
    val _intent_ = value.intent
    if (_intent_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_intent_)
    }
    gen.writeName("intentProbability")
    val _intentProbability_ = value.intentProbability
    if (_intentProbability_ == null) {
      gen.writeNull()
    } else {
      gen.writeNumber(_intentProbability_)
    }
    gen.writeName("entitiesProbability")
    val _entitiesProbability_ = value.entitiesProbability
    if (_entitiesProbability_ == null) {
      gen.writeNull()
    } else {
      gen.writeNumber(_entitiesProbability_)
    }
    gen.writeName("lastUsage")
    val _lastUsage_ = value.lastUsage
    serializers.writeValue(gen, _lastUsage_)
    gen.writeName("count")
    val _count_ = value.count
    gen.writeNumber(_count_)
    gen.writeName("validated")
    val _validated_ = value.validated
    gen.writeBoolean(_validated_)
    gen.writeEndObject()
  }
}
