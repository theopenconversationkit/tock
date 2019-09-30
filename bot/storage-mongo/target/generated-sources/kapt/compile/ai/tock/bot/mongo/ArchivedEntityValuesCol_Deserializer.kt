package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ArchivedEntityValuesCol_Deserializer : JsonDeserializer<ArchivedEntityValuesCol>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ArchivedEntityValuesCol::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ArchivedEntityValuesCol {
        with(p) {
            var __id_: Id<ArchivedEntityValuesCol>? = null
            var __id_set : Boolean = false
            var _values_: MutableList<ArchivedEntityValuesCol.ArchivedEntityValueWrapper>? = null
            var _values_set : Boolean = false
            var _lastUpdateDate_: Instant? = null
            var _lastUpdateDate_set : Boolean = false
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
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    "values" -> {
                            _values_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_values__reference);
                            _values_set = true
                            }
                    "lastUpdateDate" -> {
                            _lastUpdateDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _lastUpdateDate_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(__id_set && _values_set && _lastUpdateDate_set)
                    ArchivedEntityValuesCol(_id = __id_!!, values = _values_!!, lastUpdateDate =
                            _lastUpdateDate_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_values_set)
                    map[parameters.getValue("values")] = _values_
                    if(_lastUpdateDate_set)
                    map[parameters.getValue("lastUpdateDate")] = _lastUpdateDate_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ArchivedEntityValuesCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ArchivedEntityValuesCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("_id" to primaryConstructor.findParameterByName("_id")!!,
                "values" to primaryConstructor.findParameterByName("values")!!, "lastUpdateDate" to
                primaryConstructor.findParameterByName("lastUpdateDate")!!) }

        private val __id__reference: TypeReference<Id<ArchivedEntityValuesCol>> = object :
                TypeReference<Id<ArchivedEntityValuesCol>>() {}

        private val _values__reference:
                TypeReference<List<ArchivedEntityValuesCol.ArchivedEntityValueWrapper>> = object :
                TypeReference<List<ArchivedEntityValuesCol.ArchivedEntityValueWrapper>>() {}
    }
}
