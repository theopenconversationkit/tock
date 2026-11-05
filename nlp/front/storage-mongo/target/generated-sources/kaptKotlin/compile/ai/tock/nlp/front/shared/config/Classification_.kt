@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.config

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

private val __Entities: KProperty1<Classification, List<ClassifiedEntity>?>
  get() = Classification::entities

private val __IntentId: KProperty1<Classification, Id<IntentDefinition>?>
  get() = Classification::intentId

public class Classification_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Classification?>,
) : KPropertyPath<T, Classification?>(previous, property) {
  public val entities: ClassifiedEntity_Col<T>
    get() = ClassifiedEntity_Col(this, __Entities)

  public val intentId: KPropertyPath<T, Id<IntentDefinition>?>
    get() = KPropertyPath(this, __IntentId)

  public companion object {
    public val Entities: ClassifiedEntity_Col<Classification>
      get() = ClassifiedEntity_Col(null, __Entities)

    public val IntentId: KProperty1<Classification, Id<IntentDefinition>?>
      get() = __IntentId
  }
}

public class Classification_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<Classification?>?>,
) : KCollectionPropertyPath<T, Classification?, Classification_<T>>(previous, property) {
  public val entities: ClassifiedEntity_Col<T>
    get() = ClassifiedEntity_Col(this, __Entities)

  public val intentId: KPropertyPath<T, Id<IntentDefinition>?>
    get() = KPropertyPath(this, __IntentId)

  override fun memberWithAdditionalPath(additionalPath: String): Classification_<T> = Classification_(this, customProperty(this, additionalPath))
}

public class Classification_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, Classification?>?>,
) : KMapPropertyPath<T, K, Classification?, Classification_<T>>(previous, property) {
  public val entities: ClassifiedEntity_Col<T>
    get() = ClassifiedEntity_Col(this, __Entities)

  public val intentId: KPropertyPath<T, Id<IntentDefinition>?>
    get() = KPropertyPath(this, __IntentId)

  override fun memberWithAdditionalPath(additionalPath: String): Classification_<T> = Classification_(this, customProperty(this, additionalPath))
}
