package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ArchivedEntityValueWrapper_Serializer :
        StdSerializer<ArchivedEntityValuesCol.ArchivedEntityValueWrapper>(ArchivedEntityValuesCol.ArchivedEntityValueWrapper::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(ArchivedEntityValuesCol.ArchivedEntityValueWrapper::class.java,
            this)

    override fun serialize(
        value: ArchivedEntityValuesCol.ArchivedEntityValueWrapper,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("entityValue")
        val _entityValue_ = value.entityValue
        if(_entityValue_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_entityValue_, gen)
                }
        gen.writeFieldName("actionId")
        val _actionId_ = value.actionId
        if(_actionId_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_actionId_, gen)
                }
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeEndObject()
    }
}
