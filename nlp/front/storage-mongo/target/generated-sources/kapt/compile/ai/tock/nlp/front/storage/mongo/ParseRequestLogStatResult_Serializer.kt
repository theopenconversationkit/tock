package ai.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ParseRequestLogStatResult_Serializer :
        StdSerializer<ParseRequestLogMongoDAO.ParseRequestLogStatResult>(ParseRequestLogMongoDAO.ParseRequestLogStatResult::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(ParseRequestLogMongoDAO.ParseRequestLogStatResult::class.java,
            this)

    override fun serialize(
        value: ParseRequestLogMongoDAO.ParseRequestLogStatResult,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("error")
        val _error_ = value.error
        gen.writeNumber(_error_)
        gen.writeFieldName("count")
        val _count_ = value.count
        gen.writeNumber(_count_)
        gen.writeFieldName("duration")
        val _duration_ = value.duration
        gen.writeNumber(_duration_)
        gen.writeFieldName("intentProbability")
        val _intentProbability_ = value.intentProbability
        gen.writeNumber(_intentProbability_)
        gen.writeFieldName("entitiesProbability")
        val _entitiesProbability_ = value.entitiesProbability
        gen.writeNumber(_entitiesProbability_)
        gen.writeEndObject()
    }
}
