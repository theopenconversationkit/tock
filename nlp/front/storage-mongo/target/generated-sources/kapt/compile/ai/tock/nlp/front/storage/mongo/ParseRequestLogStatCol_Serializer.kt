package ai.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ParseRequestLogStatCol_Serializer :
        StdSerializer<ParseRequestLogMongoDAO.ParseRequestLogStatCol>(ParseRequestLogMongoDAO.ParseRequestLogStatCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(ParseRequestLogMongoDAO.ParseRequestLogStatCol::class.java,
            this)

    override fun serialize(
        value: ParseRequestLogMongoDAO.ParseRequestLogStatCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("text")
        val _text_ = value.text
        gen.writeString(_text_)
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        serializers.defaultSerializeValue(_applicationId_, gen)
        gen.writeFieldName("language")
        val _language_ = value.language
        serializers.defaultSerializeValue(_language_, gen)
        gen.writeFieldName("intentProbability")
        val _intentProbability_ = value.intentProbability
        if(_intentProbability_ == null) { gen.writeNull() } else {
                gen.writeNumber(_intentProbability_)
                }
        gen.writeFieldName("entitiesProbability")
        val _entitiesProbability_ = value.entitiesProbability
        if(_entitiesProbability_ == null) { gen.writeNull() } else {
                gen.writeNumber(_entitiesProbability_)
                }
        gen.writeFieldName("lastUsage")
        val _lastUsage_ = value.lastUsage
        serializers.defaultSerializeValue(_lastUsage_, gen)
        gen.writeFieldName("count")
        val _count_ = value.count
        gen.writeNumber(_count_)
        gen.writeEndObject()
    }
}
