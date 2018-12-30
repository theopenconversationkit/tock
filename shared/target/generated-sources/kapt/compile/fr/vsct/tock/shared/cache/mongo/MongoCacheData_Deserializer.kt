package fr.vsct.tock.shared.cache.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.shared.jackson.AnyValueWrapper
import java.time.Instant
import kotlin.ByteArray
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class MongoCacheData_Deserializer :
        StdDeserializer<MongoCacheData>(MongoCacheData::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(MongoCacheData::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MongoCacheData {
        with(p) {
            var _id_: Id<*>? = null
            var _id_set = false
            var _type_: String? = null
            var _type_set = false
            var _s_: String? = null
            var _s_set = false
            var _b_: ByteArray? = null
            var _b_set = false
            var _a_: AnyValueWrapper? = null
            var _a_set = false
            var _date_: Instant? = null
            var _date_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "id" -> {
                            _id_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_id__reference);
                            _id_set = true
                            }
                    "type" -> {
                            _type_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _type_set = true
                            }
                    "s" -> {
                            _s_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _s_set = true
                            }
                    "b" -> {
                            _b_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_b__reference);
                            _b_set = true
                            }
                    "a" -> {
                            _a_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(AnyValueWrapper::class.java);
                            _a_set = true
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
            return if(_id_set && _type_set && _s_set && _b_set && _a_set && _date_set)
                    MongoCacheData(id = _id_!!, type = _type_!!, s = _s_, b = _b_, a = _a_, date =
                            _date_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_id_set)
                    map[parameters.getValue("id")] = _id_
                    if(_type_set)
                    map[parameters.getValue("type")] = _type_
                    if(_s_set)
                    map[parameters.getValue("s")] = _s_
                    if(_b_set)
                    map[parameters.getValue("b")] = _b_
                    if(_a_set)
                    map[parameters.getValue("a")] = _a_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<MongoCacheData> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { MongoCacheData::class.primaryConstructor!!
                }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("id" to primaryConstructor.findParameterByName("id")!!,
                "type" to primaryConstructor.findParameterByName("type")!!, "s" to
                primaryConstructor.findParameterByName("s")!!, "b" to
                primaryConstructor.findParameterByName("b")!!, "a" to
                primaryConstructor.findParameterByName("a")!!, "date" to
                primaryConstructor.findParameterByName("date")!!) }

        private val _id__reference: TypeReference<Id<*>> = object : TypeReference<Id<*>>() {}

        private val _b__reference: TypeReference<ByteArray> = object : TypeReference<ByteArray>() {}
    }
}
