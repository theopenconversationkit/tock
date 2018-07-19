package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.nlp.NlpCallStats
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class NlpStatsCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, NlpStatsCol?>) : KPropertyPath<T, NlpStatsCol?>(previous,property) {
    val _id: NlpStatsColId_<T>
        get() = NlpStatsColId_(this,NlpStatsCol::_id)

    val stats: KProperty1<T, NlpCallStats?>
        get() = org.litote.kmongo.property.KPropertyPath(this,NlpStatsCol::stats)

    val appNamespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,NlpStatsCol::appNamespace)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,NlpStatsCol::date)
    companion object {
        val _id: NlpStatsColId_<NlpStatsCol>
            get() = NlpStatsColId_<NlpStatsCol>(null,NlpStatsCol::_id)
        val Stats: KProperty1<NlpStatsCol, NlpCallStats?>
            get() = NlpStatsCol::stats
        val AppNamespace: KProperty1<NlpStatsCol, String?>
            get() = NlpStatsCol::appNamespace
        val Date: KProperty1<NlpStatsCol, Instant?>
            get() = NlpStatsCol::date}
}

internal class NlpStatsCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<NlpStatsCol>?>) : KCollectionPropertyPath<T, NlpStatsCol?, NlpStatsCol_<T>>(previous,property) {
    val _id: NlpStatsColId_<T>
        get() = NlpStatsColId_(this,NlpStatsCol::_id)

    val stats: KProperty1<T, NlpCallStats?>
        get() = org.litote.kmongo.property.KPropertyPath(this,NlpStatsCol::stats)

    val appNamespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,NlpStatsCol::appNamespace)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,NlpStatsCol::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NlpStatsCol_<T> = NlpStatsCol_(this, customProperty(this, additionalPath))}
