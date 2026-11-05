@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import java.util.Locale
import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id:
    KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::_id

private val __ApplicationId:
    KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Id<ApplicationDefinition>?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::applicationId

private val __AverageDiff: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Double?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::averageDiff

private val __Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Long?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::count

private val __Intent1: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, String?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::intent1

private val __Intent2: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, String?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::intent2

private val __Language: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Locale?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::language

internal class ParseRequestLogIntentStatCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?>,
) : KPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?>(previous, property) {
  public val _id: KPropertyPath<T, Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
    get() = KPropertyPath(this, ___id)

  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val averageDiff: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __AverageDiff)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val intent1: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent1)

  public val intent2: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent2)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public companion object {
    public val _id:
        KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
      get() = ___id

    public val ApplicationId:
        KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Id<ApplicationDefinition>?>
      get() = __ApplicationId

    public val AverageDiff:
        KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Double?>
      get() = __AverageDiff

    public val Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Long?>
      get() = __Count

    public val Intent1: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, String?>
      get() = __Intent1

    public val Intent2: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, String?>
      get() = __Intent2

    public val Language: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Locale?>
      get() = __Language
  }
}

internal class ParseRequestLogIntentStatCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?>?>,
) : KCollectionPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?, ParseRequestLogIntentStatCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
    get() = KPropertyPath(this, ___id)

  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val averageDiff: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __AverageDiff)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val intent1: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent1)

  public val intent2: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent2)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogIntentStatCol_<T> = ParseRequestLogIntentStatCol_(this, customProperty(this, additionalPath))
}

internal class ParseRequestLogIntentStatCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?>?>,
) : KMapPropertyPath<T, K, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?, ParseRequestLogIntentStatCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
    get() = KPropertyPath(this, ___id)

  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val averageDiff: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __AverageDiff)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val intent1: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent1)

  public val intent2: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent2)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogIntentStatCol_<T> = ParseRequestLogIntentStatCol_(this, customProperty(this, additionalPath))
}
