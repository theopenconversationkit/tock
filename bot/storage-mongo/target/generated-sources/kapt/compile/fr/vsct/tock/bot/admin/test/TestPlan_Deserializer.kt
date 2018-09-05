package fr.vsct.tock.bot.admin.test

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.message.Message
import java.util.Locale
import kotlin.String
import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class TestPlan_Deserializer : StdDeserializer<TestPlan>(TestPlan::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(TestPlan::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TestPlan {
        with(p) {
        var dialogs: List<TestDialogReport>? = null
        var name: String? = null
        var applicationId: String? = null
        var namespace: String? = null
        var nlpModel: String? = null
        var botApplicationConfigurationId: Id<BotApplicationConfiguration>? = null
        var locale: Locale? = null
        var startAction: Message? = null
        var targetConnectorType: ConnectorType? = null
        var _id: Id<TestPlan>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "dialogs" -> dialogs = p.readValueAs(dialogs_reference)
        "name" -> name = p.text
        "applicationId" -> applicationId = p.text
        "namespace" -> namespace = p.text
        "nlpModel" -> nlpModel = p.text
        "botApplicationConfigurationId" -> botApplicationConfigurationId = p.readValueAs(botApplicationConfigurationId_reference)
        "locale" -> locale = p.readValueAs(Locale::class.java)
        "startAction" -> startAction = p.readValueAs(Message::class.java)
        "targetConnectorType" -> targetConnectorType = p.readValueAs(ConnectorType::class.java)
        "_id" -> _id = p.readValueAs(_id_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return TestPlan(dialogs!!, name!!, applicationId!!, namespace!!, nlpModel!!, botApplicationConfigurationId!!, locale!!, startAction, targetConnectorType!!, _id!!)
                }
    }
    companion object {
        val dialogs_reference: TypeReference<List<TestDialogReport>> =
                object : TypeReference<List<TestDialogReport>>() {}

        val botApplicationConfigurationId_reference: TypeReference<Id<BotApplicationConfiguration>> =
                object : TypeReference<Id<BotApplicationConfiguration>>() {}

        val _id_reference: TypeReference<Id<TestPlan>> = object : TypeReference<Id<TestPlan>>() {}
    }
}
