@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.config

import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __End: KProperty1<ClassifiedEntity, Int?>
  get() = ClassifiedEntity::end

private val __Role: KProperty1<ClassifiedEntity, String?>
  get() = ClassifiedEntity::role

private val __Start: KProperty1<ClassifiedEntity, Int?>
  get() = ClassifiedEntity::start

private val __SubEntities: KProperty1<ClassifiedEntity, List<ClassifiedEntity>?>
  get() = ClassifiedEntity::subEntities

private val __Type: KProperty1<ClassifiedEntity, String?>
  get() = ClassifiedEntity::type

public class ClassifiedEntity_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ClassifiedEntity?>,
) : KPropertyPath<T, ClassifiedEntity?>(previous, property) {
  public val end: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __End)

  public val role: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Role)

  public val start: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Start)

  public val subEntities: ClassifiedEntity_Col<T>
    get() = ClassifiedEntity_Col(this, __SubEntities)

  public val type: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Type)

  public companion object {
    public val End: KProperty1<ClassifiedEntity, Int?>
      get() = __End

    public val Role: KProperty1<ClassifiedEntity, String?>
      get() = __Role

    public val Start: KProperty1<ClassifiedEntity, Int?>
      get() = __Start

    public val SubEntities: ClassifiedEntity_Col<ClassifiedEntity>
      get() = ClassifiedEntity_Col(null, __SubEntities)

    public val Type: KProperty1<ClassifiedEntity, String?>
      get() = __Type
  }
}

public class ClassifiedEntity_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ClassifiedEntity?>?>,
) : KCollectionPropertyPath<T, ClassifiedEntity?, ClassifiedEntity_<T>>(previous, property) {
  public val end: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __End)

  public val role: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Role)

  public val start: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Start)

  public val subEntities: ClassifiedEntity_Col<T>
    get() = ClassifiedEntity_Col(this, __SubEntities)

  public val type: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Type)

  override fun memberWithAdditionalPath(additionalPath: String): ClassifiedEntity_<T> = ClassifiedEntity_(this, customProperty(this, additionalPath))
}

public class ClassifiedEntity_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ClassifiedEntity?>?>,
) : KMapPropertyPath<T, K, ClassifiedEntity?, ClassifiedEntity_<T>>(previous, property) {
  public val end: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __End)

  public val role: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Role)

  public val start: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Start)

  public val subEntities: ClassifiedEntity_Col<T>
    get() = ClassifiedEntity_Col(this, __SubEntities)

  public val type: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Type)

  override fun memberWithAdditionalPath(additionalPath: String): ClassifiedEntity_<T> = ClassifiedEntity_(this, customProperty(this, additionalPath))
}
