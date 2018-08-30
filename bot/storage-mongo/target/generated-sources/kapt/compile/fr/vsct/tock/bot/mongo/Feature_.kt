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

internal class Feature_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Feature?>) : KPropertyPath<T, Feature?>(previous,property) {
    val _id: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,Feature::_id)

    val key: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,Feature::key)

    val enabled: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,Feature::enabled)

    val botId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,Feature::botId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,Feature::namespace)
    companion object {
        val _id: KProperty1<Feature, String?>
            get() = Feature::_id
        val Key: KProperty1<Feature, String?>
            get() = Feature::key
        val Enabled: KProperty1<Feature, Boolean?>
            get() = Feature::enabled
        val BotId: KProperty1<Feature, String?>
            get() = Feature::botId
        val Namespace: KProperty1<Feature, String?>
            get() = Feature::namespace}
}

internal class Feature_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<Feature>?>) : KCollectionPropertyPath<T, Feature?, Feature_<T>>(previous,property) {
    val _id: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,Feature::_id)

    val key: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,Feature::key)

    val enabled: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,Feature::enabled)

    val botId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,Feature::botId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,Feature::namespace)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): Feature_<T> = Feature_(this, customProperty(this, additionalPath))}

internal class Feature_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, Feature>?>) : KMapPropertyPath<T, K, Feature?, Feature_<T>>(previous,property) {
    val _id: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,Feature::_id)

    val key: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,Feature::key)

    val enabled: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,Feature::enabled)

    val botId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,Feature::botId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,Feature::namespace)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): Feature_<T> = Feature_(this, customProperty(this, additionalPath))}
