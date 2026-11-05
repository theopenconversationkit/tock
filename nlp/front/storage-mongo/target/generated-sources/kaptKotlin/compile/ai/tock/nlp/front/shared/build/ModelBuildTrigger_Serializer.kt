package ai.tock.nlp.front.shared.build

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ModelBuildTrigger_Serializer : StdSerializer<ModelBuildTrigger>(ModelBuildTrigger::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ModelBuildTrigger::class.java, this)

  override fun serialize(
    `value`: ModelBuildTrigger,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("applicationId")
    val _applicationId_ = value.applicationId
    serializers.writeValue(gen, _applicationId_)
    gen.writeName("all")
    val _all_ = value.all
    gen.writeBoolean(_all_)
    gen.writeName("onlyIfModelNotExists")
    val _onlyIfModelNotExists_ = value.onlyIfModelNotExists
    gen.writeBoolean(_onlyIfModelNotExists_)
    gen.writeEndObject()
  }
}
