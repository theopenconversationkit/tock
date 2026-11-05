@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.config

import ai.tock.nlp.core.EntitiesRegexp
import java.util.LinkedHashSet
import java.util.Locale
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

private val ___id: KProperty1<IntentDefinition, Id<IntentDefinition>?>
  get() = IntentDefinition::_id

private val __Applications: KProperty1<IntentDefinition, Set<Id<ApplicationDefinition>>?>
  get() = IntentDefinition::applications

private val __Category: KProperty1<IntentDefinition, String?>
  get() = IntentDefinition::category

private val __Description: KProperty1<IntentDefinition, String?>
  get() = IntentDefinition::description

private val __Entities: KProperty1<IntentDefinition, Set<EntityDefinition>?>
  get() = IntentDefinition::entities

private val __EntitiesRegexp:
    KProperty1<IntentDefinition, Map<Locale, LinkedHashSet<EntitiesRegexp>>?>
  get() = IntentDefinition::entitiesRegexp

private val __Label: KProperty1<IntentDefinition, String?>
  get() = IntentDefinition::label

private val __MandatoryStates: KProperty1<IntentDefinition, Set<String>?>
  get() = IntentDefinition::mandatoryStates

private val __Name: KProperty1<IntentDefinition, String?>
  get() = IntentDefinition::name

private val __Namespace: KProperty1<IntentDefinition, String?>
  get() = IntentDefinition::namespace

private val __QualifiedName: KProperty1<IntentDefinition, String?>
  get() = IntentDefinition::qualifiedName

private val __SharedIntents: KProperty1<IntentDefinition, Set<Id<IntentDefinition>>?>
  get() = IntentDefinition::sharedIntents

public class IntentDefinition_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, IntentDefinition?>,
) : KPropertyPath<T, IntentDefinition?>(previous, property) {
  public val _id: KPropertyPath<T, Id<IntentDefinition>?>
    get() = KPropertyPath(this, ___id)

  public val applications: KCollectionSimplePropertyPath<T, Id<ApplicationDefinition>?>
    get() = KCollectionSimplePropertyPath(this, __Applications)

  public val category: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Category)

  public val description: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Description)

  public val entities: EntityDefinition_Col<T>
    get() = EntityDefinition_Col(this, __Entities)

  public val entitiesRegexp: KMapSimplePropertyPath<T, Locale?, LinkedHashSet<EntitiesRegexp>?>
    get() = KMapSimplePropertyPath(this, __EntitiesRegexp)

  public val label: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Label)

  public val mandatoryStates: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __MandatoryStates)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val qualifiedName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __QualifiedName)

  public val sharedIntents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
    get() = KCollectionSimplePropertyPath(this, __SharedIntents)

  public companion object {
    public val _id: KProperty1<IntentDefinition, Id<IntentDefinition>?>
      get() = ___id

    public val Applications:
        KCollectionSimplePropertyPath<IntentDefinition, Id<ApplicationDefinition>?>
      get() = KCollectionSimplePropertyPath(null, __Applications)

    public val Category: KProperty1<IntentDefinition, String?>
      get() = __Category

    public val Description: KProperty1<IntentDefinition, String?>
      get() = __Description

    public val Entities: EntityDefinition_Col<IntentDefinition>
      get() = EntityDefinition_Col(null, __Entities)

    public val EntitiesRegexp:
        KMapSimplePropertyPath<IntentDefinition, Locale?, LinkedHashSet<EntitiesRegexp>?>
      get() = KMapSimplePropertyPath(null, __EntitiesRegexp)

    public val Label: KProperty1<IntentDefinition, String?>
      get() = __Label

    public val MandatoryStates: KCollectionSimplePropertyPath<IntentDefinition, String?>
      get() = KCollectionSimplePropertyPath(null, __MandatoryStates)

    public val Name: KProperty1<IntentDefinition, String?>
      get() = __Name

    public val Namespace: KProperty1<IntentDefinition, String?>
      get() = __Namespace

    public val QualifiedName: KProperty1<IntentDefinition, String?>
      get() = __QualifiedName

    public val SharedIntents: KCollectionSimplePropertyPath<IntentDefinition, Id<IntentDefinition>?>
      get() = KCollectionSimplePropertyPath(null, __SharedIntents)
  }
}

public class IntentDefinition_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<IntentDefinition?>?>,
) : KCollectionPropertyPath<T, IntentDefinition?, IntentDefinition_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<IntentDefinition>?>
    get() = KPropertyPath(this, ___id)

  public val applications: KCollectionSimplePropertyPath<T, Id<ApplicationDefinition>?>
    get() = KCollectionSimplePropertyPath(this, __Applications)

  public val category: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Category)

  public val description: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Description)

  public val entities: EntityDefinition_Col<T>
    get() = EntityDefinition_Col(this, __Entities)

  public val entitiesRegexp: KMapSimplePropertyPath<T, Locale?, LinkedHashSet<EntitiesRegexp>?>
    get() = KMapSimplePropertyPath(this, __EntitiesRegexp)

  public val label: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Label)

  public val mandatoryStates: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __MandatoryStates)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val qualifiedName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __QualifiedName)

  public val sharedIntents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
    get() = KCollectionSimplePropertyPath(this, __SharedIntents)

  override fun memberWithAdditionalPath(additionalPath: String): IntentDefinition_<T> = IntentDefinition_(this, customProperty(this, additionalPath))
}

public class IntentDefinition_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, IntentDefinition?>?>,
) : KMapPropertyPath<T, K, IntentDefinition?, IntentDefinition_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<IntentDefinition>?>
    get() = KPropertyPath(this, ___id)

  public val applications: KCollectionSimplePropertyPath<T, Id<ApplicationDefinition>?>
    get() = KCollectionSimplePropertyPath(this, __Applications)

  public val category: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Category)

  public val description: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Description)

  public val entities: EntityDefinition_Col<T>
    get() = EntityDefinition_Col(this, __Entities)

  public val entitiesRegexp: KMapSimplePropertyPath<T, Locale?, LinkedHashSet<EntitiesRegexp>?>
    get() = KMapSimplePropertyPath(this, __EntitiesRegexp)

  public val label: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Label)

  public val mandatoryStates: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __MandatoryStates)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val qualifiedName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __QualifiedName)

  public val sharedIntents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
    get() = KCollectionSimplePropertyPath(this, __SharedIntents)

  override fun memberWithAdditionalPath(additionalPath: String): IntentDefinition_<T> = IntentDefinition_(this, customProperty(this, additionalPath))
}
