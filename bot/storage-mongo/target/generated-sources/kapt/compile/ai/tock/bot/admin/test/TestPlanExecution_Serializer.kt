package ai.tock.bot.admin.test

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class TestPlanExecution_Serializer :
        StdSerializer<TestPlanExecution>(TestPlanExecution::class.java), JacksonModuleServiceLoader
        {
    override fun module() = SimpleModule().addSerializer(TestPlanExecution::class.java, this)

    override fun serialize(
        value: TestPlanExecution,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("testPlanId")
        val _testPlanId_ = value.testPlanId
        serializers.defaultSerializeValue(_testPlanId_, gen)
        gen.writeFieldName("dialogs")
        val _dialogs_ = value.dialogs
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.admin.test.DialogExecutionReport::class.java)
                ),
                true,
                null
                )
                .serialize(_dialogs_, gen, serializers)
        gen.writeFieldName("nbErrors")
        val _nbErrors_ = value.nbErrors
        gen.writeNumber(_nbErrors_)
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeFieldName("duration")
        val _duration_ = value.duration
        serializers.defaultSerializeValue(_duration_, gen)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("status")
        val _status_ = value.status
        serializers.defaultSerializeValue(_status_, gen)
        gen.writeEndObject()
    }
}
