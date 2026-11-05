package ai.tock.nlp.front.shared.config

import ai.tock.nlp.core.EntitiesRegexp
import java.util.LinkedHashSet
import java.util.Locale
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class IntentDefinition_Serializer : StdSerializer<IntentDefinition>(IntentDefinition::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(IntentDefinition::class.java, this)

  override fun serialize(
    `value`: IntentDefinition,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("name")
    val _name_ = value.name
    gen.writeString(_name_)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("applications")
    val _applications_ = value.applications
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(Id::class.javaObjectType)), true).serialize(_applications_, gen, serializers)
    gen.writeName("entities")
    val _entities_ = value.entities
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(EntityDefinition::class.javaObjectType)), true).serialize(_entities_, gen, serializers)
    gen.writeName("entitiesRegexp")
    val _entitiesRegexp_ = value.entitiesRegexp
    serializers.findTypedValueSerializer(serializers.typeFactory.constructMapType(Map::class.java, serializers.typeFactory.constructType(Locale::class.javaObjectType), serializers.typeFactory.constructCollectionType(LinkedHashSet::class.java, serializers.typeFactory.constructType(EntitiesRegexp::class.javaObjectType))), true).serialize(_entitiesRegexp_, gen, serializers)
    gen.writeName("mandatoryStates")
    val _mandatoryStates_ = value.mandatoryStates
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(String::class.javaObjectType)), true).serialize(_mandatoryStates_, gen, serializers)
    gen.writeName("sharedIntents")
    val _sharedIntents_ = value.sharedIntents
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(Id::class.javaObjectType)), true).serialize(_sharedIntents_, gen, serializers)
    gen.writeName("label")
    val _label_ = value.label
    if (_label_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_label_)
    }
    gen.writeName("description")
    val _description_ = value.description
    if (_description_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_description_)
    }
    gen.writeName("category")
    val _category_ = value.category
    if (_category_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_category_)
    }
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeEndObject()
  }
}
