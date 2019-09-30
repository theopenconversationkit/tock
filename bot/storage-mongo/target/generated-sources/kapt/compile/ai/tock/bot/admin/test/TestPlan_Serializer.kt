package ai.tock.bot.admin.test

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class TestPlan_Serializer : StdSerializer<TestPlan>(TestPlan::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(TestPlan::class.java, this)

    override fun serialize(
        value: TestPlan,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("dialogs")
        val _dialogs_ = value.dialogs
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.admin.test.TestDialogReport::class.java)
                ),
                true,
                null
                )
                .serialize(_dialogs_, gen, serializers)
        gen.writeFieldName("name")
        val _name_ = value.name
        gen.writeString(_name_)
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        gen.writeString(_applicationId_)
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("nlpModel")
        val _nlpModel_ = value.nlpModel
        gen.writeString(_nlpModel_)
        gen.writeFieldName("botApplicationConfigurationId")
        val _botApplicationConfigurationId_ = value.botApplicationConfigurationId
        serializers.defaultSerializeValue(_botApplicationConfigurationId_, gen)
        gen.writeFieldName("locale")
        val _locale_ = value.locale
        serializers.defaultSerializeValue(_locale_, gen)
        gen.writeFieldName("startAction")
        val _startAction_ = value.startAction
        if(_startAction_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_startAction_, gen)
                }
        gen.writeFieldName("targetConnectorType")
        val _targetConnectorType_ = value.targetConnectorType
        serializers.defaultSerializeValue(_targetConnectorType_, gen)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeEndObject()
    }
}
