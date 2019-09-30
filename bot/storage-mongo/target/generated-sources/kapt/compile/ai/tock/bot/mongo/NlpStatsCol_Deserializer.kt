package ai.tock.bot.mongo

import ai.tock.bot.engine.nlp.NlpCallStats
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpStatsCol_Deserializer : JsonDeserializer<NlpStatsCol>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NlpStatsCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpStatsCol {
        with(p) {
            var __id_: NlpStatsColId? = null
            var __id_set : Boolean = false
            var _stats_: NlpCallStats? = null
            var _stats_set : Boolean = false
            var _appNamespace_: String? = null
            var _appNamespace_set : Boolean = false
            var _date_: Instant? = null
            var _date_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "_id" -> {
                            __id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpStatsColId::class.java);
                            __id_set = true
                            }
                    "stats" -> {
                            _stats_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpCallStats::class.java);
                            _stats_set = true
                            }
                    "appNamespace" -> {
                            _appNamespace_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _appNamespace_set = true
                            }
                    "date" -> {
                            _date_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _date_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(__id_set && _stats_set && _appNamespace_set && _date_set)
                    NlpStatsCol(_id = __id_!!, stats = _stats_!!, appNamespace = _appNamespace_!!,
                            date = _date_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_stats_set)
                    map[parameters.getValue("stats")] = _stats_
                    if(_appNamespace_set)
                    map[parameters.getValue("appNamespace")] = _appNamespace_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<NlpStatsCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { NlpStatsCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("_id" to primaryConstructor.findParameterByName("_id")!!,
                "stats" to primaryConstructor.findParameterByName("stats")!!, "appNamespace" to
                primaryConstructor.findParameterByName("appNamespace")!!, "date" to
                primaryConstructor.findParameterByName("date")!!) }
    }
}
