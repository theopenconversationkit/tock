package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfiguration_
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Configuration: KProperty1<ConfigurationLookup, BotApplicationConfiguration?>
    get() = ConfigurationLookup::configuration
internal class ConfigurationLookup_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ConfigurationLookup?>) : KPropertyPath<T, ConfigurationLookup?>(previous,property) {
    val configuration: BotApplicationConfiguration_<T>
        get() = BotApplicationConfiguration_(this,ConfigurationLookup::configuration)

    companion object {
        val Configuration: BotApplicationConfiguration_<ConfigurationLookup>
            get() = BotApplicationConfiguration_(null,__Configuration)}
}

internal class ConfigurationLookup_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ConfigurationLookup>?>) : KCollectionPropertyPath<T, ConfigurationLookup?,
        ConfigurationLookup_<T>>(previous,property) {
    val configuration: BotApplicationConfiguration_<T>
        get() = BotApplicationConfiguration_(this,ConfigurationLookup::configuration)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ConfigurationLookup_<T> =
            ConfigurationLookup_(this, customProperty(this, additionalPath))}

internal class ConfigurationLookup_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, ConfigurationLookup>?>) : KMapPropertyPath<T, K, ConfigurationLookup?,
        ConfigurationLookup_<T>>(previous,property) {
    val configuration: BotApplicationConfiguration_<T>
        get() = BotApplicationConfiguration_(this,ConfigurationLookup::configuration)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ConfigurationLookup_<T> =
            ConfigurationLookup_(this, customProperty(this, additionalPath))}
