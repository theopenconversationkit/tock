package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class NlpStatsCol_Serializer : StdSerializer<NlpStatsCol>(NlpStatsCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(NlpStatsCol::class.java, this)

  override fun serialize(
    `value`: NlpStatsCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("stats")
    val _stats_ = value.stats
    serializers.writeValue(gen, _stats_)
    gen.writeName("appNamespace")
    val _appNamespace_ = value.appNamespace
    gen.writeString(_appNamespace_)
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeEndObject()
  }
}
