package ai.tock.bot.admin.test

import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class TestPlanExecution_Serializer : StdSerializer<TestPlanExecution>(TestPlanExecution::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(TestPlanExecution::class.java, this)

  override fun serialize(
    `value`: TestPlanExecution,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("testPlanId")
    val _testPlanId_ = value.testPlanId
    serializers.writeValue(gen, _testPlanId_)
    gen.writeName("dialogs")
    val _dialogs_ = value.dialogs
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(DialogExecutionReport::class.javaObjectType)), true).serialize(_dialogs_, gen, serializers)
    gen.writeName("nbErrors")
    val _nbErrors_ = value.nbErrors
    gen.writeNumber(_nbErrors_)
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeName("duration")
    val _duration_ = value.duration
    serializers.writeValue(gen, _duration_)
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("status")
    val _status_ = value.status
    serializers.writeValue(gen, _status_)
    gen.writeEndObject()
  }
}
