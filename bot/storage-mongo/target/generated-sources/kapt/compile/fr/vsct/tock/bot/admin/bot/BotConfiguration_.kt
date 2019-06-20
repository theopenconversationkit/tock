package fr.vsct.tock.bot.admin.bot

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Name: KProperty1<BotConfiguration, String?>
    get() = BotConfiguration::name
private val __BotId: KProperty1<BotConfiguration, String?>
    get() = BotConfiguration::botId
private val __Namespace: KProperty1<BotConfiguration, String?>
    get() = BotConfiguration::namespace
private val __NlpModel: KProperty1<BotConfiguration, String?>
    get() = BotConfiguration::nlpModel
private val __ApiKey: KProperty1<BotConfiguration, String?>
    get() = BotConfiguration::apiKey
private val __WebhookUrl: KProperty1<BotConfiguration, String?>
    get() = BotConfiguration::webhookUrl
class BotConfiguration_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        BotConfiguration?>) : KPropertyPath<T, BotConfiguration?>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__NlpModel)

    val apiKey: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApiKey)

    val webhookUrl: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__WebhookUrl)

    companion object {
        val Name: KProperty1<BotConfiguration, String?>
            get() = __Name
        val BotId: KProperty1<BotConfiguration, String?>
            get() = __BotId
        val Namespace: KProperty1<BotConfiguration, String?>
            get() = __Namespace
        val NlpModel: KProperty1<BotConfiguration, String?>
            get() = __NlpModel
        val ApiKey: KProperty1<BotConfiguration, String?>
            get() = __ApiKey
        val WebhookUrl: KProperty1<BotConfiguration, String?>
            get() = __WebhookUrl}
}

class BotConfiguration_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<BotConfiguration>?>) : KCollectionPropertyPath<T, BotConfiguration?,
        BotConfiguration_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__NlpModel)

    val apiKey: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApiKey)

    val webhookUrl: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__WebhookUrl)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): BotConfiguration_<T> =
            BotConfiguration_(this, customProperty(this, additionalPath))}

class BotConfiguration_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        BotConfiguration>?>) : KMapPropertyPath<T, K, BotConfiguration?,
        BotConfiguration_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__NlpModel)

    val apiKey: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApiKey)

    val webhookUrl: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__WebhookUrl)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): BotConfiguration_<T> =
            BotConfiguration_(this, customProperty(this, additionalPath))}
