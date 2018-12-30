package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.core.EntitiesRegexp
import java.util.LinkedHashSet
import java.util.Locale
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class IntentDefinition_Deserializer :
        StdDeserializer<IntentDefinition>(IntentDefinition::class.java), JacksonModuleServiceLoader
        {
    override fun module() = SimpleModule().addDeserializer(IntentDefinition::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IntentDefinition {
        with(p) {
            var _name_: String? = null
            var _name_set = false
            var _namespace_: String? = null
            var _namespace_set = false
            var _applications_: MutableSet<Id<ApplicationDefinition>>? = null
            var _applications_set = false
            var _entities_: MutableSet<EntityDefinition>? = null
            var _entities_set = false
            var _entitiesRegexp_: MutableMap<Locale, LinkedHashSet<EntitiesRegexp>>? = null
            var _entitiesRegexp_set = false
            var _mandatoryStates_: MutableSet<String>? = null
            var _mandatoryStates_set = false
            var _sharedIntents_: MutableSet<Id<IntentDefinition>>? = null
            var _sharedIntents_set = false
            var _label_: String? = null
            var _label_set = false
            var _description_: String? = null
            var _description_set = false
            var _category_: String? = null
            var _category_set = false
            var __id_: Id<IntentDefinition>? = null
            var __id_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "name" -> {
                            _name_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _name_set = true
                            }
                    "namespace" -> {
                            _namespace_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    "applications" -> {
                            _applications_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applications__reference);
                            _applications_set = true
                            }
                    "entities" -> {
                            _entities_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_entities__reference);
                            _entities_set = true
                            }
                    "entitiesRegexp" -> {
                            _entitiesRegexp_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_entitiesRegexp__reference);
                            _entitiesRegexp_set = true
                            }
                    "mandatoryStates" -> {
                            _mandatoryStates_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_mandatoryStates__reference);
                            _mandatoryStates_set = true
                            }
                    "sharedIntents" -> {
                            _sharedIntents_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_sharedIntents__reference);
                            _sharedIntents_set = true
                            }
                    "label" -> {
                            _label_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _label_set = true
                            }
                    "description" -> {
                            _description_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _description_set = true
                            }
                    "category" -> {
                            _category_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _category_set = true
                            }
                    "_id" -> {
                            __id_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_name_set && _namespace_set && _applications_set && _entities_set &&
                    _entitiesRegexp_set && _mandatoryStates_set && _sharedIntents_set && _label_set
                    && _description_set && _category_set && __id_set)
                    IntentDefinition(name = _name_!!, namespace = _namespace_!!, applications =
                            _applications_!!, entities = _entities_!!, entitiesRegexp =
                            _entitiesRegexp_!!, mandatoryStates = _mandatoryStates_!!, sharedIntents
                            = _sharedIntents_!!, label = _label_, description = _description_,
                            category = _category_, _id = __id_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_name_set)
                    map[parameters.getValue("name")] = _name_
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_applications_set)
                    map[parameters.getValue("applications")] = _applications_
                    if(_entities_set)
                    map[parameters.getValue("entities")] = _entities_
                    if(_entitiesRegexp_set)
                    map[parameters.getValue("entitiesRegexp")] = _entitiesRegexp_
                    if(_mandatoryStates_set)
                    map[parameters.getValue("mandatoryStates")] = _mandatoryStates_
                    if(_sharedIntents_set)
                    map[parameters.getValue("sharedIntents")] = _sharedIntents_
                    if(_label_set)
                    map[parameters.getValue("label")] = _label_
                    if(_description_set)
                    map[parameters.getValue("description")] = _description_
                    if(_category_set)
                    map[parameters.getValue("category")] = _category_
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<IntentDefinition> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                IntentDefinition::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("name" to primaryConstructor.findParameterByName("name")!!,
                "namespace" to primaryConstructor.findParameterByName("namespace")!!, "applications"
                to primaryConstructor.findParameterByName("applications")!!, "entities" to
                primaryConstructor.findParameterByName("entities")!!, "entitiesRegexp" to
                primaryConstructor.findParameterByName("entitiesRegexp")!!, "mandatoryStates" to
                primaryConstructor.findParameterByName("mandatoryStates")!!, "sharedIntents" to
                primaryConstructor.findParameterByName("sharedIntents")!!, "label" to
                primaryConstructor.findParameterByName("label")!!, "description" to
                primaryConstructor.findParameterByName("description")!!, "category" to
                primaryConstructor.findParameterByName("category")!!, "_id" to
                primaryConstructor.findParameterByName("_id")!!) }

        private val _applications__reference: TypeReference<Set<Id<ApplicationDefinition>>> = object
                : TypeReference<Set<Id<ApplicationDefinition>>>() {}

        private val _entities__reference: TypeReference<Set<EntityDefinition>> = object :
                TypeReference<Set<EntityDefinition>>() {}

        private val _entitiesRegexp__reference: TypeReference<Map<Locale,
                LinkedHashSet<EntitiesRegexp>>> = object : TypeReference<Map<Locale,
                LinkedHashSet<EntitiesRegexp>>>() {}

        private val _mandatoryStates__reference: TypeReference<Set<String>> = object :
                TypeReference<Set<String>>() {}

        private val _sharedIntents__reference: TypeReference<Set<Id<IntentDefinition>>> = object :
                TypeReference<Set<Id<IntentDefinition>>>() {}

        private val __id__reference: TypeReference<Id<IntentDefinition>> = object :
                TypeReference<Id<IntentDefinition>>() {}
    }
}
