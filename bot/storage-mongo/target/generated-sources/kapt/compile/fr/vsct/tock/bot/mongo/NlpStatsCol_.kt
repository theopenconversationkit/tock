package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.nlp.NlpCallStats
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class NlpStatsCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, NlpStatsCol?>) : KPropertyPath<T, NlpStatsCol?>(previous,property) {
    val _id: NlpStatsColId_<T>
        get() = NlpStatsColId_(this,NlpStatsCol::_id)

    val stats: KPropertyPath<T, NlpCallStats?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.bot.engine.nlp.NlpCallStats?>(this,NlpStatsCol::stats)

    val appNamespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,NlpStatsCol::appNamespace)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,NlpStatsCol::date)
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

    val stats: KPropertyPath<T, NlpCallStats?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.bot.engine.nlp.NlpCallStats?>(this,NlpStatsCol::stats)

    val appNamespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,NlpStatsCol::appNamespace)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,NlpStatsCol::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NlpStatsCol_<T> = NlpStatsCol_(this, customProperty(this, additionalPath))}

internal class NlpStatsCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, NlpStatsCol>?>) : KMapPropertyPath<T, K, NlpStatsCol?, NlpStatsCol_<T>>(previous,property) {
    val _id: NlpStatsColId_<T>
        get() = NlpStatsColId_(this,NlpStatsCol::_id)

    val stats: KPropertyPath<T, NlpCallStats?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.bot.engine.nlp.NlpCallStats?>(this,NlpStatsCol::stats)

    val appNamespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,NlpStatsCol::appNamespace)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,NlpStatsCol::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NlpStatsCol_<T> = NlpStatsCol_(this, customProperty(this, additionalPath))}
