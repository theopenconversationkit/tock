package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class StoryDefinitionConfigurationHistoryCol_Serializer :
        StdSerializer<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>(StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::class.java,
            this)

    override fun serialize(
        value: StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("conf")
        val _conf_ = value.conf
        serializers.defaultSerializeValue(_conf_, gen)
        gen.writeFieldName("deleted")
        val _deleted_ = value.deleted
        gen.writeBoolean(_deleted_)
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeEndObject()
    }
}
