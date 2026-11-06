@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.Intent_
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __Actions: KProperty1<DialogCol.StoryMongoWrapper, List<DialogCol.ActionMongoWrapper>?>
  get() = DialogCol.StoryMongoWrapper::actions

private val __CurrentIntent: KProperty1<DialogCol.StoryMongoWrapper, Intent?>
  get() = DialogCol.StoryMongoWrapper::currentIntent

private val __CurrentStep: KProperty1<DialogCol.StoryMongoWrapper, String?>
  get() = DialogCol.StoryMongoWrapper::currentStep

private val __StoryDefinitionId: KProperty1<DialogCol.StoryMongoWrapper, String?>
  get() = DialogCol.StoryMongoWrapper::storyDefinitionId

internal class StoryMongoWrapper_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DialogCol.StoryMongoWrapper?>,
) : KPropertyPath<T, DialogCol.StoryMongoWrapper?>(previous, property) {
  public val actions: ActionMongoWrapper_Col<T>
    get() = ActionMongoWrapper_Col(this, __Actions)

  public val currentIntent: Intent_<T>
    get() = Intent_(this, __CurrentIntent)

  public val currentStep: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __CurrentStep)

  public val storyDefinitionId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryDefinitionId)

  public companion object {
    public val Actions: ActionMongoWrapper_Col<DialogCol.StoryMongoWrapper>
      get() = ActionMongoWrapper_Col(null, __Actions)

    public val CurrentIntent: Intent_<DialogCol.StoryMongoWrapper>
      get() = Intent_(null, __CurrentIntent)

    public val CurrentStep: KProperty1<DialogCol.StoryMongoWrapper, String?>
      get() = __CurrentStep

    public val StoryDefinitionId: KProperty1<DialogCol.StoryMongoWrapper, String?>
      get() = __StoryDefinitionId
  }
}

internal class StoryMongoWrapper_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DialogCol.StoryMongoWrapper?>?>,
) : KCollectionPropertyPath<T, DialogCol.StoryMongoWrapper?, StoryMongoWrapper_<T>>(previous, property) {
  public val actions: ActionMongoWrapper_Col<T>
    get() = ActionMongoWrapper_Col(this, __Actions)

  public val currentIntent: Intent_<T>
    get() = Intent_(this, __CurrentIntent)

  public val currentStep: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __CurrentStep)

  public val storyDefinitionId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryDefinitionId)

  override fun memberWithAdditionalPath(additionalPath: String): StoryMongoWrapper_<T> = StoryMongoWrapper_(this, customProperty(this, additionalPath))
}

internal class StoryMongoWrapper_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DialogCol.StoryMongoWrapper?>?>,
) : KMapPropertyPath<T, K, DialogCol.StoryMongoWrapper?, StoryMongoWrapper_<T>>(previous, property) {
  public val actions: ActionMongoWrapper_Col<T>
    get() = ActionMongoWrapper_Col(this, __Actions)

  public val currentIntent: Intent_<T>
    get() = Intent_(this, __CurrentIntent)

  public val currentStep: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __CurrentStep)

  public val storyDefinitionId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryDefinitionId)

  override fun memberWithAdditionalPath(additionalPath: String): StoryMongoWrapper_<T> = StoryMongoWrapper_(this, customProperty(this, additionalPath))
}
