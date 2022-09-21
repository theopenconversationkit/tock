package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class DialogFlowStateTransitionStatCol_Serializer :
        StdSerializer<DialogFlowStateTransitionStatCol>(DialogFlowStateTransitionStatCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(DialogFlowStateTransitionStatCol::class.java, this)

    override fun serialize(
        value: DialogFlowStateTransitionStatCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        serializers.defaultSerializeValue(_applicationId_, gen)
        gen.writeFieldName("transitionId")
        val _transitionId_ = value.transitionId
        serializers.defaultSerializeValue(_transitionId_, gen)
        gen.writeFieldName("dialogId")
        val _dialogId_ = value.dialogId
        serializers.defaultSerializeValue(_dialogId_, gen)
        gen.writeFieldName("text")
        val _text_ = value.text
        if(_text_ == null) { gen.writeNull() } else {
                gen.writeString(_text_)
                }
        gen.writeFieldName("locale")
        val _locale_ = value.locale
        if(_locale_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_locale_, gen)
                }
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeFieldName("processedLevel")
        val _processedLevel_ = value.processedLevel
        gen.writeNumber(_processedLevel_)
        gen.writeEndObject()
    }
}
