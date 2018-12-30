package fr.vsct.tock.nlp.model.service.storage.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpApplicationConfigurationCol_Serializer :
        StdSerializer<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol>(NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::class.java,
            this)

    override fun serialize(
        value: NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("applicationName")
        val _applicationName_ = value.applicationName
        gen.writeString(_applicationName_)
        gen.writeFieldName("engineType")
        val _engineType_ = value.engineType
        serializers.defaultSerializeValue(_engineType_, gen)
        gen.writeFieldName("configuration")
        val _configuration_ = value.configuration
        serializers.defaultSerializeValue(_configuration_, gen)
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeEndObject()
    }
}
