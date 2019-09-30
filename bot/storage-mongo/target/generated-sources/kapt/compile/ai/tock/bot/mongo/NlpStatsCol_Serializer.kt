package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpStatsCol_Serializer : StdSerializer<NlpStatsCol>(NlpStatsCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(NlpStatsCol::class.java, this)

    override fun serialize(
        value: NlpStatsCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("stats")
        val _stats_ = value.stats
        serializers.defaultSerializeValue(_stats_, gen)
        gen.writeFieldName("appNamespace")
        val _appNamespace_ = value.appNamespace
        gen.writeString(_appNamespace_)
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeEndObject()
    }
}
