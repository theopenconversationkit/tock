package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class DialogFlowStateTransitionStatDateAggregationCol_Serializer : StdSerializer<DialogFlowStateTransitionStatDateAggregationCol>(DialogFlowStateTransitionStatDateAggregationCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(DialogFlowStateTransitionStatDateAggregationCol::class.java, this)

  override fun serialize(
    `value`: DialogFlowStateTransitionStatDateAggregationCol,
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
    gen.writeName("hourOfDay")
    val _hourOfDay_ = value.hourOfDay
    gen.writeNumber(_hourOfDay_)
    gen.writeName("intent")
    val _intent_ = value.intent
    if (_intent_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_intent_)
    }
    gen.writeName("storyDefinitionId")
    val _storyDefinitionId_ = value.storyDefinitionId
    gen.writeString(_storyDefinitionId_)
    gen.writeName("storyCategory")
    val _storyCategory_ = value.storyCategory
    gen.writeString(_storyCategory_)
    gen.writeName("storyType")
    val _storyType_ = value.storyType
    gen.writeString(_storyType_)
    gen.writeName("locale")
    val _locale_ = value.locale
    serializers.writeValue(gen, _locale_)
    gen.writeName("configurationName")
    val _configurationName_ = value.configurationName
    gen.writeString(_configurationName_)
    gen.writeName("connectorType")
    val _connectorType_ = value.connectorType
    serializers.writeValue(gen, _connectorType_)
    gen.writeName("actionType")
    val _actionType_ = value.actionType
    serializers.writeValue(gen, _actionType_)
    gen.writeName("count")
    val _count_ = value.count
    gen.writeNumber(_count_)
    gen.writeEndObject()
  }
}
