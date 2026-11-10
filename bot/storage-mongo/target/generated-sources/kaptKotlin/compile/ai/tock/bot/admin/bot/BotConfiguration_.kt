@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.admin.bot

import java.util.Locale
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ApiKey: KProperty1<BotConfiguration, String?>
  get() = BotConfiguration::apiKey

private val __BotId: KProperty1<BotConfiguration, String?>
  get() = BotConfiguration::botId

private val __Name: KProperty1<BotConfiguration, String?>
  get() = BotConfiguration::name

private val __Namespace: KProperty1<BotConfiguration, String?>
  get() = BotConfiguration::namespace

private val __NlpModel: KProperty1<BotConfiguration, String?>
  get() = BotConfiguration::nlpModel

private val __SupportedLocales: KProperty1<BotConfiguration, Set<Locale>?>
  get() = BotConfiguration::supportedLocales

private val __WebhookUrl: KProperty1<BotConfiguration, String?>
  get() = BotConfiguration::webhookUrl

public class BotConfiguration_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, BotConfiguration?>,
) : KPropertyPath<T, BotConfiguration?>(previous, property) {
  public val apiKey: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApiKey)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nlpModel: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __NlpModel)

  public val supportedLocales: KCollectionSimplePropertyPath<T, Locale?>
    get() = KCollectionSimplePropertyPath(this, __SupportedLocales)

  public val webhookUrl: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __WebhookUrl)

  public companion object {
    public val ApiKey: KProperty1<BotConfiguration, String?>
      get() = __ApiKey

    public val BotId: KProperty1<BotConfiguration, String?>
      get() = __BotId

    public val Name: KProperty1<BotConfiguration, String?>
      get() = __Name

    public val Namespace: KProperty1<BotConfiguration, String?>
      get() = __Namespace

    public val NlpModel: KProperty1<BotConfiguration, String?>
      get() = __NlpModel

    public val SupportedLocales: KCollectionSimplePropertyPath<BotConfiguration, Locale?>
      get() = KCollectionSimplePropertyPath(null, __SupportedLocales)

    public val WebhookUrl: KProperty1<BotConfiguration, String?>
      get() = __WebhookUrl
  }
}

public class BotConfiguration_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<BotConfiguration?>?>,
) : KCollectionPropertyPath<T, BotConfiguration?, BotConfiguration_<T>>(previous, property) {
  public val apiKey: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApiKey)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nlpModel: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __NlpModel)

  public val supportedLocales: KCollectionSimplePropertyPath<T, Locale?>
    get() = KCollectionSimplePropertyPath(this, __SupportedLocales)

  public val webhookUrl: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __WebhookUrl)

  override fun memberWithAdditionalPath(additionalPath: String): BotConfiguration_<T> = BotConfiguration_(this, customProperty(this, additionalPath))
}

public class BotConfiguration_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, BotConfiguration?>?>,
) : KMapPropertyPath<T, K, BotConfiguration?, BotConfiguration_<T>>(previous, property) {
  public val apiKey: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApiKey)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nlpModel: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __NlpModel)

  public val supportedLocales: KCollectionSimplePropertyPath<T, Locale?>
    get() = KCollectionSimplePropertyPath(this, __SupportedLocales)

  public val webhookUrl: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __WebhookUrl)

  override fun memberWithAdditionalPath(additionalPath: String): BotConfiguration_<T> = BotConfiguration_(this, customProperty(this, additionalPath))
}
