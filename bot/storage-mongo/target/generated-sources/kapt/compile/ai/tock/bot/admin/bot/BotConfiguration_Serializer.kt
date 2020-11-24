package ai.tock.bot.admin.bot

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class BotConfiguration_Serializer :
        StdSerializer<BotConfiguration>(BotConfiguration::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(BotConfiguration::class.java, this)

    override fun serialize(
        value: BotConfiguration,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("name")
        val _name_ = value.name
        gen.writeString(_name_)
        gen.writeFieldName("botId")
        val _botId_ = value.botId
        gen.writeString(_botId_)
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("nlpModel")
        val _nlpModel_ = value.nlpModel
        gen.writeString(_nlpModel_)
        gen.writeFieldName("apiKey")
        val _apiKey_ = value.apiKey
        gen.writeString(_apiKey_)
        gen.writeFieldName("webhookUrl")
        val _webhookUrl_ = value.webhookUrl
        if(_webhookUrl_ == null) { gen.writeNull() } else {
                gen.writeString(_webhookUrl_)
                }
        gen.writeFieldName("supportedLocales")
        val _supportedLocales_ = value.supportedLocales
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(java.util.Locale::class.java)
                ),
                true,
                null
                )
                .serialize(_supportedLocales_, gen, serializers)
        gen.writeEndObject()
    }
}
