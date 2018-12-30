package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class DayAndYear_Deserializer :
        StdDeserializer<ParseRequestLogMongoDAO.DayAndYear>(ParseRequestLogMongoDAO.DayAndYear::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(ParseRequestLogMongoDAO.DayAndYear::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            ParseRequestLogMongoDAO.DayAndYear {
        with(p) {
            var _dayOfYear_: Int? = null
            var _dayOfYear_set = false
            var _year_: Int? = null
            var _year_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "dayOfYear" -> {
                            _dayOfYear_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Int::class.java);
                            _dayOfYear_set = true
                            }
                    "year" -> {
                            _year_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Int::class.java);
                            _year_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_dayOfYear_set && _year_set)
                    ParseRequestLogMongoDAO.DayAndYear(dayOfYear = _dayOfYear_!!, year = _year_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_dayOfYear_set)
                    map[parameters.getValue("dayOfYear")] = _dayOfYear_
                    if(_year_set)
                    map[parameters.getValue("year")] = _year_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ParseRequestLogMongoDAO.DayAndYear> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ParseRequestLogMongoDAO.DayAndYear::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("dayOfYear" to
                primaryConstructor.findParameterByName("dayOfYear")!!, "year" to
                primaryConstructor.findParameterByName("year")!!) }
    }
}
