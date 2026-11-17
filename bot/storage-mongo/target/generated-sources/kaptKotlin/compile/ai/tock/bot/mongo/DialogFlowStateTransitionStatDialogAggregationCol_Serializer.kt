package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class DialogFlowStateTransitionStatDialogAggregationCol_Serializer : StdSerializer<DialogFlowStateTransitionStatDialogAggregationCol>(DialogFlowStateTransitionStatDialogAggregationCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(DialogFlowStateTransitionStatDialogAggregationCol::class.java, this)

  override fun serialize(
    `value`: DialogFlowStateTransitionStatDialogAggregationCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("applicationId")
    val _applicationId_ = value.applicationId
    serializers.writeValue(gen, _applicationId_)
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeName("dialogId")
    val _dialogId_ = value.dialogId
    serializers.writeValue(gen, _dialogId_)
    gen.writeName("count")
    val _count_ = value.count
    gen.writeNumber(_count_)
    gen.writeEndObject()
  }
}
