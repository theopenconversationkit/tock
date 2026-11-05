@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.config

import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<EntityTypeDefinition, Id<EntityTypeDefinition>?>
  get() = EntityTypeDefinition::_id

private val __Description: KProperty1<EntityTypeDefinition, String?>
  get() = EntityTypeDefinition::description

private val __Dictionary: KProperty1<EntityTypeDefinition, Boolean?>
  get() = EntityTypeDefinition::dictionary

private val __Name: KProperty1<EntityTypeDefinition, String?>
  get() = EntityTypeDefinition::name

private val __Obfuscated: KProperty1<EntityTypeDefinition, Boolean?>
  get() = EntityTypeDefinition::obfuscated

private val __SubEntities: KProperty1<EntityTypeDefinition, List<EntityDefinition>?>
  get() = EntityTypeDefinition::subEntities

public class EntityTypeDefinition_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, EntityTypeDefinition?>,
) : KPropertyPath<T, EntityTypeDefinition?>(previous, property) {
  public val _id: KPropertyPath<T, Id<EntityTypeDefinition>?>
    get() = KPropertyPath(this, ___id)

  public val description: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Description)

  public val dictionary: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Dictionary)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val obfuscated: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Obfuscated)

  public val subEntities: EntityDefinition_Col<T>
    get() = EntityDefinition_Col(this, __SubEntities)

  public companion object {
    public val _id: KProperty1<EntityTypeDefinition, Id<EntityTypeDefinition>?>
      get() = ___id

    public val Description: KProperty1<EntityTypeDefinition, String?>
      get() = __Description

    public val Dictionary: KProperty1<EntityTypeDefinition, Boolean?>
      get() = __Dictionary

    public val Name: KProperty1<EntityTypeDefinition, String?>
      get() = __Name

    public val Obfuscated: KProperty1<EntityTypeDefinition, Boolean?>
      get() = __Obfuscated

    public val SubEntities: EntityDefinition_Col<EntityTypeDefinition>
      get() = EntityDefinition_Col(null, __SubEntities)
  }
}

public class EntityTypeDefinition_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<EntityTypeDefinition?>?>,
) : KCollectionPropertyPath<T, EntityTypeDefinition?, EntityTypeDefinition_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<EntityTypeDefinition>?>
    get() = KPropertyPath(this, ___id)

  public val description: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Description)

  public val dictionary: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Dictionary)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val obfuscated: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Obfuscated)

  public val subEntities: EntityDefinition_Col<T>
    get() = EntityDefinition_Col(this, __SubEntities)

  override fun memberWithAdditionalPath(additionalPath: String): EntityTypeDefinition_<T> = EntityTypeDefinition_(this, customProperty(this, additionalPath))
}

public class EntityTypeDefinition_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, EntityTypeDefinition?>?>,
) : KMapPropertyPath<T, K, EntityTypeDefinition?, EntityTypeDefinition_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<EntityTypeDefinition>?>
    get() = KPropertyPath(this, ___id)

  public val description: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Description)

  public val dictionary: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Dictionary)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val obfuscated: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Obfuscated)

  public val subEntities: EntityDefinition_Col<T>
    get() = EntityDefinition_Col(this, __SubEntities)

  override fun memberWithAdditionalPath(additionalPath: String): EntityTypeDefinition_<T> = EntityTypeDefinition_(this, customProperty(this, additionalPath))
}
