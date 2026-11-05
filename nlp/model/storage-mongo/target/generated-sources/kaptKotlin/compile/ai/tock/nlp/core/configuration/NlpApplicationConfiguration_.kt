@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.core.configuration

import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ApplicationConfiguration:
    KProperty1<NlpApplicationConfiguration, NlpModelConfiguration?>
  get() = NlpApplicationConfiguration::applicationConfiguration

private val __EntityConfiguration: KProperty1<NlpApplicationConfiguration, NlpModelConfiguration?>
  get() = NlpApplicationConfiguration::entityConfiguration

private val __HasApplicationConfiguration: KProperty1<NlpApplicationConfiguration, Boolean?>
  get() = NlpApplicationConfiguration::hasApplicationConfiguration

private val __HasEntityConfiguration: KProperty1<NlpApplicationConfiguration, Boolean?>
  get() = NlpApplicationConfiguration::hasEntityConfiguration

private val __HasIntentConfiguration: KProperty1<NlpApplicationConfiguration, Boolean?>
  get() = NlpApplicationConfiguration::hasIntentConfiguration

private val __HasTokenizerConfiguration: KProperty1<NlpApplicationConfiguration, Boolean?>
  get() = NlpApplicationConfiguration::hasTokenizerConfiguration

private val __IntentConfiguration: KProperty1<NlpApplicationConfiguration, NlpModelConfiguration?>
  get() = NlpApplicationConfiguration::intentConfiguration

private val __TokenizerConfiguration:
    KProperty1<NlpApplicationConfiguration, NlpModelConfiguration?>
  get() = NlpApplicationConfiguration::tokenizerConfiguration

public class NlpApplicationConfiguration_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, NlpApplicationConfiguration?>,
) : KPropertyPath<T, NlpApplicationConfiguration?>(previous, property) {
  public val applicationConfiguration: NlpModelConfiguration_<T>
    get() = NlpModelConfiguration_(this, __ApplicationConfiguration)

  public val entityConfiguration: NlpModelConfiguration_<T>
    get() = NlpModelConfiguration_(this, __EntityConfiguration)

  public val hasApplicationConfiguration: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasApplicationConfiguration)

  public val hasEntityConfiguration: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasEntityConfiguration)

  public val hasIntentConfiguration: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasIntentConfiguration)

  public val hasTokenizerConfiguration: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasTokenizerConfiguration)

  public val intentConfiguration: NlpModelConfiguration_<T>
    get() = NlpModelConfiguration_(this, __IntentConfiguration)

  public val tokenizerConfiguration: NlpModelConfiguration_<T>
    get() = NlpModelConfiguration_(this, __TokenizerConfiguration)

  public companion object {
    public val ApplicationConfiguration: NlpModelConfiguration_<NlpApplicationConfiguration>
      get() = NlpModelConfiguration_(null, __ApplicationConfiguration)

    public val EntityConfiguration: NlpModelConfiguration_<NlpApplicationConfiguration>
      get() = NlpModelConfiguration_(null, __EntityConfiguration)

    public val HasApplicationConfiguration: KProperty1<NlpApplicationConfiguration, Boolean?>
      get() = __HasApplicationConfiguration

    public val HasEntityConfiguration: KProperty1<NlpApplicationConfiguration, Boolean?>
      get() = __HasEntityConfiguration

    public val HasIntentConfiguration: KProperty1<NlpApplicationConfiguration, Boolean?>
      get() = __HasIntentConfiguration

    public val HasTokenizerConfiguration: KProperty1<NlpApplicationConfiguration, Boolean?>
      get() = __HasTokenizerConfiguration

    public val IntentConfiguration: NlpModelConfiguration_<NlpApplicationConfiguration>
      get() = NlpModelConfiguration_(null, __IntentConfiguration)

    public val TokenizerConfiguration: NlpModelConfiguration_<NlpApplicationConfiguration>
      get() = NlpModelConfiguration_(null, __TokenizerConfiguration)
  }
}

public class NlpApplicationConfiguration_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<NlpApplicationConfiguration?>?>,
) : KCollectionPropertyPath<T, NlpApplicationConfiguration?, NlpApplicationConfiguration_<T>>(previous, property) {
  public val applicationConfiguration: NlpModelConfiguration_<T>
    get() = NlpModelConfiguration_(this, __ApplicationConfiguration)

  public val entityConfiguration: NlpModelConfiguration_<T>
    get() = NlpModelConfiguration_(this, __EntityConfiguration)

  public val hasApplicationConfiguration: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasApplicationConfiguration)

  public val hasEntityConfiguration: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasEntityConfiguration)

  public val hasIntentConfiguration: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasIntentConfiguration)

  public val hasTokenizerConfiguration: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasTokenizerConfiguration)

  public val intentConfiguration: NlpModelConfiguration_<T>
    get() = NlpModelConfiguration_(this, __IntentConfiguration)

  public val tokenizerConfiguration: NlpModelConfiguration_<T>
    get() = NlpModelConfiguration_(this, __TokenizerConfiguration)

  override fun memberWithAdditionalPath(additionalPath: String): NlpApplicationConfiguration_<T> = NlpApplicationConfiguration_(this, customProperty(this, additionalPath))
}

public class NlpApplicationConfiguration_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, NlpApplicationConfiguration?>?>,
) : KMapPropertyPath<T, K, NlpApplicationConfiguration?, NlpApplicationConfiguration_<T>>(previous, property) {
  public val applicationConfiguration: NlpModelConfiguration_<T>
    get() = NlpModelConfiguration_(this, __ApplicationConfiguration)

  public val entityConfiguration: NlpModelConfiguration_<T>
    get() = NlpModelConfiguration_(this, __EntityConfiguration)

  public val hasApplicationConfiguration: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasApplicationConfiguration)

  public val hasEntityConfiguration: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasEntityConfiguration)

  public val hasIntentConfiguration: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasIntentConfiguration)

  public val hasTokenizerConfiguration: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasTokenizerConfiguration)

  public val intentConfiguration: NlpModelConfiguration_<T>
    get() = NlpModelConfiguration_(this, __IntentConfiguration)

  public val tokenizerConfiguration: NlpModelConfiguration_<T>
    get() = NlpModelConfiguration_(this, __TokenizerConfiguration)

  override fun memberWithAdditionalPath(additionalPath: String): NlpApplicationConfiguration_<T> = NlpApplicationConfiguration_(this, customProperty(this, additionalPath))
}
