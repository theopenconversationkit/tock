@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.test

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KMapSimplePropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ApplicationId: KProperty1<TestBuild, Id<ApplicationDefinition>?>
  get() = TestBuild::applicationId

private val __BuildModelDuration: KProperty1<TestBuild, Duration?>
  get() = TestBuild::buildModelDuration

private val __EntityErrors: KProperty1<TestBuild, Int?>
  get() = TestBuild::entityErrors

private val __EntityErrorsByIntent: KProperty1<TestBuild, Map<String, Int>?>
  get() = TestBuild::entityErrorsByIntent

private val __IntentErrors: KProperty1<TestBuild, Int?>
  get() = TestBuild::intentErrors

private val __IntentErrorsByIntent: KProperty1<TestBuild, Map<String, Int>?>
  get() = TestBuild::intentErrorsByIntent

private val __Language: KProperty1<TestBuild, Locale?>
  get() = TestBuild::language

private val __NbErrors: KProperty1<TestBuild, Int?>
  get() = TestBuild::nbErrors

private val __NbSentencesInModel: KProperty1<TestBuild, Int?>
  get() = TestBuild::nbSentencesInModel

private val __NbSentencesTested: KProperty1<TestBuild, Int?>
  get() = TestBuild::nbSentencesTested

private val __NbSentencesTestedByIntent: KProperty1<TestBuild, Map<String, Int>?>
  get() = TestBuild::nbSentencesTestedByIntent

private val __StartDate: KProperty1<TestBuild, Instant?>
  get() = TestBuild::startDate

private val __TestSentencesDuration: KProperty1<TestBuild, Duration?>
  get() = TestBuild::testSentencesDuration

public class TestBuild_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, TestBuild?>,
) : KPropertyPath<T, TestBuild?>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val buildModelDuration: KPropertyPath<T, Duration?>
    get() = KPropertyPath(this, __BuildModelDuration)

  public val entityErrors: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __EntityErrors)

  public val entityErrorsByIntent: KMapSimplePropertyPath<T, String?, Int?>
    get() = KMapSimplePropertyPath(this, __EntityErrorsByIntent)

  public val intentErrors: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __IntentErrors)

  public val intentErrorsByIntent: KMapSimplePropertyPath<T, String?, Int?>
    get() = KMapSimplePropertyPath(this, __IntentErrorsByIntent)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val nbErrors: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __NbErrors)

  public val nbSentencesInModel: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __NbSentencesInModel)

  public val nbSentencesTested: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __NbSentencesTested)

  public val nbSentencesTestedByIntent: KMapSimplePropertyPath<T, String?, Int?>
    get() = KMapSimplePropertyPath(this, __NbSentencesTestedByIntent)

  public val startDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __StartDate)

  public val testSentencesDuration: KPropertyPath<T, Duration?>
    get() = KPropertyPath(this, __TestSentencesDuration)

  public companion object {
    public val ApplicationId: KProperty1<TestBuild, Id<ApplicationDefinition>?>
      get() = __ApplicationId

    public val BuildModelDuration: KProperty1<TestBuild, Duration?>
      get() = __BuildModelDuration

    public val EntityErrors: KProperty1<TestBuild, Int?>
      get() = __EntityErrors

    public val EntityErrorsByIntent: KMapSimplePropertyPath<TestBuild, String?, Int?>
      get() = KMapSimplePropertyPath(null, __EntityErrorsByIntent)

    public val IntentErrors: KProperty1<TestBuild, Int?>
      get() = __IntentErrors

    public val IntentErrorsByIntent: KMapSimplePropertyPath<TestBuild, String?, Int?>
      get() = KMapSimplePropertyPath(null, __IntentErrorsByIntent)

    public val Language: KProperty1<TestBuild, Locale?>
      get() = __Language

    public val NbErrors: KProperty1<TestBuild, Int?>
      get() = __NbErrors

    public val NbSentencesInModel: KProperty1<TestBuild, Int?>
      get() = __NbSentencesInModel

    public val NbSentencesTested: KProperty1<TestBuild, Int?>
      get() = __NbSentencesTested

    public val NbSentencesTestedByIntent: KMapSimplePropertyPath<TestBuild, String?, Int?>
      get() = KMapSimplePropertyPath(null, __NbSentencesTestedByIntent)

    public val StartDate: KProperty1<TestBuild, Instant?>
      get() = __StartDate

    public val TestSentencesDuration: KProperty1<TestBuild, Duration?>
      get() = __TestSentencesDuration
  }
}

public class TestBuild_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<TestBuild?>?>,
) : KCollectionPropertyPath<T, TestBuild?, TestBuild_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val buildModelDuration: KPropertyPath<T, Duration?>
    get() = KPropertyPath(this, __BuildModelDuration)

  public val entityErrors: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __EntityErrors)

  public val entityErrorsByIntent: KMapSimplePropertyPath<T, String?, Int?>
    get() = KMapSimplePropertyPath(this, __EntityErrorsByIntent)

  public val intentErrors: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __IntentErrors)

  public val intentErrorsByIntent: KMapSimplePropertyPath<T, String?, Int?>
    get() = KMapSimplePropertyPath(this, __IntentErrorsByIntent)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val nbErrors: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __NbErrors)

  public val nbSentencesInModel: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __NbSentencesInModel)

  public val nbSentencesTested: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __NbSentencesTested)

  public val nbSentencesTestedByIntent: KMapSimplePropertyPath<T, String?, Int?>
    get() = KMapSimplePropertyPath(this, __NbSentencesTestedByIntent)

  public val startDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __StartDate)

  public val testSentencesDuration: KPropertyPath<T, Duration?>
    get() = KPropertyPath(this, __TestSentencesDuration)

  override fun memberWithAdditionalPath(additionalPath: String): TestBuild_<T> = TestBuild_(this, customProperty(this, additionalPath))
}

public class TestBuild_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, TestBuild?>?>,
) : KMapPropertyPath<T, K, TestBuild?, TestBuild_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val buildModelDuration: KPropertyPath<T, Duration?>
    get() = KPropertyPath(this, __BuildModelDuration)

  public val entityErrors: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __EntityErrors)

  public val entityErrorsByIntent: KMapSimplePropertyPath<T, String?, Int?>
    get() = KMapSimplePropertyPath(this, __EntityErrorsByIntent)

  public val intentErrors: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __IntentErrors)

  public val intentErrorsByIntent: KMapSimplePropertyPath<T, String?, Int?>
    get() = KMapSimplePropertyPath(this, __IntentErrorsByIntent)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val nbErrors: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __NbErrors)

  public val nbSentencesInModel: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __NbSentencesInModel)

  public val nbSentencesTested: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __NbSentencesTested)

  public val nbSentencesTestedByIntent: KMapSimplePropertyPath<T, String?, Int?>
    get() = KMapSimplePropertyPath(this, __NbSentencesTestedByIntent)

  public val startDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __StartDate)

  public val testSentencesDuration: KPropertyPath<T, Duration?>
    get() = KPropertyPath(this, __TestSentencesDuration)

  override fun memberWithAdditionalPath(additionalPath: String): TestBuild_<T> = TestBuild_(this, customProperty(this, additionalPath))
}
