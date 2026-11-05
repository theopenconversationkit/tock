@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.build

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __All: KProperty1<ModelBuildTrigger, Boolean?>
  get() = ModelBuildTrigger::all

private val __ApplicationId: KProperty1<ModelBuildTrigger, Id<ApplicationDefinition>?>
  get() = ModelBuildTrigger::applicationId

private val __OnlyIfModelNotExists: KProperty1<ModelBuildTrigger, Boolean?>
  get() = ModelBuildTrigger::onlyIfModelNotExists

public class ModelBuildTrigger_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ModelBuildTrigger?>,
) : KPropertyPath<T, ModelBuildTrigger?>(previous, property) {
  public val all: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __All)

  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val onlyIfModelNotExists: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __OnlyIfModelNotExists)

  public companion object {
    public val All: KProperty1<ModelBuildTrigger, Boolean?>
      get() = __All

    public val ApplicationId: KProperty1<ModelBuildTrigger, Id<ApplicationDefinition>?>
      get() = __ApplicationId

    public val OnlyIfModelNotExists: KProperty1<ModelBuildTrigger, Boolean?>
      get() = __OnlyIfModelNotExists
  }
}

public class ModelBuildTrigger_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ModelBuildTrigger?>?>,
) : KCollectionPropertyPath<T, ModelBuildTrigger?, ModelBuildTrigger_<T>>(previous, property) {
  public val all: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __All)

  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val onlyIfModelNotExists: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __OnlyIfModelNotExists)

  override fun memberWithAdditionalPath(additionalPath: String): ModelBuildTrigger_<T> = ModelBuildTrigger_(this, customProperty(this, additionalPath))
}

public class ModelBuildTrigger_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ModelBuildTrigger?>?>,
) : KMapPropertyPath<T, K, ModelBuildTrigger?, ModelBuildTrigger_<T>>(previous, property) {
  public val all: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __All)

  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val onlyIfModelNotExists: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __OnlyIfModelNotExists)

  override fun memberWithAdditionalPath(additionalPath: String): ModelBuildTrigger_<T> = ModelBuildTrigger_(this, customProperty(this, additionalPath))
}
