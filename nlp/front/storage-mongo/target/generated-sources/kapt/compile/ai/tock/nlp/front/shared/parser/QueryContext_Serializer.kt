package ai.tock.nlp.front.shared.parser

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class QueryContext_Serializer : StdSerializer<QueryContext>(QueryContext::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(QueryContext::class.java, this)

    override fun serialize(
        value: QueryContext,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("language")
        val _language_ = value.language
        serializers.defaultSerializeValue(_language_, gen)
        gen.writeFieldName("clientId")
        val _clientId_ = value.clientId
        gen.writeString(_clientId_)
        gen.writeFieldName("clientDevice")
        val _clientDevice_ = value.clientDevice
        if(_clientDevice_ == null) { gen.writeNull() } else {
                gen.writeString(_clientDevice_)
                }
        gen.writeFieldName("dialogId")
        val _dialogId_ = value.dialogId
        gen.writeString(_dialogId_)
        gen.writeFieldName("referenceDate")
        val _referenceDate_ = value.referenceDate
        serializers.defaultSerializeValue(_referenceDate_, gen)
        gen.writeFieldName("referenceTimezone")
        val _referenceTimezone_ = value.referenceTimezone
        serializers.defaultSerializeValue(_referenceTimezone_, gen)
        gen.writeFieldName("test")
        val _test_ = value.test
        gen.writeBoolean(_test_)
        gen.writeFieldName("registerQuery")
        val _registerQuery_ = value.registerQuery
        gen.writeBoolean(_registerQuery_)
        gen.writeFieldName("checkExistingQuery")
        val _checkExistingQuery_ = value.checkExistingQuery
        gen.writeBoolean(_checkExistingQuery_)
        gen.writeFieldName("increaseQueryCounter")
        val _increaseQueryCounter_ = value.increaseQueryCounter
        gen.writeBoolean(_increaseQueryCounter_)
        gen.writeEndObject()
    }
}
