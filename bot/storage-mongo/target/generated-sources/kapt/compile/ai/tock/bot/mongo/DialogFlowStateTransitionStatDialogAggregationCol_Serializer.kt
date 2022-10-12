package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class DialogFlowStateTransitionStatDialogAggregationCol_Serializer :
        StdSerializer<DialogFlowStateTransitionStatDialogAggregationCol>(DialogFlowStateTransitionStatDialogAggregationCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(DialogFlowStateTransitionStatDialogAggregationCol::class.java,
            this)

    override fun serialize(
        value: DialogFlowStateTransitionStatDialogAggregationCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        serializers.defaultSerializeValue(_applicationId_, gen)
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeFieldName("dialogId")
        val _dialogId_ = value.dialogId
        serializers.defaultSerializeValue(_dialogId_, gen)
        gen.writeFieldName("count")
        val _count_ = value.count
        gen.writeNumber(_count_)
        gen.writeEndObject()
    }
}
