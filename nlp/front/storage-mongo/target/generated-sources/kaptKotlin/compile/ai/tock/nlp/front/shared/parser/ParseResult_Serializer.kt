package ai.tock.nlp.front.shared.parser

import kotlin.Double
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ParseResult_Serializer : StdSerializer<ParseResult>(ParseResult::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ParseResult::class.java, this)

  override fun serialize(
    `value`: ParseResult,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("intent")
    val _intent_ = value.intent
    gen.writeString(_intent_)
    gen.writeName("intentNamespace")
    val _intentNamespace_ = value.intentNamespace
    gen.writeString(_intentNamespace_)
    gen.writeName("language")
    val _language_ = value.language
    serializers.writeValue(gen, _language_)
    gen.writeName("entities")
    val _entities_ = value.entities
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(ParsedEntityValue::class.javaObjectType)), true).serialize(_entities_, gen, serializers)
    gen.writeName("notRetainedEntities")
    val _notRetainedEntities_ = value.notRetainedEntities
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(ParsedEntityValue::class.javaObjectType)), true).serialize(_notRetainedEntities_, gen, serializers)
    gen.writeName("intentProbability")
    val _intentProbability_ = value.intentProbability
    gen.writeNumber(_intentProbability_)
    gen.writeName("entitiesProbability")
    val _entitiesProbability_ = value.entitiesProbability
    gen.writeNumber(_entitiesProbability_)
    gen.writeName("retainedQuery")
    val _retainedQuery_ = value.retainedQuery
    gen.writeString(_retainedQuery_)
    gen.writeName("otherIntentsProbabilities")
    val _otherIntentsProbabilities_ = value.otherIntentsProbabilities
    serializers.findTypedValueSerializer(serializers.typeFactory.constructMapType(Map::class.java, serializers.typeFactory.constructType(String::class.javaObjectType), serializers.typeFactory.constructType(Double::class.javaObjectType)), true).serialize(_otherIntentsProbabilities_, gen, serializers)
    gen.writeName("originalIntentsProbabilities")
    val _originalIntentsProbabilities_ = value.originalIntentsProbabilities
    serializers.findTypedValueSerializer(serializers.typeFactory.constructMapType(Map::class.java, serializers.typeFactory.constructType(String::class.javaObjectType), serializers.typeFactory.constructType(Double::class.javaObjectType)), true).serialize(_originalIntentsProbabilities_, gen, serializers)
    gen.writeEndObject()
  }
}
