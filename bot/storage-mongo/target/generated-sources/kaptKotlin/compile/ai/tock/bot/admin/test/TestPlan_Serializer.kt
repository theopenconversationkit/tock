package ai.tock.bot.admin.test

import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class TestPlan_Serializer : StdSerializer<TestPlan>(TestPlan::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(TestPlan::class.java, this)

  override fun serialize(
    `value`: TestPlan,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("dialogs")
    val _dialogs_ = value.dialogs
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(TestDialogReport::class.javaObjectType)), true).serialize(_dialogs_, gen, serializers)
    gen.writeName("name")
    val _name_ = value.name
    gen.writeString(_name_)
    gen.writeName("applicationId")
    val _applicationId_ = value.applicationId
    gen.writeString(_applicationId_)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("nlpModel")
    val _nlpModel_ = value.nlpModel
    gen.writeString(_nlpModel_)
    gen.writeName("botApplicationConfigurationId")
    val _botApplicationConfigurationId_ = value.botApplicationConfigurationId
    serializers.writeValue(gen, _botApplicationConfigurationId_)
    gen.writeName("locale")
    val _locale_ = value.locale
    serializers.writeValue(gen, _locale_)
    gen.writeName("startAction")
    val _startAction_ = value.startAction
    if (_startAction_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _startAction_)
    }
    gen.writeName("targetConnectorType")
    val _targetConnectorType_ = value.targetConnectorType
    serializers.writeValue(gen, _targetConnectorType_)
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeEndObject()
  }
}
