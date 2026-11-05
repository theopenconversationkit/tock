@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.engine.nlp.NlpCallStats
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<NlpStatsCol, NlpStatsColId?>
  get() = NlpStatsCol::_id

private val __AppNamespace: KProperty1<NlpStatsCol, String?>
  get() = NlpStatsCol::appNamespace

private val __Date: KProperty1<NlpStatsCol, Instant?>
  get() = NlpStatsCol::date

private val __Stats: KProperty1<NlpStatsCol, NlpCallStats?>
  get() = NlpStatsCol::stats

internal class NlpStatsCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, NlpStatsCol?>,
) : KPropertyPath<T, NlpStatsCol?>(previous, property) {
  public val _id: NlpStatsColId_<T>
    get() = NlpStatsColId_(this, ___id)

  public val appNamespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __AppNamespace)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val stats: KPropertyPath<T, NlpCallStats?>
    get() = KPropertyPath(this, __Stats)

  public companion object {
    public val _id: NlpStatsColId_<NlpStatsCol>
      get() = NlpStatsColId_(null, ___id)

    public val AppNamespace: KProperty1<NlpStatsCol, String?>
      get() = __AppNamespace

    public val Date: KProperty1<NlpStatsCol, Instant?>
      get() = __Date

    public val Stats: KProperty1<NlpStatsCol, NlpCallStats?>
      get() = __Stats
  }
}

internal class NlpStatsCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<NlpStatsCol?>?>,
) : KCollectionPropertyPath<T, NlpStatsCol?, NlpStatsCol_<T>>(previous, property) {
  public val _id: NlpStatsColId_<T>
    get() = NlpStatsColId_(this, ___id)

  public val appNamespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __AppNamespace)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val stats: KPropertyPath<T, NlpCallStats?>
    get() = KPropertyPath(this, __Stats)

  override fun memberWithAdditionalPath(additionalPath: String): NlpStatsCol_<T> = NlpStatsCol_(this, customProperty(this, additionalPath))
}

internal class NlpStatsCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, NlpStatsCol?>?>,
) : KMapPropertyPath<T, K, NlpStatsCol?, NlpStatsCol_<T>>(previous, property) {
  public val _id: NlpStatsColId_<T>
    get() = NlpStatsColId_(this, ___id)

  public val appNamespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __AppNamespace)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val stats: KPropertyPath<T, NlpCallStats?>
    get() = KPropertyPath(this, __Stats)

  override fun memberWithAdditionalPath(additionalPath: String): NlpStatsCol_<T> = NlpStatsCol_(this, customProperty(this, additionalPath))
}
