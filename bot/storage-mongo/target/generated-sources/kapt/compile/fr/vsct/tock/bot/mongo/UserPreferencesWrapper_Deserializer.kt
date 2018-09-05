package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.ZoneId
import java.util.Locale
import kotlin.Boolean
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader

internal class UserPreferencesWrapper_Deserializer : StdDeserializer<UserTimelineCol.UserPreferencesWrapper>(UserTimelineCol.UserPreferencesWrapper::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(UserTimelineCol.UserPreferencesWrapper::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UserTimelineCol.UserPreferencesWrapper {
        with(p) {
        var firstName: String? = null
        var lastName: String? = null
        var email: String? = null
        var timezone: ZoneId? = null
        var locale: Locale? = null
        var picture: String? = null
        var gender: String? = null
        var test: Boolean? = null
        var encrypted: Boolean? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "firstName" -> firstName = p.text
        "lastName" -> lastName = p.text
        "email" -> email = p.text
        "timezone" -> timezone = p.readValueAs(ZoneId::class.java)
        "locale" -> locale = p.readValueAs(Locale::class.java)
        "picture" -> picture = p.text
        "gender" -> gender = p.text
        "test" -> test = p.readValueAs(Boolean::class.java)
        "encrypted" -> encrypted = p.readValueAs(Boolean::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return UserTimelineCol.UserPreferencesWrapper(firstName, lastName, email, timezone!!, locale!!, picture, gender, test!!, encrypted!!)
                }
    }
    companion object
}
