package ai.tock.bot.mongo

import ai.tock.bot.engine.nlp.NlpCallStats
import java.time.Instant
import kotlin.Any
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class NlpStatsCol_Deserializer : ValueDeserializer<NlpStatsCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(NlpStatsCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpStatsCol {
    with(p) {
      var __id_: NlpStatsColId? = null
      var __id_set: Boolean = false
      var _stats_: NlpCallStats? = null
      var _stats_set: Boolean = false
      var _appNamespace_: String? = null
      var _appNamespace_set: Boolean = false
      var _date_: Instant? = null
      var _date_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(NlpStatsColId::class.javaObjectType)
            __id_set = true
          }
          "stats" -> {
            _stats_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(NlpCallStats::class.javaObjectType)
            _stats_set = true
          }
          "appNamespace" -> {
            _appNamespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _appNamespace_set = true
          }
          "date" -> {
            _date_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _date_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (__id_set && _stats_set && _appNamespace_set && _date_set) {
        NlpStatsCol(_id = __id_!!, stats = _stats_!!, appNamespace = _appNamespace_!!, date = _date_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_stats_set) map_[parameters.getValue("stats")] = _stats_
        if (_appNamespace_set) map_[parameters.getValue("appNamespace")] = _appNamespace_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<NlpStatsCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { NlpStatsCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "stats" to primaryConstructor.findParameterByName("stats")!!,
            "appNamespace" to primaryConstructor.findParameterByName("appNamespace")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
          )
        }
  }
}
