@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.config

import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __AtStartOfDay: KProperty1<EntityDefinition, Boolean?>
  get() = EntityDefinition::atStartOfDay

private val __EntityTypeName: KProperty1<EntityDefinition, String?>
  get() = EntityDefinition::entityTypeName

private val __Role: KProperty1<EntityDefinition, String?>
  get() = EntityDefinition::role

public class EntityDefinition_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, EntityDefinition?>,
) : KPropertyPath<T, EntityDefinition?>(previous, property) {
  public val atStartOfDay: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __AtStartOfDay)

  public val entityTypeName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __EntityTypeName)

  public val role: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Role)

  public companion object {
    public val AtStartOfDay: KProperty1<EntityDefinition, Boolean?>
      get() = __AtStartOfDay

    public val EntityTypeName: KProperty1<EntityDefinition, String?>
      get() = __EntityTypeName

    public val Role: KProperty1<EntityDefinition, String?>
      get() = __Role
  }
}

public class EntityDefinition_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<EntityDefinition?>?>,
) : KCollectionPropertyPath<T, EntityDefinition?, EntityDefinition_<T>>(previous, property) {
  public val atStartOfDay: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __AtStartOfDay)

  public val entityTypeName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __EntityTypeName)

  public val role: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Role)

  override fun memberWithAdditionalPath(additionalPath: String): EntityDefinition_<T> = EntityDefinition_(this, customProperty(this, additionalPath))
}

public class EntityDefinition_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, EntityDefinition?>?>,
) : KMapPropertyPath<T, K, EntityDefinition?, EntityDefinition_<T>>(previous, property) {
  public val atStartOfDay: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __AtStartOfDay)

  public val entityTypeName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __EntityTypeName)

  public val role: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Role)

  override fun memberWithAdditionalPath(additionalPath: String): EntityDefinition_<T> = EntityDefinition_(this, customProperty(this, additionalPath))
}
