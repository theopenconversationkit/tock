package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class DialogFlowStateTransitionStatDateAggregationCol_Serializer :
        StdSerializer<DialogFlowStateTransitionStatDateAggregationCol>(DialogFlowStateTransitionStatDateAggregationCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(DialogFlowStateTransitionStatDateAggregationCol::class.java,
            this)

    override fun serialize(
        value: DialogFlowStateTransitionStatDateAggregationCol,
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
        gen.writeFieldName("hourOfDay")
        val _hourOfDay_ = value.hourOfDay
        gen.writeNumber(_hourOfDay_)
        gen.writeFieldName("intent")
        val _intent_ = value.intent
        if(_intent_ == null) { gen.writeNull() } else {
                gen.writeString(_intent_)
                }
        gen.writeFieldName("storyDefinitionId")
        val _storyDefinitionId_ = value.storyDefinitionId
        gen.writeString(_storyDefinitionId_)
        gen.writeFieldName("storyCategory")
        val _storyCategory_ = value.storyCategory
        gen.writeString(_storyCategory_)
        gen.writeFieldName("storyType")
        val _storyType_ = value.storyType
        gen.writeString(_storyType_)
        gen.writeFieldName("locale")
        val _locale_ = value.locale
        serializers.defaultSerializeValue(_locale_, gen)
        gen.writeFieldName("configurationName")
        val _configurationName_ = value.configurationName
        gen.writeString(_configurationName_)
        gen.writeFieldName("connectorType")
        val _connectorType_ = value.connectorType
        serializers.defaultSerializeValue(_connectorType_, gen)
        gen.writeFieldName("actionType")
        val _actionType_ = value.actionType
        serializers.defaultSerializeValue(_actionType_, gen)
        gen.writeFieldName("count")
        val _count_ = value.count
        gen.writeNumber(_count_)
        gen.writeEndObject()
    }
}
