package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.engine.nlp.NlpCallStats
import java.time.Instant
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpStatsCol_Deserializer : StdDeserializer<NlpStatsCol>(NlpStatsCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NlpStatsCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpStatsCol {
        with(p) {
            var __id_: NlpStatsColId? = null
            var __id_set = false
            var _stats_: NlpCallStats? = null
            var _stats_set = false
            var _appNamespace_: String? = null
            var _appNamespace_set = false
            var _date_: Instant? = null
            var _date_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "_id" -> {
                            __id_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpStatsColId::class.java);
                            __id_set = true
                            }
                    "stats" -> {
                            _stats_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpCallStats::class.java);
                            _stats_set = true
                            }
                    "appNamespace" -> {
                            _appNamespace_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _appNamespace_set = true
                            }
                    "date" -> {
                            _date_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _date_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
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
