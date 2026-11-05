@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.storage.mongo

import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id:
    KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, ParseRequestLogMongoDAO.DayAndYear?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::_id

private val __Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Int?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::count

private val __Duration: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Double?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::duration

private val __EntitiesProbability:
    KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Double?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::entitiesProbability

private val __Error: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Int?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::error

private val __IntentProbability:
    KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Double?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::intentProbability

internal class ParseRequestLogStatResult_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ParseRequestLogMongoDAO.ParseRequestLogStatResult?>,
) : KPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogStatResult?>(previous, property) {
  public val _id: DayAndYear_<T>
    get() = DayAndYear_(this, ___id)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val duration: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __Duration)

  public val entitiesProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __EntitiesProbability)

  public val error: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Error)

  public val intentProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __IntentProbability)

  public companion object {
    public val _id: DayAndYear_<ParseRequestLogMongoDAO.ParseRequestLogStatResult>
      get() = DayAndYear_(null, ___id)

    public val Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Int?>
      get() = __Count

    public val Duration: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Double?>
      get() = __Duration

    public val EntitiesProbability:
        KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Double?>
      get() = __EntitiesProbability

    public val Error: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Int?>
      get() = __Error

    public val IntentProbability:
        KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Double?>
      get() = __IntentProbability
  }
}

internal class ParseRequestLogStatResult_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ParseRequestLogMongoDAO.ParseRequestLogStatResult?>?>,
) : KCollectionPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogStatResult?, ParseRequestLogStatResult_<T>>(previous, property) {
  public val _id: DayAndYear_<T>
    get() = DayAndYear_(this, ___id)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val duration: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __Duration)

  public val entitiesProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __EntitiesProbability)

  public val error: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Error)

  public val intentProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __IntentProbability)

  override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogStatResult_<T> = ParseRequestLogStatResult_(this, customProperty(this, additionalPath))
}

internal class ParseRequestLogStatResult_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ParseRequestLogMongoDAO.ParseRequestLogStatResult?>?>,
) : KMapPropertyPath<T, K, ParseRequestLogMongoDAO.ParseRequestLogStatResult?, ParseRequestLogStatResult_<T>>(previous, property) {
  public val _id: DayAndYear_<T>
    get() = DayAndYear_(this, ___id)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val duration: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __Duration)

  public val entitiesProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __EntitiesProbability)

  public val error: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Error)

  public val intentProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __IntentProbability)

  override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogStatResult_<T> = ParseRequestLogStatResult_(this, customProperty(this, additionalPath))
}
