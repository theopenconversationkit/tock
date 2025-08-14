package ai.tock.nlp.front.shared.parser

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ParseResult_Serializer : StdSerializer<ParseResult>(ParseResult::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(ParseResult::class.java, this)

    override fun serialize(
        value: ParseResult,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("intent")
        val _intent_ = value.intent
        gen.writeString(_intent_)
        gen.writeFieldName("intentNamespace")
        val _intentNamespace_ = value.intentNamespace
        gen.writeString(_intentNamespace_)
        gen.writeFieldName("language")
        val _language_ = value.language
        serializers.defaultSerializeValue(_language_, gen)
        gen.writeFieldName("entities")
        val _entities_ = value.entities
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.nlp.front.shared.parser.ParsedEntityValue::class.java)
                ),
                true,
                null
                )
                .serialize(_entities_, gen, serializers)
        gen.writeFieldName("notRetainedEntities")
        val _notRetainedEntities_ = value.notRetainedEntities
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.nlp.front.shared.parser.ParsedEntityValue::class.java)
                ),
                true,
                null
                )
                .serialize(_notRetainedEntities_, gen, serializers)
        gen.writeFieldName("intentProbability")
        val _intentProbability_ = value.intentProbability
        gen.writeNumber(_intentProbability_)
        gen.writeFieldName("entitiesProbability")
        val _entitiesProbability_ = value.entitiesProbability
        gen.writeNumber(_entitiesProbability_)
        gen.writeFieldName("retainedQuery")
        val _retainedQuery_ = value.retainedQuery
        gen.writeString(_retainedQuery_)
        gen.writeFieldName("otherIntentsProbabilities")
        val _otherIntentsProbabilities_ = value.otherIntentsProbabilities
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java),
                serializers.config.typeFactory.constructType(kotlin.Double::class.java)
                ),
                true,
                null
                )
                .serialize(_otherIntentsProbabilities_, gen, serializers)
        gen.writeFieldName("originalIntentsProbabilities")
        val _originalIntentsProbabilities_ = value.originalIntentsProbabilities
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java),
                serializers.config.typeFactory.constructType(kotlin.Double::class.java)
                ),
                true,
                null
                )
                .serialize(_originalIntentsProbabilities_, gen, serializers)
        gen.writeEndObject()
    }
}
