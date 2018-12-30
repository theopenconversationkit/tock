package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ParseRequestLogIntentStatCol_Serializer :
        StdSerializer<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>(ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::class.java,
            this)

    override fun serialize(
        value: ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        serializers.defaultSerializeValue(_applicationId_, gen)
        gen.writeFieldName("language")
        val _language_ = value.language
        serializers.defaultSerializeValue(_language_, gen)
        gen.writeFieldName("intent1")
        val _intent1_ = value.intent1
        gen.writeString(_intent1_)
        gen.writeFieldName("intent2")
        val _intent2_ = value.intent2
        gen.writeString(_intent2_)
        gen.writeFieldName("averageDiff")
        val _averageDiff_ = value.averageDiff
        gen.writeNumber(_averageDiff_)
        gen.writeFieldName("count")
        val _count_ = value.count
        gen.writeNumber(_count_)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeEndObject()
    }
}
