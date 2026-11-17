@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.config

import ai.tock.nlp.core.NlpEngineType
import java.util.Locale
import kotlin.Boolean
import kotlin.Double
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KMapSimplePropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<ApplicationDefinition, Id<ApplicationDefinition>?>
  get() = ApplicationDefinition::_id

private val __IntentStatesMap:
    KProperty1<ApplicationDefinition, Map<Id<IntentDefinition>, Set<String>>?>
  get() = ApplicationDefinition::intentStatesMap

private val __Intents: KProperty1<ApplicationDefinition, Set<Id<IntentDefinition>>?>
  get() = ApplicationDefinition::intents

private val __KnownIntentThreshold: KProperty1<ApplicationDefinition, Double?>
  get() = ApplicationDefinition::knownIntentThreshold

private val __Label: KProperty1<ApplicationDefinition, String?>
  get() = ApplicationDefinition::label

private val __MergeEngineTypes: KProperty1<ApplicationDefinition, Boolean?>
  get() = ApplicationDefinition::mergeEngineTypes

private val __Name: KProperty1<ApplicationDefinition, String?>
  get() = ApplicationDefinition::name

private val __Namespace: KProperty1<ApplicationDefinition, String?>
  get() = ApplicationDefinition::namespace

private val __NlpEngineType: KProperty1<ApplicationDefinition, NlpEngineType?>
  get() = ApplicationDefinition::nlpEngineType

private val __NormalizeText: KProperty1<ApplicationDefinition, Boolean?>
  get() = ApplicationDefinition::normalizeText

private val __QualifiedName: KProperty1<ApplicationDefinition, String?>
  get() = ApplicationDefinition::qualifiedName

private val __SupportSubEntities: KProperty1<ApplicationDefinition, Boolean?>
  get() = ApplicationDefinition::supportSubEntities

private val __SupportedLocales: KProperty1<ApplicationDefinition, Set<Locale>?>
  get() = ApplicationDefinition::supportedLocales

private val __UnknownIntentThreshold: KProperty1<ApplicationDefinition, Double?>
  get() = ApplicationDefinition::unknownIntentThreshold

private val __UseEntityModels: KProperty1<ApplicationDefinition, Boolean?>
  get() = ApplicationDefinition::useEntityModels

public class ApplicationDefinition_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ApplicationDefinition?>,
) : KPropertyPath<T, ApplicationDefinition?>(previous, property) {
  public val _id: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, ___id)

  public val intentStatesMap: KMapSimplePropertyPath<T, Id<IntentDefinition>?, Set<String>?>
    get() = KMapSimplePropertyPath(this, __IntentStatesMap)

  public val intents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
    get() = KCollectionSimplePropertyPath(this, __Intents)

  public val knownIntentThreshold: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __KnownIntentThreshold)

  public val label: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Label)

  public val mergeEngineTypes: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __MergeEngineTypes)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nlpEngineType: KPropertyPath<T, NlpEngineType?>
    get() = KPropertyPath(this, __NlpEngineType)

  public val normalizeText: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __NormalizeText)

  public val qualifiedName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __QualifiedName)

  public val supportSubEntities: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __SupportSubEntities)

  public val supportedLocales: KCollectionSimplePropertyPath<T, Locale?>
    get() = KCollectionSimplePropertyPath(this, __SupportedLocales)

  public val unknownIntentThreshold: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __UnknownIntentThreshold)

  public val useEntityModels: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __UseEntityModels)

  public companion object {
    public val _id: KProperty1<ApplicationDefinition, Id<ApplicationDefinition>?>
      get() = ___id

    public val IntentStatesMap:
        KMapSimplePropertyPath<ApplicationDefinition, Id<IntentDefinition>?, Set<String>?>
      get() = KMapSimplePropertyPath(null, __IntentStatesMap)

    public val Intents: KCollectionSimplePropertyPath<ApplicationDefinition, Id<IntentDefinition>?>
      get() = KCollectionSimplePropertyPath(null, __Intents)

    public val KnownIntentThreshold: KProperty1<ApplicationDefinition, Double?>
      get() = __KnownIntentThreshold

    public val Label: KProperty1<ApplicationDefinition, String?>
      get() = __Label

    public val MergeEngineTypes: KProperty1<ApplicationDefinition, Boolean?>
      get() = __MergeEngineTypes

    public val Name: KProperty1<ApplicationDefinition, String?>
      get() = __Name

    public val Namespace: KProperty1<ApplicationDefinition, String?>
      get() = __Namespace

    public val NlpEngineType: KProperty1<ApplicationDefinition, NlpEngineType?>
      get() = __NlpEngineType

    public val NormalizeText: KProperty1<ApplicationDefinition, Boolean?>
      get() = __NormalizeText

    public val QualifiedName: KProperty1<ApplicationDefinition, String?>
      get() = __QualifiedName

    public val SupportSubEntities: KProperty1<ApplicationDefinition, Boolean?>
      get() = __SupportSubEntities

    public val SupportedLocales: KCollectionSimplePropertyPath<ApplicationDefinition, Locale?>
      get() = KCollectionSimplePropertyPath(null, __SupportedLocales)

    public val UnknownIntentThreshold: KProperty1<ApplicationDefinition, Double?>
      get() = __UnknownIntentThreshold

    public val UseEntityModels: KProperty1<ApplicationDefinition, Boolean?>
      get() = __UseEntityModels
  }
}

