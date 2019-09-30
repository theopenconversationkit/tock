package ai.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ParseRequestLogCol_Serializer :
        StdSerializer<ParseRequestLogMongoDAO.ParseRequestLogCol>(ParseRequestLogMongoDAO.ParseRequestLogCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(ParseRequestLogMongoDAO.ParseRequestLogCol::class.java,
            this)

    override fun serialize(
        value: ParseRequestLogMongoDAO.ParseRequestLogCol,
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
        gen.writeFieldName("query")
        val _query_ = value.query
        serializers.defaultSerializeValue(_query_, gen)
        gen.writeFieldName("result")
        val _result_ = value.result
        if(_result_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_result_, gen)
                }
        gen.writeFieldName("durationInMS")
        val _durationInMS_ = value.durationInMS
        gen.writeNumber(_durationInMS_)
        gen.writeFieldName("error")
        val _error_ = value.error
        gen.writeBoolean(_error_)
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeEndObject()
    }
}
