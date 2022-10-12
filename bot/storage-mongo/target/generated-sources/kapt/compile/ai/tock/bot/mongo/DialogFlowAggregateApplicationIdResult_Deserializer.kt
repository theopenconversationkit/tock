package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.LocalDateTime
import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class DialogFlowAggregateApplicationIdResult_Deserializer :
        JsonDeserializer<DialogFlowAggregateApplicationIdResult>(), JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(DialogFlowAggregateApplicationIdResult::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            DialogFlowAggregateApplicationIdResult {
        with(p) {
            var _applicationId_: Id<BotApplicationConfiguration>? = null
            var _applicationId_set : Boolean = false
            var _date_: LocalDateTime? = null
            var _date_set : Boolean = false
            var _count_: Int? = null
            var _count_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "applicationId" -> {
                            _applicationId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationId__reference);
                            _applicationId_set = true
                            }
                    "date" -> {
                            _date_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(LocalDateTime::class.java);
                            _date_set = true
                            }
                    "count" -> {
                            _count_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _count_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_applicationId_set && _date_set && _count_set)
                    DialogFlowAggregateApplicationIdResult(applicationId = _applicationId_!!, date =
                            _date_!!, count = _count_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_applicationId_set)
                    map[parameters.getValue("applicationId")] = _applicationId_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_
                    if(_count_set)
                    map[parameters.getValue("count")] = _count_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<DialogFlowAggregateApplicationIdResult> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                DialogFlowAggregateApplicationIdResult::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("applicationId" to
                primaryConstructor.findParameterByName("applicationId")!!, "date" to
                primaryConstructor.findParameterByName("date")!!, "count" to
                primaryConstructor.findParameterByName("count")!!) }

        private val _applicationId__reference: TypeReference<Id<BotApplicationConfiguration>> =
                object : TypeReference<Id<BotApplicationConfiguration>>() {}
    }
}
