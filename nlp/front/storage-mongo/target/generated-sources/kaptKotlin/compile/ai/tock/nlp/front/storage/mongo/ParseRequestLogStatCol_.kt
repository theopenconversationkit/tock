@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Instant
import java.util.Locale
import kotlin.Boolean
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

private val __ApplicationId:
    KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Id<ApplicationDefinition>?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::applicationId

private val __Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Long?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::count

private val __EntitiesProbability:
    KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Double?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::entitiesProbability

private val __Intent: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, String?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::intent

private val __IntentProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Double?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::intentProbability

private val __Language: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Locale?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::language

private val __LastUsage: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Instant?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::lastUsage

private val __Text: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, String?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::text

private val __Validated: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Boolean?>
  get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::validated

internal class ParseRequestLogStatCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ParseRequestLogMongoDAO.ParseRequestLogStatCol?>,
) : KPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogStatCol?>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val entitiesProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __EntitiesProbability)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val intentProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __IntentProbability)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val lastUsage: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUsage)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public val validated: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Validated)

  public companion object {
    public val ApplicationId:
        KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Id<ApplicationDefinition>?>
      get() = __ApplicationId

    public val Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Long?>
      get() = __Count

    public val EntitiesProbability:
        KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Double?>
      get() = __EntitiesProbability

    public val Intent: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, String?>
      get() = __Intent

    public val IntentProbability:
        KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Double?>
      get() = __IntentProbability

    public val Language: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Locale?>
      get() = __Language

    public val LastUsage: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Instant?>
      get() = __LastUsage

    public val Text: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, String?>
      get() = __Text

    public val Validated: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Boolean?>
      get() = __Validated
  }
}

internal class ParseRequestLogStatCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ParseRequestLogMongoDAO.ParseRequestLogStatCol?>?>,
) : KCollectionPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogStatCol?, ParseRequestLogStatCol_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val entitiesProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __EntitiesProbability)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val intentProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __IntentProbability)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val lastUsage: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUsage)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public val validated: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Validated)

  override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogStatCol_<T> = ParseRequestLogStatCol_(this, customProperty(this, additionalPath))
}

internal class ParseRequestLogStatCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ParseRequestLogMongoDAO.ParseRequestLogStatCol?>?>,
) : KMapPropertyPath<T, K, ParseRequestLogMongoDAO.ParseRequestLogStatCol?, ParseRequestLogStatCol_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val entitiesProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __EntitiesProbability)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val intentProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __IntentProbability)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val lastUsage: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUsage)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public val validated: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Validated)

  override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogStatCol_<T> = ParseRequestLogStatCol_(this, customProperty(this, additionalPath))
}
