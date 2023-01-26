package ai.tock.nlp.front.shared.namespace

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class NamespaceConfiguration_Deserializer : JsonDeserializer<NamespaceConfiguration>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NamespaceConfiguration::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NamespaceConfiguration {
        with(p) {
            var _namespace_: String? = null
            var _namespace_set : Boolean = false
            var _defaultSharingConfiguration_: NamespaceSharingConfiguration? = null
            var _defaultSharingConfiguration_set : Boolean = false
            var _namespaceImportConfiguration_: MutableMap<String, NamespaceSharingConfiguration>? =
                    null
            var _namespaceImportConfiguration_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "namespace" -> {
                            _namespace_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    "defaultSharingConfiguration" -> {
                            _defaultSharingConfiguration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NamespaceSharingConfiguration::class.java);
                            _defaultSharingConfiguration_set = true
                            }
                    "namespaceImportConfiguration" -> {
                            _namespaceImportConfiguration_ = if(_token_ == JsonToken.VALUE_NULL)
                                    null
                             else p.readValueAs(_namespaceImportConfiguration__reference);
                            _namespaceImportConfiguration_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_namespace_set && _defaultSharingConfiguration_set &&
                    _namespaceImportConfiguration_set)
                    NamespaceConfiguration(namespace = _namespace_!!, defaultSharingConfiguration =
                            _defaultSharingConfiguration_!!, namespaceImportConfiguration =
                            _namespaceImportConfiguration_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_defaultSharingConfiguration_set)
                    map[parameters.getValue("defaultSharingConfiguration")] =
                            _defaultSharingConfiguration_
                    if(_namespaceImportConfiguration_set)
                    map[parameters.getValue("namespaceImportConfiguration")] =
                            _namespaceImportConfiguration_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<NamespaceConfiguration> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                NamespaceConfiguration::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("namespace" to
                primaryConstructor.findParameterByName("namespace")!!, "defaultSharingConfiguration"
                to primaryConstructor.findParameterByName("defaultSharingConfiguration")!!,
                "namespaceImportConfiguration" to
                primaryConstructor.findParameterByName("namespaceImportConfiguration")!!) }

        private val _namespaceImportConfiguration__reference: TypeReference<Map<String,
                NamespaceSharingConfiguration>> = object : TypeReference<Map<String,
                NamespaceSharingConfiguration>>() {}
    }
}
