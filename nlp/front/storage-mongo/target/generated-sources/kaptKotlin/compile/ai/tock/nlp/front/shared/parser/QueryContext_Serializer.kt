package ai.tock.nlp.front.shared.parser

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class QueryContext_Serializer : StdSerializer<QueryContext>(QueryContext::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(QueryContext::class.java, this)

  override fun serialize(
    `value`: QueryContext,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("language")
    val _language_ = value.language
    serializers.writeValue(gen, _language_)
    gen.writeName("clientId")
    val _clientId_ = value.clientId
    gen.writeString(_clientId_)
    gen.writeName("clientDevice")
    val _clientDevice_ = value.clientDevice
    if (_clientDevice_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_clientDevice_)
    }
    gen.writeName("dialogId")
    val _dialogId_ = value.dialogId
    gen.writeString(_dialogId_)
    gen.writeName("referenceDate")
    val _referenceDate_ = value.referenceDate
    serializers.writeValue(gen, _referenceDate_)
    gen.writeName("referenceTimezone")
    val _referenceTimezone_ = value.referenceTimezone
    serializers.writeValue(gen, _referenceTimezone_)
    gen.writeName("test")
    val _test_ = value.test
    gen.writeBoolean(_test_)
    gen.writeName("registerQuery")
    val _registerQuery_ = value.registerQuery
    gen.writeBoolean(_registerQuery_)
    gen.writeName("checkExistingQuery")
    val _checkExistingQuery_ = value.checkExistingQuery
    gen.writeBoolean(_checkExistingQuery_)
    gen.writeName("increaseQueryCounter")
    val _increaseQueryCounter_ = value.increaseQueryCounter
    gen.writeBoolean(_increaseQueryCounter_)
    gen.writeEndObject()
  }
}