public class ApplicationDefinition_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ApplicationDefinition?>?>,
) : KCollectionPropertyPath<T, ApplicationDefinition?, ApplicationDefinition_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, ___id)

  public val intentStatesMap: KMapSimplePropertyPath<T, Id<IntentDefinition>?, Set<String>?>
    get() = KMapSimplePropertyPath(this, __IntentStatesMap)

  public val intents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
    get() = KCollectionSimplePropertyPath(this, __Intents)

  public val knownIntentThreshold: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __KnownIntentThreshold)

  public val label: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Label)

  public val mergeEngineTypes: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __MergeEngineTypes)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nlpEngineType: KPropertyPath<T, NlpEngineType?>
    get() = KPropertyPath(this, __NlpEngineType)

  public val normalizeText: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __NormalizeText)

  public val qualifiedName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __QualifiedName)

  public val supportSubEntities: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __SupportSubEntities)

  public val supportedLocales: KCollectionSimplePropertyPath<T, Locale?>
    get() = KCollectionSimplePropertyPath(this, __SupportedLocales)

  public val unknownIntentThreshold: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __UnknownIntentThreshold)

  public val useEntityModels: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __UseEntityModels)

  override fun memberWithAdditionalPath(additionalPath: String): ApplicationDefinition_<T> = ApplicationDefinition_(this, customProperty(this, additionalPath))
}

public class ApplicationDefinition_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ApplicationDefinition?>?>,
) : KMapPropertyPath<T, K, ApplicationDefinition?, ApplicationDefinition_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, ___id)

  public val intentStatesMap: KMapSimplePropertyPath<T, Id<IntentDefinition>?, Set<String>?>
    get() = KMapSimplePropertyPath(this, __IntentStatesMap)

  public val intents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
    get() = KCollectionSimplePropertyPath(this, __Intents)

  public val knownIntentThreshold: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __KnownIntentThreshold)

  public val label: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Label)

  public val mergeEngineTypes: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __MergeEngineTypes)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nlpEngineType: KPropertyPath<T, NlpEngineType?>
    get() = KPropertyPath(this, __NlpEngineType)

  public val normalizeText: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __NormalizeText)

  public val qualifiedName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __QualifiedName)

  public val supportSubEntities: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __SupportSubEntities)

  public val supportedLocales: KCollectionSimplePropertyPath<T, Locale?>
    get() = KCollectionSimplePropertyPath(this, __SupportedLocales)

  public val unknownIntentThreshold: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __UnknownIntentThreshold)

  public val useEntityModels: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __UseEntityModels)

  override fun memberWithAdditionalPath(additionalPath: String): ApplicationDefinition_<T> = ApplicationDefinition_(this, customProperty(this, additionalPath))
}
