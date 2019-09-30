package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class DialogTextCol_Serializer : StdSerializer<DialogTextCol>(DialogTextCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(DialogTextCol::class.java, this)

    override fun serialize(
        value: DialogTextCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("text")
        val _text_ = value.text
        gen.writeString(_text_)
        gen.writeFieldName("dialogId")
        val _dialogId_ = value.dialogId
        serializers.defaultSerializeValue(_dialogId_, gen)
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeEndObject()
    }
}
