package ai.tock.bot.mongo

import ai.tock.bot.engine.nlp.NlpCallStats
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val ___id: KProperty1<NlpStatsCol, NlpStatsColId?>
    get() = NlpStatsCol::_id
private val __Stats: KProperty1<NlpStatsCol, NlpCallStats?>
    get() = NlpStatsCol::stats
private val __AppNamespace: KProperty1<NlpStatsCol, String?>
    get() = NlpStatsCol::appNamespace
private val __Date: KProperty1<NlpStatsCol, Instant?>
    get() = NlpStatsCol::date
internal class NlpStatsCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        NlpStatsCol?>) : KPropertyPath<T, NlpStatsCol?>(previous,property) {
    val _id: NlpStatsColId_<T>
        get() = NlpStatsColId_(this,NlpStatsCol::_id)

    val stats: KPropertyPath<T, NlpCallStats?>
        get() = KPropertyPath(this,__Stats)

    val appNamespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__AppNamespace)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    companion object {
        val _id: NlpStatsColId_<NlpStatsCol>
            get() = NlpStatsColId_(null,___id)
        val Stats: KProperty1<NlpStatsCol, NlpCallStats?>
            get() = __Stats
        val AppNamespace: KProperty1<NlpStatsCol, String?>
            get() = __AppNamespace
        val Date: KProperty1<NlpStatsCol, Instant?>
            get() = __Date}
}

internal class NlpStatsCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<NlpStatsCol>?>) : KCollectionPropertyPath<T, NlpStatsCol?,
        NlpStatsCol_<T>>(previous,property) {
    val _id: NlpStatsColId_<T>
        get() = NlpStatsColId_(this,NlpStatsCol::_id)

    val stats: KPropertyPath<T, NlpCallStats?>
        get() = KPropertyPath(this,__Stats)

    val appNamespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__AppNamespace)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NlpStatsCol_<T> =
            NlpStatsCol_(this, customProperty(this, additionalPath))}

internal class NlpStatsCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        NlpStatsCol>?>) : KMapPropertyPath<T, K, NlpStatsCol?, NlpStatsCol_<T>>(previous,property) {
    val _id: NlpStatsColId_<T>
        get() = NlpStatsColId_(this,NlpStatsCol::_id)

    val stats: KPropertyPath<T, NlpCallStats?>
        get() = KPropertyPath(this,__Stats)

    val appNamespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__AppNamespace)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NlpStatsCol_<T> =
            NlpStatsCol_(this, customProperty(this, additionalPath))}
