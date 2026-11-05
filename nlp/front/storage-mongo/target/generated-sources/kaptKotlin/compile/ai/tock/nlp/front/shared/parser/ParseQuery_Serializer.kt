package ai.tock.nlp.front.shared.parser

import kotlin.String
import kotlin.collections.List
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ParseQuery_Serializer : StdSerializer<ParseQuery>(ParseQuery::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ParseQuery::class.java, this)

  override fun serialize(
    `value`: ParseQuery,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("queries")
    val _queries_ = value.queries
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(String::class.javaObjectType)), true).serialize(_queries_, gen, serializers)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("applicationName")
    val _applicationName_ = value.applicationName
    gen.writeString(_applicationName_)
    gen.writeName("context")
    val _context_ = value.context
    serializers.writeValue(gen, _context_)
    gen.writeName("state")
    val _state_ = value.state
    serializers.writeValue(gen, _state_)
    gen.writeName("intentsSubset")
    val _intentsSubset_ = value.intentsSubset
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(IntentQualifier::class.javaObjectType)), true).serialize(_intentsSubset_, gen, serializers)
    gen.writeName("configuration")
    val _configuration_ = value.configuration
    if (_configuration_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_configuration_)
    }
    gen.writeEndObject()
  }
}
