@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.parser

import java.util.Locale
import kotlin.Double
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KMapSimplePropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __Entities: KProperty1<ParseResult, List<ParsedEntityValue>?>
  get() = ParseResult::entities

private val __EntitiesProbability: KProperty1<ParseResult, Double?>
  get() = ParseResult::entitiesProbability

private val __Intent: KProperty1<ParseResult, String?>
  get() = ParseResult::intent

private val __IntentNamespace: KProperty1<ParseResult, String?>
  get() = ParseResult::intentNamespace

private val __IntentProbability: KProperty1<ParseResult, Double?>
  get() = ParseResult::intentProbability

private val __Language: KProperty1<ParseResult, Locale?>
  get() = ParseResult::language

private val __NotRetainedEntities: KProperty1<ParseResult, List<ParsedEntityValue>?>
  get() = ParseResult::notRetainedEntities

private val __OriginalIntentsProbabilities: KProperty1<ParseResult, Map<String, Double>?>
  get() = ParseResult::originalIntentsProbabilities

private val __OtherIntentsProbabilities: KProperty1<ParseResult, Map<String, Double>?>
  get() = ParseResult::otherIntentsProbabilities

private val __RetainedQuery: KProperty1<ParseResult, String?>
  get() = ParseResult::retainedQuery

public class ParseResult_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ParseResult?>,
) : KPropertyPath<T, ParseResult?>(previous, property) {
  public val entities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
    get() = KCollectionSimplePropertyPath(this, __Entities)

  public val entitiesProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __EntitiesProbability)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val intentNamespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __IntentNamespace)

  public val intentProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __IntentProbability)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val notRetainedEntities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
    get() = KCollectionSimplePropertyPath(this, __NotRetainedEntities)

  public val originalIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
    get() = KMapSimplePropertyPath(this, __OriginalIntentsProbabilities)

  public val otherIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
    get() = KMapSimplePropertyPath(this, __OtherIntentsProbabilities)

  public val retainedQuery: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __RetainedQuery)

  public companion object {
    public val Entities: KCollectionSimplePropertyPath<ParseResult, ParsedEntityValue?>
      get() = KCollectionSimplePropertyPath(null, __Entities)

    public val EntitiesProbability: KProperty1<ParseResult, Double?>
      get() = __EntitiesProbability

    public val Intent: KProperty1<ParseResult, String?>
      get() = __Intent

    public val IntentNamespace: KProperty1<ParseResult, String?>
      get() = __IntentNamespace

    public val IntentProbability: KProperty1<ParseResult, Double?>
      get() = __IntentProbability

    public val Language: KProperty1<ParseResult, Locale?>
      get() = __Language

    public val NotRetainedEntities: KCollectionSimplePropertyPath<ParseResult, ParsedEntityValue?>
      get() = KCollectionSimplePropertyPath(null, __NotRetainedEntities)

    public val OriginalIntentsProbabilities: KMapSimplePropertyPath<ParseResult, String?, Double?>
      get() = KMapSimplePropertyPath(null, __OriginalIntentsProbabilities)

    public val OtherIntentsProbabilities: KMapSimplePropertyPath<ParseResult, String?, Double?>
      get() = KMapSimplePropertyPath(null, __OtherIntentsProbabilities)

    public val RetainedQuery: KProperty1<ParseResult, String?>
      get() = __RetainedQuery
  }
}

public class ParseResult_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ParseResult?>?>,
) : KCollectionPropertyPath<T, ParseResult?, ParseResult_<T>>(previous, property) {
  public val entities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
    get() = KCollectionSimplePropertyPath(this, __Entities)

  public val entitiesProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __EntitiesProbability)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val intentNamespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __IntentNamespace)

  public val intentProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __IntentProbability)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val notRetainedEntities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
    get() = KCollectionSimplePropertyPath(this, __NotRetainedEntities)

  public val originalIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
    get() = KMapSimplePropertyPath(this, __OriginalIntentsProbabilities)

  public val otherIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
    get() = KMapSimplePropertyPath(this, __OtherIntentsProbabilities)

  public val retainedQuery: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __RetainedQuery)

  override fun memberWithAdditionalPath(additionalPath: String): ParseResult_<T> = ParseResult_(this, customProperty(this, additionalPath))
}

public class ParseResult_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ParseResult?>?>,
) : KMapPropertyPath<T, K, ParseResult?, ParseResult_<T>>(previous, property) {
  public val entities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
    get() = KCollectionSimplePropertyPath(this, __Entities)

  public val entitiesProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __EntitiesProbability)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val intentNamespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __IntentNamespace)

  public val intentProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __IntentProbability)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val notRetainedEntities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
    get() = KCollectionSimplePropertyPath(this, __NotRetainedEntities)

  public val originalIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
    get() = KMapSimplePropertyPath(this, __OriginalIntentsProbabilities)

  public val otherIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
    get() = KMapSimplePropertyPath(this, __OtherIntentsProbabilities)

  public val retainedQuery: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __RetainedQuery)

  override fun memberWithAdditionalPath(additionalPath: String): ParseResult_<T> = ParseResult_(this, customProperty(this, additionalPath))
}
