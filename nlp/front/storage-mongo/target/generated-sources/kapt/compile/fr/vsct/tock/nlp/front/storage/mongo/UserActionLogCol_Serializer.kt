package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class UserActionLogCol_Serializer :
        StdSerializer<UserActionLogMongoDAO.UserActionLogCol>(UserActionLogMongoDAO.UserActionLogCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(UserActionLogMongoDAO.UserActionLogCol::class.java, this)

    override fun serialize(
        value: UserActionLogMongoDAO.UserActionLogCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        if(_applicationId_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_applicationId_, gen)
                }
        gen.writeFieldName("login")
        val _login_ = value.login
        gen.writeString(_login_)
        gen.writeFieldName("actionType")
        val _actionType_ = value.actionType
        gen.writeString(_actionType_)
        gen.writeFieldName("newData")
        val _newData_ = value.newData
        if(_newData_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_newData_, gen)
                }
        gen.writeFieldName("error")
        val _error_ = value.error
        gen.writeBoolean(_error_)
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeEndObject()
    }
}
