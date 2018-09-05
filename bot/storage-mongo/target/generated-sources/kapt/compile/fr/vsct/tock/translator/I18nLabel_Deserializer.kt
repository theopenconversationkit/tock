package fr.vsct.tock.translator

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.util.LinkedHashSet
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class I18nLabel_Deserializer : StdDeserializer<I18nLabel>(I18nLabel::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(I18nLabel::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): I18nLabel {
        with(p) {
        var _id: Id<I18nLabel>? = null
        var namespace: String? = null
        var category: String? = null
        var i18n: LinkedHashSet<I18nLocalizedLabel>? = null
        var defaultLabel: String? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "_id" -> _id = p.readValueAs(_id_reference)
        "namespace" -> namespace = p.text
        "category" -> category = p.text
        "i18n" -> i18n = p.readValueAs(i18n_reference)
        "defaultLabel" -> defaultLabel = p.text
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return I18nLabel(_id!!, namespace!!, category!!, i18n!!, defaultLabel) }
    }
    companion object {
        val _id_reference: TypeReference<Id<I18nLabel>> = object : TypeReference<Id<I18nLabel>>() {}

        val i18n_reference: TypeReference<LinkedHashSet<I18nLocalizedLabel>> =
                object : TypeReference<LinkedHashSet<I18nLocalizedLabel>>() {}
    }
}
