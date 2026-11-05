@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration_
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __Story: KProperty1<StoryLookup, StoryDefinitionConfiguration?>
  get() = StoryLookup::story

internal class StoryLookup_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, StoryLookup?>,
) : KPropertyPath<T, StoryLookup?>(previous, property) {
  public val story: StoryDefinitionConfiguration_<T>
    get() = StoryDefinitionConfiguration_(this, __Story)

  public companion object {
    public val Story: StoryDefinitionConfiguration_<StoryLookup>
      get() = StoryDefinitionConfiguration_(null, __Story)
  }
}

internal class StoryLookup_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<StoryLookup?>?>,
) : KCollectionPropertyPath<T, StoryLookup?, StoryLookup_<T>>(previous, property) {
  public val story: StoryDefinitionConfiguration_<T>
    get() = StoryDefinitionConfiguration_(this, __Story)

  override fun memberWithAdditionalPath(additionalPath: String): StoryLookup_<T> = StoryLookup_(this, customProperty(this, additionalPath))
}

internal class StoryLookup_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, StoryLookup?>?>,
) : KMapPropertyPath<T, K, StoryLookup?, StoryLookup_<T>>(previous, property) {
  public val story: StoryDefinitionConfiguration_<T>
    get() = StoryDefinitionConfiguration_(this, __Story)

  override fun memberWithAdditionalPath(additionalPath: String): StoryLookup_<T> = StoryLookup_(this, customProperty(this, additionalPath))
}
