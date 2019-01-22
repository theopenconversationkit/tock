package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class IntentDefinition_Serializer :
        StdSerializer<IntentDefinition>(IntentDefinition::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(IntentDefinition::class.java, this)

    override fun serialize(
        value: IntentDefinition,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("name")
        val _name_ = value.name
        gen.writeString(_name_)
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("applications")
        val _applications_ = value.applications
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(org.litote.kmongo.Id::class.java)
                ),
                true,
                null
                )
                .serialize(_applications_, gen, serializers)
        gen.writeFieldName("entities")
        val _entities_ = value.entities
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(fr.vsct.tock.nlp.front.shared.config.EntityDefinition::class.java)
                ),
                true,
                null
                )
                .serialize(_entities_, gen, serializers)
        gen.writeFieldName("entitiesRegexp")
        val _entitiesRegexp_ = value.entitiesRegexp
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                serializers.config.typeFactory.constructType(java.util.Locale::class.java),
                serializers.config.typeFactory.constructCollectionType(
                java.util.LinkedHashSet::class.java,
                serializers.config.typeFactory.constructType(fr.vsct.tock.nlp.core.EntitiesRegexp::class.java)
                )
                ),
                true,
                null
                )
                .serialize(_entitiesRegexp_, gen, serializers)
        gen.writeFieldName("mandatoryStates")
        val _mandatoryStates_ = value.mandatoryStates
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java)
                ),
                true,
                null
                )
                .serialize(_mandatoryStates_, gen, serializers)
        gen.writeFieldName("sharedIntents")
        val _sharedIntents_ = value.sharedIntents
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(org.litote.kmongo.Id::class.java)
                ),
                true,
                null
                )
                .serialize(_sharedIntents_, gen, serializers)
        gen.writeFieldName("label")
        val _label_ = value.label
        if(_label_ == null) { gen.writeNull() } else {
                gen.writeString(_label_)
                }
        gen.writeFieldName("description")
        val _description_ = value.description
        if(_description_ == null) { gen.writeNull() } else {
                gen.writeString(_description_)
                }
        gen.writeFieldName("category")
        val _category_ = value.category
        if(_category_ == null) { gen.writeNull() } else {
                gen.writeString(_category_)
                }
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeEndObject()
    }
}
