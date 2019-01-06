package fr.vsct.tock.bot.mongo

import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val ___id: KProperty1<Feature, String?>
    get() = Feature::_id
private val __Key: KProperty1<Feature, String?>
    get() = Feature::key
private val __Enabled: KProperty1<Feature, Boolean?>
    get() = Feature::enabled
private val __BotId: KProperty1<Feature, String?>
    get() = Feature::botId
private val __Namespace: KProperty1<Feature, String?>
    get() = Feature::namespace
internal class Feature_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Feature?>) :
        KPropertyPath<T, Feature?>(previous,property) {
    val _id: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,___id)

    val key: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Key)

    val enabled: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__Enabled)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__BotId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Namespace)

    companion object {
        val _id: KProperty1<Feature, String?>
            get() = ___id
        val Key: KProperty1<Feature, String?>
            get() = __Key
        val Enabled: KProperty1<Feature, Boolean?>
            get() = __Enabled
        val BotId: KProperty1<Feature, String?>
            get() = __BotId
        val Namespace: KProperty1<Feature, String?>
            get() = __Namespace}
}

internal class Feature_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<Feature>?>) : KCollectionPropertyPath<T, Feature?,
        Feature_<T>>(previous,property) {
    val _id: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,___id)

    val key: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Key)

    val enabled: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__Enabled)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__BotId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Namespace)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): Feature_<T> = Feature_(this,
            customProperty(this, additionalPath))}

internal class Feature_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        Feature>?>) : KMapPropertyPath<T, K, Feature?, Feature_<T>>(previous,property) {
    val _id: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,___id)

    val key: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Key)

    val enabled: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__Enabled)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__BotId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Namespace)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): Feature_<T> = Feature_(this,
            customProperty(this, additionalPath))}
