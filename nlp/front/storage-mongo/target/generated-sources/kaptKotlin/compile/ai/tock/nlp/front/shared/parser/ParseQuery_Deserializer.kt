package ai.tock.nlp.front.shared.parser

import kotlin.Any
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class ParseQuery_Deserializer : ValueDeserializer<ParseQuery>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ParseQuery::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseQuery {
    with(p) {
      var _queries_: List<String>? = null
      var _queries_set: Boolean = false
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _applicationName_: String? = null
      var _applicationName_set: Boolean = false
      var _context_: QueryContext? = null
      var _context_set: Boolean = false
      var _state_: QueryState? = null
      var _state_set: Boolean = false
      var _intentsSubset_: Set<IntentQualifier>? = null
      var _intentsSubset_set: Boolean = false
      var _configuration_: String? = null
      var _configuration_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "queries" -> {
            _queries_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_queries__reference)
            _queries_set = true
          }
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "applicationName" -> {
            _applicationName_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _applicationName_set = true
          }
          "context" -> {
            _context_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(QueryContext::class.javaObjectType)
            _context_set = true
          }
          "state" -> {
            _state_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(QueryState::class.javaObjectType)
            _state_set = true
          }
          "intentsSubset" -> {
            _intentsSubset_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_intentsSubset__reference)
            _intentsSubset_set = true
          }
          "configuration" -> {
            _configuration_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _configuration_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_queries_set && _namespace_set && _applicationName_set && _context_set && _state_set && _intentsSubset_set && _configuration_set) {
        ParseQuery(queries = _queries_!!, namespace = _namespace_!!, applicationName = _applicationName_!!, context = _context_!!, state = _state_!!, intentsSubset = _intentsSubset_!!, configuration = _configuration_)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_queries_set) map_[parameters.getValue("queries")] = _queries_
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_applicationName_set) map_[parameters.getValue("applicationName")] = _applicationName_
        if (_context_set) map_[parameters.getValue("context")] = _context_
        if (_state_set) map_[parameters.getValue("state")] = _state_
        if (_intentsSubset_set) map_[parameters.getValue("intentsSubset")] = _intentsSubset_
        if (_configuration_set) map_[parameters.getValue("configuration")] = _configuration_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ParseQuery> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ParseQuery::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "queries" to primaryConstructor.findParameterByName("queries")!!,
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "applicationName" to primaryConstructor.findParameterByName("applicationName")!!,
            "context" to primaryConstructor.findParameterByName("context")!!,
            "state" to primaryConstructor.findParameterByName("state")!!,
            "intentsSubset" to primaryConstructor.findParameterByName("intentsSubset")!!,
            "configuration" to primaryConstructor.findParameterByName("configuration")!!,
          )
        }

    private val _queries__reference: TypeReference<List<String>> =
        object : TypeReference<List<String>>() {}

    private val _intentsSubset__reference: TypeReference<Set<IntentQualifier>> =
        object : TypeReference<Set<IntentQualifier>>() {}
  }
}
